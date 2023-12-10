package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.util.BasicFormatter;
import club.bigtian.mf.plugin.core.util.NotificationUtils;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;

import java.awt.datatransfer.StringSelection;

public class MybatisFlexAgentHyperlinkInfo implements HyperlinkInfo {
    private static final BasicFormatter FORMATTER = new BasicFormatter();

    private final Project project;
    private String sql;

    public MybatisFlexAgentHyperlinkInfo(Project project, String sql) {
        this.project = project;
        this.sql = sql;
    }

    @Override
    public void navigate(Project project) {
        // 在这里处理点击事件
        //复制到粘贴板

        CopyPasteManager.getInstance().setContents(new StringSelection(FORMATTER.format(sql)));
        NotificationUtils.notifySuccess("已复制到粘贴板", "复制成功");

        // openFile();
    }
    // public void openFile() {
    //     try {
    //         // 创建临时文件
    //         File tempFile = File.createTempFile("temp", ".sql");
    //         FileUtil.writeString(sql, tempFile, "UTF-8");
    //         // 获取 VirtualFile
    //         VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile);
    //         if (virtualFile != null) {
    //             // 打开文件
    //             FileEditorManager.getInstance(project).openFile(virtualFile, true);
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}