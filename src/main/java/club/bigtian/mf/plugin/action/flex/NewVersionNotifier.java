package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.util.NotificationUtils;
import club.bigtian.mf.plugin.entity.PluginInfo;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;

import java.util.List;

public class NewVersionNotifier  {

    private static final String PLUGIN_ID = "com.mybatisflex.bigtian"; // 替换为你的插件ID



    public static void checkForNewVersion() {
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID));
        if (pluginDescriptor == null) {
            return;
        }

        String currentVersion = pluginDescriptor.getVersion();

        // 获取插件市场上的最新版本号
        String latestVersion = getLatestPluginVersionFromMarket();

        // 比较版本号
        if (compareVersions(latestVersion, currentVersion) > 0) {
            // 显示更新提示
            showUpdateNotification();
        }
    }

    private static String getLatestPluginVersionFromMarket() {
        List<PluginInfo> pluginInfos = JSON.parseObject(HttpUtil.get("https://plugins.jetbrains.com/api/plugins/22165/updates?channel=&size=1"), new TypeReference<List<PluginInfo>>() {
        });
        return pluginInfos.get(0).getVersion();
    }


    private static void showUpdateNotification() {
        NotificationUtils.start();
    }

    // 辅助方法：比较版本号
    private static int compareVersions(String version1, String version2) {
        // 在实际应用中可能需要根据版本号格式进行更复杂的比较
        return version1.compareTo(version2);
    }

}
