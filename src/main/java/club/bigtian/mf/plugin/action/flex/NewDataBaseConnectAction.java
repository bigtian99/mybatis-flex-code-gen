package club.bigtian.mf.plugin.action.flex;

import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.util.Modules;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.database.access.DatabaseCredentials;
import com.intellij.database.dataSource.*;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.view.ui.DataSourceManagerDialog;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.yaml.snakeyaml.Yaml;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 自动根据yml、.properties连接数据库
 */
public class NewDataBaseConnectAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        DbPsiFacade dbPsiFacade = DbPsiFacade.getInstance(e.getProject());

        InputEvent inputEvent = e.getInputEvent();
        if (inputEvent instanceof KeyEvent) {
            LocalDataSourceManager instance = LocalDataSourceManager.getInstance(e.getProject());
            List<String> existingDatabaseConnection = dbPsiFacade.getDataSources().stream()
                    .map(el -> el.getName())
                    .collect(Collectors.toList());
            if (Template.getCheckBoxConfig("databaseConfig")) {
                for (Module module : Modules.getModule(e.getProject())) {
                    LocalDataSource localDataSource = getLocalDataSource(module);
                    if (ObjectUtil.isNull(localDataSource) || existingDatabaseConnection.contains(localDataSource.getName())) {
                        continue;
                    }
                    existingDatabaseConnection.add(localDataSource.getName());


                    instance.addDataSource(localDataSource);
                }
            } else {
                //     获取选中的模块
                Module module = e.getData(LangDataKeys.MODULE);
                if (ObjectUtil.isEmpty(module)) {
                    Messages.showErrorDialog("请选中模块", "错误");
                    return;
                }
                LocalDataSource localDataSource = getLocalDataSource(module);
                if (ObjectUtil.isNull(localDataSource) || existingDatabaseConnection.contains(localDataSource.getName())) {
                    Messages.showErrorDialog("已存在该数据源", "错误");
                    return;
                }
                DataSourceManagerDialog.showDialog(e.getProject(), localDataSource, null);
            }
            return;
        }
        // 获取当前编辑的文件对应的模块
        VirtualFile projectFile = e.getData(CommonDataKeys.EDITOR).getVirtualFile();
        Module module = ModuleUtilCore.findModuleForFile(projectFile, e.getProject());
        LocalDataSource localDataSource = getLocalDataSource(module);
        DataSourceManagerDialog.showDialog(e.getProject(), localDataSource, null);

    }

    private @Nullable LocalDataSource getLocalDataSource(Module module) {
        Map<String, Object> databaseConfig = Map.of();
        PsiDirectory psiDirectory = Modules.getModuleDirectory(module, JavaModuleSourceRootTypes.RESOURCES);

        PsiFile file = psiDirectory.findFile("application.yml");

        if (ObjectUtil.isNull(file)) {
            file = psiDirectory.findFile("application.properties");
            if (ObjectUtil.isNotNull(file)) {
                Map<String, Object> propertiesConfig = getPropertiesConfig(file);
                String activeFile = MapUtil.getStr(propertiesConfig, "spring.profiles.active");
                if (StrUtil.isNotEmpty(activeFile)) {
                    file = psiDirectory.findFile("application-" + activeFile + ".properties");
                    propertiesConfig = getPropertiesConfig(file);
                }
                databaseConfig = getPropertiesDataConfig(propertiesConfig);
            }
        } else {
            Map<String, Object> ymlConfig = getYmlConfig(file);
            String activeFile = MapUtil.getStr(ymlConfig, "spring.profiles.active");
            if (StrUtil.isNotEmpty(activeFile)) {
                file = psiDirectory.findFile("application-" + activeFile + ".yml");
                ymlConfig = getYmlConfig(file);
            }
            databaseConfig = getYmlDatabaseConfig(ymlConfig);
        }
        if (ObjectUtil.isEmpty(databaseConfig)) {
            return null;
        }
        String url = databaseConfig.get("url").toString();
        String remoteUrl = ReUtil.get("//(.*?):", url, 1);
        String databaseName = StrUtil.subAfter(url, "/", true);
        if (databaseName.contains("?")) {
            databaseName = StrUtil.subBefore(databaseName, "?", false);
        }
        DatabaseCredentials databaseCredentials = DatabaseCredentials.getInstance();
        DatabaseDriverManager driverManager = DatabaseDriverManager.getInstance();
        Map<String, ? extends DatabaseDriver> driveMap = driverManager.getDrivers().stream()
                .collect(Collectors.toMap(el -> el.getName().toLowerCase(), Function.identity()));
        String databasetype = StrUtil.subBetween(url, "jdbc:", "://");

        DatabaseDriver driver = driveMap.get(databasetype);
        LocalDataSource localDataSource = LocalDataSource.create(databaseName + "@" + remoteUrl, driver.getDriverClass(), url, MapUtil.getStr(databaseConfig, "username"));
        localDataSource.setAutoSynchronize(true);
        databaseCredentials.storePassword(localDataSource, new OneTimeString(MapUtil.getStr(databaseConfig, "password")));
        localDataSource.setDatabaseDriver(driver);
        return localDataSource;
    }

    private Map<String, Object> getPropertiesDataConfig(Map<String, Object> propertiesConfig) {
        Map<String, Object> databaseMap = new HashMap<>();

        propertiesConfig.entrySet().stream().filter(el -> StrUtil.containsAny(el.getKey(), "username", "password", "url") && el.getKey().contains("datasource"))
                .forEach(el -> {
                    String key = el.getKey();
                    String value = el.getValue().toString();
                    key = StrUtil.subAfter(key, ".", true);
                    databaseMap.put(key, value);
                });
        return databaseMap;
    }

    private Map<String, Object> getPropertiesConfig(PsiFile file) {
        Map<String, Object> databaseMap = new HashMap<>();
        try {
            Files.lines(Paths.get(file.getVirtualFile().getPath())).filter(line -> !line.startsWith("#"))
                    .forEach(el -> {
                        String key;
                        String value = StrUtil.subAfter(el, "=", false);
                        key = StrUtil.subBefore(el, "=", false);
                        databaseMap.put(key, value);
                    });
        } catch (IOException e) {

        }
        return databaseMap;
    }

    private static Map<String, Object> getYmlDatabaseConfig(Map<String, Object> ymlConfig) {
        Map<String, Object> databaseMap = new HashMap<>();

        ymlConfig.entrySet().stream().filter(el -> StrUtil.containsAny(el.getKey(), "username", "password", "url") && !el.getKey().startsWith("#") && el.getKey().contains("datasource"))
                .forEach(el -> {
                    String value = el.getValue().toString();
                    String key = StrUtil.subAfter(el.getKey(), ".", true);
                    databaseMap.put(key, value);
                });
        return databaseMap;
    }

    private static Map<String, Object> getYmlConfig(PsiFile file) {
        try {
            Yaml yaml = new Yaml();
            return flattenMap(yaml.load(file.getVirtualFile().getInputStream()), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> flattenMap(Map<String, Object> source, String currentPath) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String newPath = currentPath != null ? currentPath + "." + entry.getKey() : entry.getKey();
            if (entry.getValue() instanceof Map) {
                result.putAll(flattenMap((Map<String, Object>) entry.getValue(), newPath));
            } else {
                result.put(newPath, entry.getValue());
            }
        }
        return result;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
