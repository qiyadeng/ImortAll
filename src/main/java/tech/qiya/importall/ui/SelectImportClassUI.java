package tech.qiya.importall.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SelectImportClassUI extends DialogWrapper {
    private JTable allCandiClassesTable;
    private JLabel indexOfToatal;
    private JPanel mainPanel;



    public JComponent getComponent() {
        return mainPanel;
    }



    public SelectImportClassUI() {
        super(true);

        init();
    }


    public JTable getAllCandiClassesTable() {
        return allCandiClassesTable;
    }

    public void setAllCandiClassesTable(JTable allCandiClassesTable) {
        this.allCandiClassesTable = allCandiClassesTable;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainPanel;
    }
}
