package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class FileChooserUtil {
    /**
     * 选择目录
     *
     * @param project 项目
     * @return {@code String}
     */
    public static String chooseDirectory(Project project) {
        FileChooserDescriptor descriptor =
                new FileChooserDescriptor(false, true, false, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
        if (ObjectUtil.isNull(virtualFile)) {
            return null;
        }
        return virtualFile.getPath();
    }

    /**
     * 选择文件
     *
     * @param project 项目
     * @return {@code String}
     */
    public static String chooseFile(Project project) {
        FileChooserDescriptor descriptor =
                new FileChooserDescriptor(true, false, false, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
        if (ObjectUtil.isNull(virtualFile)) {
            return null;
        }
        return virtualFile.getPath();
    }

    /**
     * 选择文件
     *
     * @param project 项目
     * @return {@code VirtualFile}
     */

    public static VirtualFile chooseFileVirtual(Project project) {
        FileChooserDescriptor descriptor =
                new FileChooserDescriptor(true, false, false, false, false, false);
        return FileChooser.chooseFile(descriptor, project, null);
    }
}
