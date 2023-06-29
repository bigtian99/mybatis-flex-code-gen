package com.mybatisflex.plugin.core.persistent;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.hierynomus.sshj.userauth.keyprovider.OpenSSHKeyFileUtil;
import com.intellij.openapi.components.*;
import com.mybatisflex.plugin.core.Template;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
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
        state.configSince.clear();
        instance.loadState(state);

    }

    public static void clearMap() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.configSince.clear();
        instance.loadState(state);
    }

    public static MybatisFlexConfig getConfig(String key) {
        Map<String, MybatisFlexConfig> sinceMap = getSinceMap();
        return sinceMap.get(key);
    }

    public static Map<String, MybatisFlexConfig> getSinceMap() {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        return state.configSince;
    }

    public static void removeSinceConfig(String key) {
        Map<String, MybatisFlexConfig> sinceMap = getSinceMap();
        sinceMap.remove(key);
        configSince(sinceMap);
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
        public Map<String, MybatisFlexConfig> configSince = new HashMap<>();

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

    public static void configSince(Map<String, MybatisFlexConfig> config) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        state.configSince.putAll(config);
        instance.loadState(state);
    }

    public static JSONObject getConfigData(String key) {
        MybatisFlexPluginConfigData instance = getInstance();
        State state = instance.getState();
        Field field = ReflectUtil.getField(state.getClass(), key);
        if (field != null) {
            String oldVal = ObjectUtil.defaultIfNull(ReflectUtil.getFieldValue(state, key), "{}").toString();
            return JSONObject.parse(oldVal);

        }
        return new JSONObject();
    }

}
