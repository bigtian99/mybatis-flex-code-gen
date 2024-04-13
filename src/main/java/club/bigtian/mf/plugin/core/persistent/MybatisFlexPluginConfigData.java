package club.bigtian.mf.plugin.core.persistent;

import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.util.ProjectUtils;
import club.bigtian.mf.plugin.core.util.TableUtils;
import club.bigtian.mf.plugin.entity.MatchTypeMapping;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 持久化配置
 */
@Service
@State(
        name = "PluginSettings",
        storages = {
                @Storage("pluginSettings.xml")
        }
)
public final class MybatisFlexPluginConfigData implements PersistentStateComponent<MybatisFlexPluginConfigData.State> {

    private State myState = new State();

    public static MybatisFlexPluginConfigData getInstance() {
        ComponentManager componentManager = ApplicationManager.getApplication();
        return componentManager.getService(MybatisFlexPluginConfigData.class);
    }

    public static void clear() {
        clearCurrentProjectConfig();
    }

    public static void clearCode() {
        clearCurrentProjectConfigVmCode();
    }

    public static void clearSince() {
        Project project = ProjectUtils.getCurrentProject();
        Map<String, LinkedHashMap<String, MybatisFlexConfig>> projectMap = getProjectSinceMap();
        projectMap.remove(project.getName());
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.configSince = JSONObject.toJSONString(projectMap);
        instance.loadState(state);
    }

    public static MybatisFlexConfig getConfig(String key) {
        LinkedHashMap<String, MybatisFlexConfig> currentProjectSinceMap = getCurrentProjectSinceMap();
//        return currentProjectSinceMap.getOrDefault(key, new MybatisFlexConfig());
        return currentProjectSinceMap.get(key);
    }

    public static Map<String, MybatisFlexConfig> getSinceMap() {
        return getCurrentProjectSinceMap();
    }


    public static void removeSinceConfig(String key) {
        LinkedHashMap<String, MybatisFlexConfig> sinceMap = getCurrentProjectSinceMap();
        sinceMap.remove(key);
        setCurrentProjectSinceMap(sinceMap);
    }

    public static void configSince(String configName, MybatisFlexConfig config) {
        setCurrentProjectSinceMap(configName, config);
    }

    public static void export(String targetPath) {
        JSONObject data = new JSONObject();
        data.put("mybatisFlexConfig", getCurrentProjectMybatisFlexConfig());
        data.put("configSince", getCurrentProjectSinceMap());
        FileUtil.writeString(data.toJSONString(), new File(targetPath + File.separator + "MybatisFlexData.json"), "UTF-8");
        Messages.showDialog("导出成功，请到选择的目录查看", "提示", new String[]{"确定"}, -1, Messages.getInformationIcon());
    }

    public static void importConfig(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Messages.showDialog("文件不存在", "提示", new String[]{"确定"}, -1, Messages.getInformationIcon());
            return;
        }
        String content = FileUtil.readString(file, "UTF-8");
        JSONObject data = JSON.parseObject(content);
        MybatisFlexConfig config = JSONObject.parseObject(data.getString("mybatisFlexConfig"), new TypeReference<MybatisFlexConfig>() {
        });
        LinkedHashMap<String, MybatisFlexConfig> sinceMap = JSONObject.parseObject(data.getString("configSince"), new TypeReference<LinkedHashMap<String, MybatisFlexConfig>>() {
        });

        setCurrentMybatisFlexConfig(config);
        setCurrentProjectSinceMap(sinceMap);

        Messages.showDialog("导入成功", "提示", new String[]{"确定"}, -1, Messages.getInformationIcon());
    }


    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }


    public static class State {
        /**
         * 当前项目配置（项目隔离）
         */
        public String mybatisFlexConfig = "{}";
        /**
         * 生成配置（项目隔离）
         */
        public String configSince = "{}";
        /**
         * 列类型和字段类型映射（通用）
         */
        public String columnFieldMap = "{}";

        public String typeMappings = "{}";

    }

    /**
     * 得到类型映射
     *
     * @return {@code Map<String, String>}
     */

    public static Map<String, List<MatchTypeMapping>> getTypeMapping() {
        Map<String, List<MatchTypeMapping>> typeMappingMap = JSON.parseObject(getInstance().getState().typeMappings, new TypeReference<Map<String, List<MatchTypeMapping>>>() {
        });
        if (CollUtil.isEmpty(typeMappingMap)) {
            typeMappingMap = TableUtils.getDefaultTypeMappingMap();
        }
        return typeMappingMap;
    }

    /**
     * 设置类型映射
     *
     * @param typeMapping 类型映射
     */

    public static void setTypeMapping(Map<String, List<MatchTypeMapping>> typeMapping) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.typeMappings = JSONObject.toJSONString(typeMapping);
        instance.loadState(state);
    }

    /**
     * 获取字段类型
     *
     * @param columnType 列类型
     * @return {@code String}
     */
    public static String getFieldType(String columnType) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, String> colunmFieldMap = JSONObject.parseObject(state.columnFieldMap, new TypeReference<Map<String, String>>() {
        });
        return colunmFieldMap.get(columnType);
    }

    /**
     * 设置字段类型
     *
     * @param columnType    列类型
     * @param qualifiedName 限定名
     */
    public static void setFieldType(String columnType, String qualifiedName) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, String> colunmFieldMap = JSONObject.parseObject(state.columnFieldMap, new TypeReference<Map<String, String>>() {
        });
        colunmFieldMap.put(columnType.toLowerCase(), qualifiedName);
        state.columnFieldMap = JSONObject.toJSONString(colunmFieldMap);
        instance.loadState(state);
    }

    /**
     * 获取项目配置（全局）
     *
     * @return {@code Map<String, LinkedHashMap<String, MybatisFlexConfig>>}
     */// 工具方法
    @Nullable
    private static LinkedHashMap<String, LinkedHashMap<String, MybatisFlexConfig>> getProjectSinceMap() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        String configSince = state.configSince;
        LinkedHashMap<String, LinkedHashMap<String, MybatisFlexConfig>> projectMap = JSONObject.parseObject(configSince, new TypeReference<LinkedHashMap<String, LinkedHashMap<String, MybatisFlexConfig>>>() {
        });
        return projectMap;
    }

    /**
     * 设置项目版本配置
     *
     * @param map 地图
     */
    private static void setCurrentProjectSinceMap(LinkedHashMap<String, MybatisFlexConfig> map) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, LinkedHashMap<String, MybatisFlexConfig>> projectMap = getProjectSinceMap();
        projectMap.put(ProjectUtils.getCurrentProjectName(), map);
        state.configSince = JSONObject.toJSONString(projectMap);
        instance.loadState(state);
    }

    private static void setCurrentProjectSinceMap(String key, MybatisFlexConfig config) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, LinkedHashMap<String, MybatisFlexConfig>> projectMap = getProjectSinceMap();
        projectMap.computeIfAbsent(ProjectUtils.getCurrentProjectName(), k -> new LinkedHashMap<>()).put(key, config);
        state.configSince = JSONObject.toJSONString(projectMap);
        instance.loadState(state);
    }

    /**
     * 获取当前项目版本配置
     *
     * @return {@code LinkedHashMap<String, MybatisFlexConfig>}
     */
    @Nullable
    private static LinkedHashMap<String, MybatisFlexConfig> getCurrentProjectSinceMap() {
        Project project = ProjectUtils.getCurrentProject();
        Map<String, LinkedHashMap<String, MybatisFlexConfig>> projectSinceMap = getProjectSinceMap();
        return projectSinceMap.getOrDefault(project.getName(), new LinkedHashMap<>());
    }

    /**
     * 获取当前项目mybatis flex配置
     *
     * @return {@code MybatisFlexConfig}
     */
    public static MybatisFlexConfig getCurrentProjectMybatisFlexConfig() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, MybatisFlexConfig> flexConfigMap = JSONObject.parseObject(state.mybatisFlexConfig, new TypeReference<Map<String, MybatisFlexConfig>>() {
        });
        return flexConfigMap.getOrDefault(ProjectUtils.getCurrentProjectName(), new MybatisFlexConfig());
    }

    /**
     * 得到项目mybatis flex配置
     *
     * @return {@code Map<String, MybatisFlexConfig>}
     */
    public static Map<String, MybatisFlexConfig> getProjectMybatisFlexConfig() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        return JSONObject.parseObject(state.mybatisFlexConfig, new TypeReference<Map<String, MybatisFlexConfig>>() {
        });
    }

    /**
     * 清空当前项目配置
     */
    public static void clearCurrentProjectConfig() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, MybatisFlexConfig> flexConfigMap = JSONObject.parseObject(state.mybatisFlexConfig, new TypeReference<Map<String, MybatisFlexConfig>>() {
        });
        flexConfigMap.remove(ProjectUtils.getCurrentProjectName());
        state.mybatisFlexConfig = JSONObject.toJSONString(flexConfigMap);
        instance.loadState(state);
    }

    /**
     * 清空当前项目配置vm代码
     */
    public static void clearCurrentProjectConfigVmCode() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, MybatisFlexConfig> flexConfigMap = JSONObject.parseObject(state.mybatisFlexConfig, new TypeReference<Map<String, MybatisFlexConfig>>() {
        });
        MybatisFlexConfig config = flexConfigMap.get(ProjectUtils.getCurrentProjectName());
        config.setTabList(new ArrayList<>());
        state.mybatisFlexConfig = JSONObject.toJSONString(flexConfigMap);
        instance.loadState(state);
    }

    /**
     * 设置mybatis flex配置
     *
     * @param config 配置
     */
    public static void setCurrentMybatisFlexConfig(MybatisFlexConfig config) {
        Map<String, MybatisFlexConfig> configMap = getProjectMybatisFlexConfig();
        configMap.put(ProjectUtils.getCurrentProjectName(), config);
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.mybatisFlexConfig = JSONObject.toJSONString(configMap);
        instance.loadState(state);
    }
}
