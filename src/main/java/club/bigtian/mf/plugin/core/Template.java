package club.bigtian.mf.plugin.core;

import club.bigtian.mf.plugin.core.config.MybatisFlexConfig;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.core.persistent.MybatisFlexPluginConfigData;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Template {
    private static Set<String> set = new HashSet<>();
    private static MybatisFlexConfig config;

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

    public static void clearConfig() {
        config = null;
    }

    public static @NotNull MybatisFlexConfig getMybatisFlexConfig() {
        if (ObjectUtil.isNull(config)) {
            config = MybatisFlexPluginConfigData.getCurrentProjectMybatisFlexConfig();
        }

        if (ObjectUtil.isNull(config.getControllerSuffix())) {
            config.setControllerSuffix(MybatisFlexConstant.CONTROLLER);
        }
        if (ObjectUtil.isNull(config.getInterfaceSuffix())) {
            config.setInterfaceSuffix(MybatisFlexConstant.SERVICE);
        }
        if (ObjectUtil.isNull(config.getImplSuffix())) {
            config.setImplSuffix(MybatisFlexConstant.SERVICE_IMPL);
        }
        if (ObjectUtil.isNull(config.getModelSuffix())) {
            config.setModelSuffix(MybatisFlexConstant.ENTITY);
        }
        if (ObjectUtil.isNull(config.getMapperSuffix())) {
            config.setMapperSuffix(MybatisFlexConstant.MAPPER);
        }

        if (ObjectUtil.isNull(config.getContrPath())) {
            config.setContrPath(MybatisFlexConstant.CONTROLLER.toLowerCase());
        }
        if (ObjectUtil.isNull(config.getDomainPath())) {
            config.setDomainPath(MybatisFlexConstant.DOMAIN.toLowerCase());
        }
        if (ObjectUtil.isNull(config.getImplPath())) {
            config.setImplPath(MybatisFlexConstant.IMPL.toLowerCase());
        }
        if (ObjectUtil.isNull(config.getServicePath())) {
            config.setServicePath(MybatisFlexConstant.SERVICE.toLowerCase());
        }
        if (ObjectUtil.isNull(config.getMapperPath())) {
            config.setMapperPath(MybatisFlexConstant.MAPPER.toLowerCase());
        }
        if (ObjectUtil.isNull(config.getXmlPath())) {
            config.setXmlPath(MybatisFlexConstant.MAPPERS.toLowerCase());
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
    public static String getTemplateContent(String templateName) {
        URL resource = Template.class.getResource(StrUtil.format("/templates/{}.vm", templateName));
        String templateContent = null;
        try {
            templateContent = StringUtil.convertLineSeparators(UrlUtil.loadText(resource));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return templateContent;
    }


    public static String getConfigData(String property) {
        return getConfigData(property, "");
    }

    public static String getConfigData(String property, String defaultValue) {
        MybatisFlexConfig config = getMybatisFlexConfig();
        Object fieldValue = ReflectUtil.getFieldValue(config, property);
        return ObjectUtil.defaultIfNull(fieldValue, defaultValue).toString();
    }

    public static String getSuffix(String property) {
        return getConfigData(property);
    }

    public static String getSuffix(String property, String defaultValue) {
        String data = getConfigData(property);
        return ObjectUtil.isNull(data) ? defaultValue : data;
    }

    public static boolean getCheckBoxConfig(String property) {
        MybatisFlexConfig config = getMybatisFlexConfig();
        Object fieldValue = ReflectUtil.getFieldValue(config, property);
        return (boolean) ObjectUtil.defaultIfNull(fieldValue, false);
    }

    public static Boolean getCheckBoxConfig(String property, boolean defaultValue) {
        MybatisFlexConfig config = getMybatisFlexConfig();
        Object fieldValue = ReflectUtil.getFieldValue(config, property);
        return (boolean) ObjectUtil.defaultIfNull(fieldValue, defaultValue);
    }

    public static <T> T getData(String property, Class<T> clazz) {
        MybatisFlexConfig config = getMybatisFlexConfig();
        Object fieldValue = ReflectUtil.getFieldValue(config, property);
        T defaultValue = null;
        try {
            defaultValue = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) ObjectUtil.defaultIfNull(fieldValue, defaultValue);

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
