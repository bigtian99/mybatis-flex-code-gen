package com.mybatisflex.plugin.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.intellij.codeInspection.reference.RefUtil;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.mybatisflex.plugin.core.config.MybatisFlexConfig;
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
        String code = getConfigData(template.split("\\.")[0]);
        if (StrUtil.isBlank(code)) {
            code = getTemplateContent(template);
        }
        return code;
    }

    @Nullable
    public static MybatisFlexConfig getMybatisFlexConfig() {
        MybatisFlexPluginConfigData instance = MybatisFlexPluginConfigData.getInstance();
        MybatisFlexPluginConfigData.State state = instance.getState();
        String mybatisFlexConfig = ObjectUtil.defaultIfNull(state.mybatisFlexConfig, "{}");
        MybatisFlexConfig config = JSONObject.parseObject(mybatisFlexConfig, new TypeReference<>() {
        });
        if (StrUtil.isEmpty(config.getControllerTemplate())) {
            config.setControllerTemplate(getTemplateContent(MybatisFlexConstant.CONTROLLER_TEMPLATE));
            config.setModelTemplate(getTemplateContent(MybatisFlexConstant.MODEL_TEMPLATE));
            config.setInterfaceTempalate(getTemplateContent(MybatisFlexConstant.INTERFACE_TEMPLATE));
            config.setImplTemplate(getTemplateContent(MybatisFlexConstant.IMPL_TEMPLATE));
            config.setMapperTemplate(getTemplateContent(MybatisFlexConstant.MAPPER_TEMPLATE));
            config.setXmlTemplate(getTemplateContent(MybatisFlexConstant.XML_TEMPLATE));
        }
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
        MybatisFlexConfig config = getMybatisFlexConfig();
        Object fieldValue = ReflectUtil.getFieldValue(config, property);
        String value = ObjectUtil.defaultIfNull(fieldValue, "").toString();
        addMd5(value + property);
        return value;
    }

    public static String getSuffix(String property, String val) {
        String fieldValue = getConfigData(property);
        String value = ObjectUtil.defaultIfBlank(fieldValue, val).toString();
        addMd5(value + property);
        return value;
    }

    public static boolean getChecBoxConfig(String property) {
        MybatisFlexConfig config = getMybatisFlexConfig();
        Object fieldValue = ReflectUtil.getFieldValue(config, property);
        boolean value = (boolean) ObjectUtil.defaultIfNull(fieldValue, false);
        addMd5(value + property);
        return value;
    }

    public static String getTablePrefix() {
        return getConfigData(MybatisFlexConstant.TABLE_PREFIX);
    }

    public static String getSince() {
        return getConfigData(MybatisFlexConstant.SINCE);
    }

    public static String getAuthor() {
        return getConfigData(MybatisFlexConstant.AUTHOR);
    }

    public static void clear() {
        set.clear();
    }
}
