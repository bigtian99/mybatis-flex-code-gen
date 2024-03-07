package club.bigtian.mf.plugin.core.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GptToolWindowFactory  implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel jPanel = new JPanel(new BorderLayout());

        JBCefClient client = JBCefApp.getInstance().createClient();
        JBCefBrowser browser = new JBCefBrowser(client, "http://chat.bigtian.club");
        jPanel.add(browser.getComponent(), BorderLayout.CENTER);
        Content content = ContentFactory.getInstance().createContent(jPanel, "", false);

        toolWindow.getComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                jPanel.setPreferredSize(new Dimension(toolWindow.getComponent().getWidth(), toolWindow.getComponent().getHeight()));
                jPanel.revalidate();
            }
        });

        toolWindow.getContentManager().addContent(content);
    }


}
