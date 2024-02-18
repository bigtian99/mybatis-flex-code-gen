package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.util.BasicFormatter;
import club.bigtian.mf.plugin.core.util.NotificationUtils;
import com.intellij.database.editor.DatabaseEditorHelper;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.util.DasUtil;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.JBIterable;

import java.awt.datatransfer.StringSelection;
import java.util.Collection;

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
        CopyPasteManager.getInstance().setContents(new StringSelection(FORMATTER.format(sql)));
        NotificationUtils.notifySuccess("已复制到粘贴板", "复制成功");
        DbPsiFacade dbPsiFacade = DbPsiFacade.getInstance(project);
        Collection<DbDataSource> dataSources = dbPsiFacade.getDataSources();
        for (DbDataSource dataSource : dataSources) {
            // 获取所有的表
            JBIterable<? extends DasTable> tables = DasUtil.getTables(dataSource);
            for (DasTable table : tables) {
                // 打印表的名称
                if (sql.contains(table.getName())) {
                    DatabaseEditorHelper.openConsoleFile(dataSource, true);
                    // // 获取当前打开的文件
                    VirtualFile file = FileEditorManager.getInstance(project).getSelectedFiles()[0];
                    // 获取文件的 Document
                    Document document = FileDocumentManager.getInstance().getDocument(file);
                    if (document != null) {
                        // 在文件的末尾插入 SQL
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            document.insertString(document.getTextLength(),  "\n" + FORMATTER.format(sql + ";\n"));
                        });
                    }
                    return;
                }
            }
        }

    }


}