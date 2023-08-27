package tech.qiya.importall;

import com.intellij.codeInspection.*;
import com.intellij.codeInspection.unusedImport.UnusedImportInspection;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.FileTypeUtils;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.fixes.DeleteImportFix;
import com.siyeh.ig.psiutils.ImportUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ImportControlInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(ImportControlInspection.class);

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {


        JavaElementVisitor javaElementVisitor = new JavaElementVisitor() {

            private  PsiJavaFile myFile = null;
            private  List<PsiImportStatementBase> importStatements =  Collections.emptyList();;
            private  List<PsiImportStatementBase> usedImportStatements = new ArrayList<>();
            @Override
            public void visitField(PsiField field) {
                super.visitField(field);
                if (field.getName().equals("name")){
                    holder.registerProblem(field,"命名非法");
                }
            }


            @Override
            public void visitJavaFile(PsiJavaFile file) {
                super.visitJavaFile(file);
//                final ImportsAreUsedVisitor visitor = new ImportsAreUsedVisitor(file);
//                //file.accept(visitor);
//                for (PsiImportStatementBase unusedImportStatement : visitor.getUnusedImportStatements()) {
//                    PsiJavaCodeReferenceElement reference = unusedImportStatement.getImportReference();
//                    LOG.warn(reference.toString());
//                    if (reference != null &&
//                            reference.multiResolve(false).length > 0 &&
//                            !(PsiTreeUtil.skipWhitespacesForward(unusedImportStatement) instanceof PsiErrorElement)) {
//                        holder.registerProblem(unusedImportStatement,
//                                "这个引用没有使用",
//                                new DeleteImportFix());
//                    }
//                }
                myFile = file;
                final PsiImportList importList = file.getImportList();
                if (importList == null) {
                    importStatements = Collections.emptyList();
                } else {
                    final PsiImportStatementBase[] importStatements = importList.getAllImportStatements();
                    this.importStatements = new ArrayList<>(Arrays.asList(importStatements));
                    this.importStatements.sort(ImportStatementComparator.getInstance());
                }

                LOG.warn("visitJavaFile: " + file.toString());
                for (PsiImportStatementBase importStatement : this.importStatements) {
                    LOG.warn(importStatement.toString());
                }


            }

            @Override
            public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
                super.visitReferenceElement(reference);
                //LOG.warn("---followReferenceToImport: " + reference.toString());
                followReferenceToImport(reference);
                super.visitReferenceElement(reference);
            }

            @Override
            public void visitImportList(@NotNull PsiImportList list) {
                //ignore imports
                LOG.warn("-------visitImportList: " + list.toString());
                for (PsiImportStatementBase importList : list.getImportStaticStatements()){
                    LOG.warn("-------PsiImportStatementBase: " + importList.toString());
                }

            }

            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (importStatements.isEmpty()) {
                    return;
                }
                super.visitElement(element);
            }

            private void followReferenceToImport(PsiJavaCodeReferenceElement reference) {

                if (reference.getQualifier() != null) {
                    // it's already fully qualified, so the import statement wasn't
                    // responsible
                    return;
                }
                // during typing there can be incomplete code
                final JavaResolveResult resolveResult = reference.advancedResolve(true);
                PsiElement element = resolveResult.getElement();
                if (element == null) {
                    JavaResolveResult[] results = reference.multiResolve(false);
                    if (results.length > 0) {
                        element = results[0].getElement();
                    }
                }
                if (!(element instanceof PsiMember member)) {
                    return;
                }
                if (findImport(member, usedImportStatements) != null) {
                    return;
                }
                final PsiImportStatementBase foundImport = findImport(member, importStatements);
                if (foundImport != null) {
                    importStatements.remove(foundImport);
                    usedImportStatements.add(foundImport);
                }
            }

            private PsiImportStatementBase findImport(PsiMember member, List<? extends PsiImportStatementBase> importStatements) {
                final String qualifiedName;
                final String packageName;
                final PsiClass containingClass = member.getContainingClass();
                if (member instanceof PsiClass referencedClass) {
                    qualifiedName = referencedClass.getQualifiedName();
                    packageName = qualifiedName != null ? StringUtil.getPackageName(qualifiedName) : null;
                    LOG.warn("qualifiedName: " + qualifiedName);
                }
                else {
                    if (!member.hasModifierProperty(PsiModifier.STATIC) || containingClass == null) {
                        return null;
                    }
                    packageName = containingClass.getQualifiedName();
                    qualifiedName = packageName + '.' + member.getName();
                }
                if (packageName == null) {
                    return null;
                }
                final boolean hasOnDemandImportConflict = ImportUtils.hasOnDemandImportConflict(qualifiedName, myFile);
                for (PsiImportStatementBase importStatement : importStatements) {
                    LOG.warn("importStatement: " + importStatement.getText());
                    if (!importStatement.isOnDemand()) {
                        final PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
                        if (reference == null) {
                            continue;
                        }
                        final JavaResolveResult[] targets = reference.multiResolve(false);
                        for (JavaResolveResult target : targets) {
                            if (member.equals(target.getElement())) {
                                return importStatement;
                            }
                        }
                    }
                    else {
                        if (hasOnDemandImportConflict) {
                            continue;
                        }
                        final PsiElement target = importStatement.resolve();
                        if (target instanceof PsiPackage aPackage) {
                            if (packageName.equals(aPackage.getQualifiedName())) {
                                return importStatement;
                            }
                        }
                        else if (target instanceof PsiClass aClass) {
                            // a regular import statement does NOT import inner classes from super classes, but a static import does
                            if (importStatement instanceof PsiImportStaticStatement) {
                                if (member.hasModifierProperty(PsiModifier.STATIC) && InheritanceUtil.isInheritorOrSelf(aClass, containingClass, true)) {
                                    return importStatement;
                                }
                            }
                            else if (importStatement instanceof PsiImportStatement && member instanceof PsiClass && aClass.equals(containingClass)) {
                                return importStatement;
                            }
                        }
                    }
                }
                return null;
            }



        };

        return javaElementVisitor;
    }

}
