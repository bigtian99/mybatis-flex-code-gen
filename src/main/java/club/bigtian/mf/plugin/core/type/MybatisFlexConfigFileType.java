package club.bigtian.mf.plugin.core.type;

import club.bigtian.mf.plugin.core.icons.Icons;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * mybatis-flex.config 文件图标
 *
 * @author bigtian
 */
public class MybatisFlexConfigFileType implements FileIconProvider {


    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (file.getName().equals("mybatis-flex.config")) {
            return Icons.FLEX;
        }
        // 返回 null 时，使用默认图标
        return null;
    }
}
