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
import tech.qiya.importall.data.CurrentNeedImportClassesData;
import tech.qiya.importall.data.HasImportedClassesData;

import java.util.*;

public class ImportControlInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final Logger LOG = Logger.getInstance(ImportControlInspection.class);

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        LOG.warn("---------编译的时候开始分析-----------" + isOnTheFly);
        CurrentNeedImportClassesData.clear();
        HasImportedClassesData.clear();
        ImportsAreUsedVisitor visitor = new ImportsAreUsedVisitor((PsiJavaFile)holder.getFile());
        return visitor;
    }

}
