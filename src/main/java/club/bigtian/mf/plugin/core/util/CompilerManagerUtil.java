package club.bigtian.mf.plugin.core.util;

import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

public class CompilerManagerUtil {
    private static final Logger LOG = Logger.getInstance(CompilerManagerUtil.class);

    /**
     * 编译
     *
     * @param files        文件
     * @param notification 通知
     */
    public static void compile(VirtualFile[] files, CompileStatusNotification notification) {
        CompilerManager compilerManager = getCompilerManager();
        compilerManager.compile(files, notification);
        LOG.error("编译完成");
    }

    /**
     * 重新编译
     */
    public static void rebuild() {
        CompilerManager compilerManager = getCompilerManager();
        compilerManager.rebuild(null);
        LOG.error("编译完成");
    }

    private static CompilerManager getCompilerManager() {
        CompilerManager compilerManager = CompilerManager.getInstance(ProjectUtils.getCurrentProject());
        return compilerManager;
    }

    /**
     * 单独编译某个模块
     *
     * @param module 模块
     */
    public static void make(Module module) {
        CompilerManager compilerManager = CompilerManager.getInstance(ProjectUtils.getCurrentProject());
        compilerManager.make(module, null);
    }
}
