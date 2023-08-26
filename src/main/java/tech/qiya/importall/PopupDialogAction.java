package tech.qiya.importall;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class PopupDialogAction extends AnAction {


    @Override
    public void update(@NotNull AnActionEvent event) {
        // Using the event, evaluate the context,
        // and enable or disable the action.
        // Set the availability based on whether a project is open
        Project currentProject = event.getProject();
        event.getPresentation().setEnabledAndVisible(currentProject != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Using the event, create and show a dialog
        Project currentProject = event.getProject();
        StringBuilder message =
                new StringBuilder(event.getPresentation().getText() + " Selected!");
        // If an element is selected in the editor, add info about it.
        Navigatable selectedElement = event.getData(CommonDataKeys.NAVIGATABLE);
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        String classPath = psiFile.getVirtualFile().getPath();
        if (classPath != null) {
            message.append("\nSelected Element: ").append(classPath);
        }
        String title = event.getPresentation().getDescription();
        Messages.showMessageDialog(
                currentProject,
                message.toString(),
                title,
                Messages.getInformationIcon());
    }
}
