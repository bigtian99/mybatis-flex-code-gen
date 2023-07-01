package com.mybatisflex.plugin.core.persistent;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.mybatisflex.plugin.core.Template;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
import com.mybatisflex.plugin.core.util.VirtualFileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        return ServiceManager.getService(MybatisFlexPluginConfigData.class);
    }

    public static void clear() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.mybatisFlexConfig = "{}";
        instance.loadState(state);
    }

    public static void clearCode() {
        MybatisFlexConfig config = Template.getMybatisFlexConfig();
        config.setModelTemplate("");
        config.setControllerTemplate("");
        config.setImplTemplate("");
        config.setMapperTemplate("");
        config.setInterfaceTempalate("");
        config.setXmlTemplate("");
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.mybatisFlexConfig = JSONObject.toJSONString(config);
        instance.loadState(state);

    }

    public static void clearSince() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.configSince = "{}";
        instance.loadState(state);
    }

    public static MybatisFlexConfig getConfig(String key) {
        Map<String, MybatisFlexConfig> sinceMap = getSinceMap();
        return sinceMap.get(key);
    }

    public static Map<String, MybatisFlexConfig> getSinceMap() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        String configSince = state.configSince;
        return JSONObject.parseObject(configSince, new TypeReference<LinkedHashMap<String, MybatisFlexConfig>>() {
        });
    }

    public static void removeSinceConfig(String key) {
        Map<String, MybatisFlexConfig> sinceMap = getSinceMap();
        sinceMap.remove(key);
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.configSince = JSONObject.toJSONString(sinceMap);
        instance.loadState(state);
    }

    public static void configSince(Map<String, MybatisFlexConfig> sinceMap) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Map<String, MybatisFlexConfig> configMap = getSinceMap();
        configMap.putAll(sinceMap);
        state.configSince = JSONObject.toJSONString(configMap);
        instance.loadState(state);
    }

    public static void export(String targetPath) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        String mybatisFlexConfig = state.mybatisFlexConfig;
        String configSince = state.configSince;
        JSONObject data = new JSONObject();
        data.put("mybatisFlexConfig", mybatisFlexConfig);
        data.put("configSince", configSince);
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
        String mybatisFlexConfig = data.getString("mybatisFlexConfig");
        String configSince = data.getString("configSince");
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.mybatisFlexConfig = mybatisFlexConfig;
        state.configSince = configSince;
        instance.loadState(state);
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
        public String mybatisFlexConfig = "{}";
        public String configSince = "{}";

    }

    public static void setData(String key, String value) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Field field = ReflectUtil.getField(state.getClass(), key);
        if (field != null) {
            String oldVal = ObjectUtil.defaultIfNull(ReflectUtil.getFieldValue(state, key), "{}").toString();
            JSONObject parse = JSONObject.parse(oldVal);
            parse.putAll(JSONObject.parse(value));
            ReflectUtil.setFieldValue(state, key, parse.toJSONString());
        }
    }
}
