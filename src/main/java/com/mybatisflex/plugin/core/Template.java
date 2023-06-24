package com.mybatisflex.plugin.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.mybatisflex.plugin.core.constant.MybatisFlexConstant;
import com.mybatisflex.plugin.core.persistent.MybatisFlexPluginConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Template {
    private static Set<String> set = new HashSet<>();

    /**
     * 得到控制器vm代码
     *
     * @return {@code String}
     */
    public static String getVmCode(String template) {
        String code = getConfigData(template);
        if (StrUtil.isBlank(code)) {
            code = getTemplateContent(template);
        }
        return code;
    }

    @Nullable
    public static JSONObject getMybatisFlexConfig() {
        MybatisFlexPluginConfigData instance = MybatisFlexPluginConfigData.getInstance();
        MybatisFlexPluginConfigData.State state = instance.getState();
        JSONObject config = JSONObject.parse(ObjectUtil.defaultIfNull(state.mybatisFlexConfig, "{}"));
        return config;
    }

    /**
     * 得到模板内容
     *
     * @param templateName 模板名称
     * @return {@code String}
     */
    @NotNull
    private static String getTemplateContent(String templateName) {
        URL resource = Template.class.getResource(StrUtil.format("/templates/{}.vm", templateName));
        String templateContent = null;
        try {
            templateContent = StringUtil.convertLineSeparators(UrlUtil.loadText(resource));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        addMd5(templateContent + templateName);
        return templateContent;
    }

    private static void addMd5(String templateContent) {
        String md5 = SecureUtil.md5(templateContent);
        set.add(md5);
    }

    /**
     * 包含
     *
     * @param code md5
     * @return boolean
     */
    public static boolean contains(String code) {
        return set.contains(SecureUtil.md5(code));
    }

    public static String getConfigData(String property) {
        JSONObject config = getMybatisFlexConfig();
        String value = ObjectUtil.defaultIfNull(config.getString(property), "");
        addMd5(value + property);
        return value;
    }

    public static String getTablePrefix() {
        return getConfigData(MybatisFlexConstant.TABLE_PREFIX);
    }

    public static void clear() {
        set.clear();
    }
}
