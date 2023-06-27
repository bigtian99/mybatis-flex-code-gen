package com.mybatisflex.plugin.core.persistent;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

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
