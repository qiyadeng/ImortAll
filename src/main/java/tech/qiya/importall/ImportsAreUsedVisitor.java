package tech.qiya.importall;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.InheritanceUtil;
import com.siyeh.ig.psiutils.ImportUtils;
import org.jetbrains.annotations.NotNull;
import tech.qiya.importall.data.CurrentNeedImportClassesData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImportsAreUsedVisitor extends JavaElementVisitor {

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

        //LOG.warn("---------visitElement-----------" + element.toString());
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

        LOG.warn("---------followReferenceToImport-----------" + reference.toString());
        if("PsiJavaCodeReferenceElement:My".equalsIgnoreCase(reference.toString())){
            LOG.warn("--------at My-----------");
            PsiClass[] classes = PsiShortNamesCache.getInstance(reference.getProject()).getClassesByName("My", reference.getResolveScope());
            for (PsiClass psiClass : classes) {
                LOG.warn("--------psiClass-----------" + psiClass.getQualifiedName());
            }
//            PsiManager psiManager = reference.getManager();
//            PsiClass inner = psiManager.findClass("tech.qiya.importall.My", reference.getResolveScope());
//            PsiImportStatement importStatement = psiManager.getElementFactory().createImportStatement(inner);
        }

        if (reference.getQualifier() != null) {
            // it's already fully qualified, so the import statement wasn't
            // responsible
            LOG.warn("reference.getQualifier() != null" + reference.getQualifier());
            return;
        }
        // during typing there can be incomplete code
        final JavaResolveResult resolveResult = reference.advancedResolve(true);
        LOG.warn("resolveResult: " + resolveResult.getElement());

        PsiElement element = resolveResult.getElement();
        if (element == null) {
            JavaResolveResult[] results = reference.multiResolve(true);
//            for (JavaResolveResult result : results) {
//                LOG.warn("result: " + result.getElement());
//            }
            if (results.length > 0) {
                element = results[0].getElement();
            }
            LOG.warn("$$$$$$$$$$$"+reference.getText());
            PsiClass[] classes = PsiShortNamesCache.getInstance(reference.getProject()).getClassesByName(reference.getReferenceName(), reference.getResolveScope());
            if(CurrentNeedImportClassesData.list== null){
                CurrentNeedImportClassesData.list = new ArrayList<>();
            }
            CurrentNeedImportClassesData.list.add(classes);
            for (PsiClass psiClass : classes) {
                LOG.warn("--------psiClass-----------" + psiClass.getQualifiedName());
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
            //LOG.warn(" ---qualifiedName: " + qualifiedName );
        }
        else {
            if (!member.hasModifierProperty(PsiModifier.STATIC) || containingClass == null) {
                return null;
            }
            packageName = containingClass.getQualifiedName();
            qualifiedName = packageName + '.' + member.getName();
            //LOG.warn("memmber is not instance of PsiClass =" + qualifiedName);
        }
        if (packageName == null) {
            return null;
        }
        final boolean hasOnDemandImportConflict = ImportUtils.hasOnDemandImportConflict(qualifiedName, myFile);
        for (PsiImportStatementBase importStatement : importStatements) {
            //LOG.warn("*******importStatement: " + importStatement.getText());
            if (!importStatement.isOnDemand()) {
                final PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
                if (reference == null) {
                    continue;
                }
                final JavaResolveResult[] targets = reference.multiResolve(false);
                for (JavaResolveResult target : targets) {
                    //LOG.warn(member.toString()+"----target: " + target.getElement());
                    if (member.equals(target.getElement())) {
                        return importStatement;
                    }
                }
            }
            else {
                //LOG.warn("$$$$$$$$$$$hasOnDemandImportConflict: " + importStatement.getText());
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
