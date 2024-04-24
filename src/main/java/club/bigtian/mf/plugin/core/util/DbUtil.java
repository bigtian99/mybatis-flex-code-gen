package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.action.flex.ExecuteSqlAction;
import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.intellij.database.console.JdbcConsole;
import com.intellij.database.console.JdbcConsoleProvider;
import com.intellij.database.editor.DatabaseEditorHelper;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.util.DasUtil;
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
import java.util.function.Consumer;

public class DbUtil {
    private static final BasicFormatter FORMATTER = new BasicFormatter();

    public static void openDbConsole(String sql, Consumer consumer) {
        Project project = ProjectUtils.getCurrentProject();
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        DbPsiFacade dbPsiFacade = DbPsiFacade.getInstance(project);
        Collection<DbDataSource> dataSources = dbPsiFacade.getDataSources();
        if (CollUtil.isEmpty(dataSources)) {
            CopyPasteManager.getInstance().setContents(new StringSelection(FORMATTER.format(sql)));
            NotificationUtils.notifySuccess("🎉SQL copied to clipboard.🎉");
            return;
        }
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
                    if (config.isExecuteSql()) {
                        JdbcConsole console = JdbcConsoleProvider.getValidConsole(project, file);
                        new ExecuteSqlAction().actionPerformed(console, sql);
                    }
                    if (document != null) {
                        // 在文件的末尾插入 SQL
                        WriteCommandAction.runWriteCommandAction(project, () -> {
                            document.insertString(document.getTextLength(), FORMATTER.format(sql));

                        });

                    }
                   if(ObjectUtil.isNotNull(consumer)){
                       consumer.accept(file);
                   }
                    return;
                }
            }
        }
    }
}
