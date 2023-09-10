package tech.qiya.importall.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import tech.qiya.importall.data.CurrentNeedImportClassesData;
import tech.qiya.importall.data.HasImportedClassesData;
import tech.qiya.importall.ui.SelectImportClassUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
        Map map = CurrentNeedImportClassesData.map;
        removeHasImportedClass(map, HasImportedClassesData.map);
        autoImportOnlyOneCandidates(map,project,e);
        if(map==null || map.size()==0){
            LOG.warn("map is null");
            NotificationGroup notificationGroup = new NotificationGroup("ImportAll", NotificationDisplayType.BALLOON, false);
            Notification notification = notificationGroup.createNotification("所有包已经导入", MessageType.INFO);
            Notifications.Bus.notify(notification);

            //加一个IDEA的泡沫框消息提示
            return;
        }



        JTable table = selectImportClassUI.getAllCandiClassesTable();
        //表格不能编辑
        //table.setEnabled(false);
        //Object[] colums  = {"包名"};
        //get keys of map
        final String[] key = {getFirstKeyOfMap(map)};
        LOG.warn("firstkey: " + key[0]);
        final PsiClass[][] psiClasses = {(PsiClass[]) map.get(key[0])};
        LOG.warn("psiClasses: " + psiClasses[0].length);
        updateTableData(psiClasses[0],table);




        table.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e1) {
                if (e1.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e1)) {
                    int row = table.getSelectedRow();
                    LOG.warn("click row: " + row);
                    String packagename = psiClasses[0][row].getQualifiedName();
                    LOG.warn("click packagename: " + packagename);
                    addImportStatement(project, packagename, psiClasses[0][row], selectImportClassUI, map, key, row, e);
                    psiClasses[0] = (PsiClass[]) map.get(key[0]);
                    updateTableData(psiClasses[0],table);
                }
            }
        });

        selectImportClassUI.show();


    }


    public void addImportStatement(Project project, String tableName, PsiClass psiClasses, SelectImportClassUI selectImportClassUI, Map map, String[] key, int row, AnActionEvent e){
        WriteCommandAction.runWriteCommandAction(project,()->{
            PsiFile file = e.getData(LangDataKeys.PSI_FILE);
            //get Editor
            //Editor editor = e.getData(CommonDataKeys.EDITOR);
            //PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
            JavaCodeStyleManager.getInstance(project).addImport((PsiJavaFile) file, psiClasses);
        });
        removeElementOfMap(map, key[0]);

        key[0] = getFirstKeyOfMap(map);
        if(key[0]==null){
            //等待1秒
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e1) {
//                e1.printStackTrace();
//            }
            //selectImportClassUI 窗口不显示
            selectImportClassUI.close(0);
        }
    }




    public String getFirstKeyOfMap(Map<String,PsiClass[]> map){
        Set keySet = map.keySet();
        if(keySet==null || keySet.size()==0){
            return null;
        }
        return (String)keySet.toArray()[0];
    }

    //method remove element of map
    public void removeElementOfMap(Map<String,PsiClass[]> map,String key){
        map.remove(key);
    }

    //set table data
    public void updateTableData(PsiClass[] psiClasses,JTable table){
        if(psiClasses==null || psiClasses.length==0){
            return;
        }
        Vector vector = new Vector<>();
        //put psiClasses into vector
        for (int i = 0; i < psiClasses.length; i++) {
            Vector<String> row = new Vector<>();
            row.add(psiClasses[i].getQualifiedName());
            vector.add(row);
        }
        Vector<String> columns = new Vector<>();
        columns.add("包名");
        DefaultTableModel model = new DefaultTableModel(vector, columns);
        table.setModel(model);
        table.repaint();
    }


    public void removeHasImportedClass(Map<String, PsiClass[]> map, Map<String, String> hasImportedMap) {
        if (hasImportedMap == null || hasImportedMap.isEmpty()) {
            return;
        }

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String[] classNameArray = key.split("\\.");
            String shortClassName = classNameArray[classNameArray.length - 1];
            if (hasImportedMap.containsKey(shortClassName)) {
                iterator.remove(); // 使用迭代器的 remove() 方法删除元素
            }
        }
    }



    public void autoImportOnlyOneCandidates(Map<String,PsiClass[]> map, Project project, AnActionEvent e) {
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            PsiClass[] psiClasses = map.get(key);
            if (psiClasses.length == 1) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    PsiFile file = e.getData(LangDataKeys.PSI_FILE);
                    //get Editor
                    //Editor editor = e.getData(CommonDataKeys.EDITOR);
                    //PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
                    JavaCodeStyleManager.getInstance(project).addImport((PsiJavaFile) file, psiClasses[0]);
                });
                iterator.remove(); // 使用迭代器的 remove() 方法删除元素
            }
        }
    }


}
