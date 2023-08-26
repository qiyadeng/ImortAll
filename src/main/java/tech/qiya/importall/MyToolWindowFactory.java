package tech.qiya.importall;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class MyToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        toolWindow.setToHideOnEmptyContent(true);

        class MyPanel extends SimpleToolWindowPanel {

            public MyPanel(boolean vertical) {
                super(vertical);

                DefaultActionGroup group = new DefaultActionGroup();
//                group.add(new MyDumbAwareAction("Login1"));
//                group.add(new MyDumbAwareAction("Login2"));
//                group.add(new MyDumbAwareAction("Login3"));

                ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ToolBar", group, false);
                setToolbar(toolbar.getComponent());
            }

        }

        // 添加一个页
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(new MyPanel(false), "First-DSY", false), 0);

    }
}
