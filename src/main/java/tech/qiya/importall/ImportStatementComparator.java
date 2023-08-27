package tech.qiya.importall;

import com.intellij.psi.PsiImportStatementBase;
import com.siyeh.ig.psiutils.PsiElementOrderComparator;

import java.util.Comparator;

public class ImportStatementComparator implements Comparator<PsiImportStatementBase> {
    public static final ImportStatementComparator INSTANCE = new ImportStatementComparator();

    private ImportStatementComparator() {}

    public static ImportStatementComparator getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(PsiImportStatementBase importStatementBase1, PsiImportStatementBase importStatementBase2) {
        final boolean onDemand = importStatementBase1.isOnDemand();
        if (onDemand != importStatementBase2.isOnDemand()) {
            return onDemand ? -1 : 1;
        }
        // just sort on demand imports first, and sort the rest in reverse file order.
        return -PsiElementOrderComparator.getInstance().compare(importStatementBase1, importStatementBase2);
    }
}
