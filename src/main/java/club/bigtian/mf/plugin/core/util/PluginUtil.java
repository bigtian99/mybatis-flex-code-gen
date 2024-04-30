package club.bigtian.mf.plugin.core.util;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

public class PluginUtil {
    /**
     * 检查冲突插件是否已安装
     *
     * @param pluginId 插件 ID
     * @return 是否已安装
     */
    public static boolean isConflictPluginInstalled(String pluginId) {
        // 在这里，你需要替换为你想要检查的插件的 ID
        PluginId plugin = PluginId.getId(pluginId);
        return PluginManagerCore.isPluginInstalled(plugin);
    }
}
