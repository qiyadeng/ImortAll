package tech.qiya.importall;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.siyeh.ig.psiutils.ImportUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImportsAreUsedVisitor extends JavaRecursiveElementWalkingVisitor {

    private static final Logger LOG = Logger.getInstance(ImportsAreUsedVisitor.class);
    private final PsiJavaFile myFile;
    private final List<PsiImportStatementBase> importStatements;
    private final List<PsiImportStatementBase> usedImportStatements = new ArrayList<>();

    ImportsAreUsedVisitor(PsiJavaFile file) {
        myFile = file;
        final PsiImportList importList = file.getImportList();
        if (importList == null) {
            importStatements = Collections.emptyList();
        } else {
            final PsiImportStatementBase[] importStatements = importList.getAllImportStatements();
            this.importStatements = new ArrayList<>(Arrays.asList(importStatements));
            this.importStatements.sort(ImportStatementComparator.getInstance());
        }
    }

    @Override
    public void visitImportList(@NotNull PsiImportList list) {
        //ignore imports
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (importStatements.isEmpty()) {
            return;
        }
        super.visitElement(element);
    }

    @Override
    public void visitReferenceElement(@NotNull PsiJavaCodeReferenceElement reference) {
        followReferenceToImport(reference);
        super.visitReferenceElement(reference);
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

    PsiImportStatementBase @NotNull [] getUnusedImportStatements() {

//        LOG.warn("importStatements: " + importStatements.size());
//        for(PsiImportStatementBase importStatement : importStatements) {
//            LOG.warn("importStatement: " + importStatement.getText());
//        }
        if (importStatements.isEmpty()) {
            return PsiImportStatementBase.EMPTY_ARRAY;
        }
        return importStatements.toArray(PsiImportStatementBase.EMPTY_ARRAY);
    }
}
