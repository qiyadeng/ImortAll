package tech.qiya.importall.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import tech.qiya.importall.ImportControlInspection;
import tech.qiya.importall.data.CurrentNeedImportClassesData;
import tech.qiya.importall.ui.ReadUI;
import tech.qiya.importall.ui.SelectImportClassUI;
import tech.qiya.importall.ui.TuantDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Vector;

public class ImportAllAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(ImportAllAction.class);

    @Override
    public void actionPerformed(AnActionEvent e) {
        SelectImportClassUI selectImportClassUI = new SelectImportClassUI();

        //ReadUI readUI = new ReadUI();
        //TuantDialog tuantDialog = new TuantDialog();
        //tuantDialog.show();
        Project project = e.getProject();
        List<PsiClass[]> list =  CurrentNeedImportClassesData.list;

        if(list==null || list.size()==0){
            LOG.warn("list is null or size is 0");
            return;
        }
        //打印list
        for(int i=0;i<list.size();i++){
            PsiClass[] psiClasses = list.get(i);
            for(int j=0;j<psiClasses.length;j++){
                PsiClass psiClass = psiClasses[j];
                //System.out.println(psiClass.getQualifiedName());
                LOG.warn("############"+i+"###########");
                LOG.warn("############"+psiClass.getQualifiedName()+"###########");
            }
        }

        JTable table = selectImportClassUI.getAllCandiClassesTable();
        //Object[] colums  = {"包名"};
        Vector<String> columns = new Vector<>();
        columns.add("包名");
//        Object[][] rows = new Object[list.size()][1];
//        for (int i = 0; i < list.size(); i++) {
//            PsiClass[] psiClasses = list.get(i);
//            rows[i][0] = psiClasses[0].getQualifiedName();
//        }



        Vector vector = new Vector<>();

        if(list!=null && list.size()>0){
            PsiClass[] keys = list.get(0);
            for(int j=0;j<keys.length;j++){
                Vector newNewVector = new Vector();
                for (int i = 0; i < columns.size(); i++) {

                    newNewVector.add(keys[i].getQualifiedName());


                }
                vector.add(newNewVector);
            }

            DefaultTableModel model = new DefaultTableModel(vector, columns);
            table.setModel(model);

        }

        table.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e1) {
                if (e1.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e1)) {
                    int row = table.getSelectedRow();
                    LOG.warn("click row: " + row);
                    String packagename = (String)table.getValueAt(row, 0);
                    LOG.warn("click packagename: " + packagename);
                    WriteCommandAction.runWriteCommandAction(project,()->{
                        PsiFile file = e.getData(LangDataKeys.PSI_FILE);
                        //get Editor
                        Editor editor = e.getData(CommonDataKeys.EDITOR);
                        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());

//                        PsiElement codeBlock = element;
//                        while (!(codeBlock instanceof PsiCodeBlock)) {
//                            codeBlock = codeBlock.getParent();
//                        }
//                        LOG.warn("codeBlock: " + codeBlock.toString());
                        // 使用PsiElementFactory创建表达式元素
//                        PsiElement newElement = PsiElementFactory.getInstance(element.getProject())
//                                .createExpressionFromText("Invocation<Object> invocation = Invocation.<Object>builder()\n" +
//                                        "                .scope(scope)\n" +
//                                        "                .service(payType)" +
//                                        "                .operate(\"" + "\")\n" +
//                                        "                .body(merchantNo)\n" +
//                                        "                .build()", element.getContext());
                        // 将新创建的表达式元素插入到光标停留在的元素的后面
                        //PsiElement newItem = PsiElementFactory.getInstance(element.getProject()).createImportStatement(list.get(0)[0]);
                        //codeBlock.addAfter(newElement, element);
                        //codeBlock.add(newItem);
                        //JavaCodeStyleManager.getInstance(project).shortenClassReferences(newItem);
                        JavaCodeStyleManager.getInstance(project).addImport((PsiJavaFile) file, list.get(0)[0]);
                    });
//                    if(list !=null && list.size()>0){
//                            PsiClass[] psiClasses = list.get(0);
//                            if (psiClasses != null && psiClasses.length>0) {
//                                PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
//                                LOG.warn("psiClasses[0].getQualifiedName(): " + psiClasses[0].getQualifiedName());
//                                PsiImportStatement importStatement = factory.createImportStatement(psiClasses[0]);
//                                JavaCodeStyleManager.getInstance(project).shortenClassReferences(importStatement);
//                                //https://intellij-support.jetbrains.com/hc/en-us/community/posts/206105479-Add-import-statements
//                                //把import语句写入到文件中
//                        }
//                    }

                }
            }


        });

        selectImportClassUI.show();


    }
}
