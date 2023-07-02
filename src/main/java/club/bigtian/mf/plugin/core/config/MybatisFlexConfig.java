package club.bigtian.mf.plugin.core.config;


import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class MybatisFlexConfig {
    /**
     * 作者
     */
    private String author;
    /**
     * 版本
     */
    private String since;
    /**
     * 表前缀
     */
    private String tablePrefix;
    /**
     * 是否生成builder
     */
    private boolean builder;
    /**
     * 是否生成data
     */
    private boolean data;
    /**
     * 是否生成allArgsConstructor
     */
    private boolean allArgsConstructor;
    /**
     * 是否生成noArgsConstructor
     */
    private boolean noArgsConstructor;
    /**
     * 是否生成swagger
     */
    private boolean swagger;

    /**
     * 控制器模板
     */
    private String controllerTemplate;
    /**
     * 实体模板
     */
    private String modelTemplate;
    /**
     * service接口模板
     */
    private String interfaceTempalate;
    /**
     * service实现模板
     */
    private String implTemplate;
    /**
     * mapper模板
     */
    private String mapperTemplate;
    /**
     * xml模板
     */
    private String xmlTemplate;

    //=============包名

    /**
     * 控制器包名
     */
    private String controllerPackage;
    /**
     * 实体包名
     */
    private String modelPackage;
    /**
     * service接口包名
     */
    private String interfacePackage;
    /**
     * service实现包名
     */
    private String implPackage;
    /**
     * mapper包名
     */
    private String mapperPackage;
    /**
     * xml包名
     */
    private String xmlPackage;

    //=============文件路径

    /**
     * 控制器文件路径
     *
     * @return
     */

    private String controllerModule;
    /**
     * 实体文件路径
     *
     * @return
     */
    private String modelModule;
    /**
     * service接口文件路径
     *
     * @return
     */
    private String interfaceModule;

    /**
     * service实现文件路径
     *
     * @return
     */
    private String implModule;
    /**
     * mapper文件路径
     *
     * @return
     */
    private String mapperModule;
    /**
     * xml文件路径
     *
     * @return
     */
    private String xmlModule;

    private boolean sync;


    private String controllerSuffix;
    private String interfaceSuffix;
    private String implSuffix;
    private String modelSuffix;
    private String mapperSuffix;

    private String idType;

    private boolean cache;

    private boolean overrideCheckBox;


    public Map<String, String> getSuffix() {
        Map<String, String> data = new HashMap<>();
        data.put("Controller", ObjectUtil.defaultIfBlank(controllerSuffix, "Controller"));
        data.put("Entity", ObjectUtil.defaultIfBlank(modelSuffix, "Entity"));
        data.put("Service", ObjectUtil.defaultIfBlank(interfaceSuffix, "Service"));
        data.put("ServiceImpl", ObjectUtil.defaultIfBlank(implSuffix, "ServiceImpl"));
        data.put("Mapper", ObjectUtil.defaultIfBlank(mapperSuffix, "Mapper"));
        data.put("", ObjectUtil.defaultIfBlank(mapperSuffix, "Mapper"));
        return data;
    }

    public Map<String, String> getTemplates() {
        Map<String, String> data = new HashMap<>();
        data.put("Controller", controllerTemplate);
        data.put("Entity", modelTemplate);
        data.put("Service", interfaceTempalate);
        data.put("ServiceImpl", implTemplate);
        data.put("Mapper", mapperTemplate);
        data.put("", xmlTemplate);
        return data;
    }

    public Map<String, String> getPackages() {
        Map<String, String> data = new HashMap<>();
        data.put("Controller", controllerPackage);
        data.put("Entity", modelPackage);
        data.put("Service", interfacePackage);
        data.put("ServiceImpl", implPackage);
        data.put("Mapper", mapperPackage);
        data.put("", xmlPackage);
        return data;
    }

    public boolean isOverrideCheckBox() {
        return overrideCheckBox;
    }

    public void setOverrideCheckBox(boolean overrideCheckBox) {
        this.overrideCheckBox = overrideCheckBox;
    }

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getControllerSuffix() {
        return controllerSuffix;
    }

    public void setControllerSuffix(String controllerSuffix) {
        this.controllerSuffix = controllerSuffix;
    }

    public String getInterfaceSuffix() {
        return interfaceSuffix;
    }

    public void setInterfaceSuffix(String interfaceSuffix) {
        this.interfaceSuffix = interfaceSuffix;
    }

    public String getImplSuffix() {
        return implSuffix;
    }

    public void setImplSuffix(String implSuffix) {
        this.implSuffix = implSuffix;
    }

    public String getModelSuffix() {
        return modelSuffix;
    }

    public void setModelSuffix(String modelSuffix) {
        this.modelSuffix = modelSuffix;
    }

    public String getMapperSuffix() {
        return mapperSuffix;
    }

    public void setMapperSuffix(String mapperSuffix) {
        this.mapperSuffix = mapperSuffix;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public boolean isBuilder() {
        return builder;
    }

    public void setBuilder(boolean builder) {
        this.builder = builder;
    }

    public boolean isData() {
        return data;
    }

    public void setData(boolean data) {
        this.data = data;
    }

    public boolean isAllArgsConstructor() {
        return allArgsConstructor;
    }

    public void setAllArgsConstructor(boolean allArgsConstructor) {
        this.allArgsConstructor = allArgsConstructor;
    }

    public boolean isNoArgsConstructor() {
        return noArgsConstructor;
    }

    public void setNoArgsConstructor(boolean noArgsConstructor) {
        this.noArgsConstructor = noArgsConstructor;
    }

    public boolean isSwagger() {
        return swagger;
    }

    public void setSwagger(boolean swagger) {
        this.swagger = swagger;
    }

    public String getControllerTemplate() {
        return controllerTemplate;
    }

    public void setControllerTemplate(String controllerTemplate) {
        this.controllerTemplate = controllerTemplate;
    }

    public String getModelTemplate() {
        return modelTemplate;
    }

    public void setModelTemplate(String modelTemplate) {
        this.modelTemplate = modelTemplate;
    }

    public String getInterfaceTempalate() {
        return interfaceTempalate;
    }

    public void setInterfaceTempalate(String interfaceTempalate) {
        this.interfaceTempalate = interfaceTempalate;
    }

    public String getImplTemplate() {
        return implTemplate;
    }

    public void setImplTemplate(String implTemplate) {
        this.implTemplate = implTemplate;
    }

    public String getMapperTemplate() {
        return mapperTemplate;
    }

    public void setMapperTemplate(String mapperTemplate) {
        this.mapperTemplate = mapperTemplate;
    }

    public String getXmlTemplate() {
        return xmlTemplate;
    }

    public void setXmlTemplate(String xmlTemplate) {
        this.xmlTemplate = xmlTemplate;
    }

    public String getControllerPackage() {
        return controllerPackage;
    }

    public void setControllerPackage(String controllerPackage) {
        this.controllerPackage = controllerPackage;
    }

    public String getModelPackage() {
        return modelPackage;
    }

    public void setModelPackage(String modelPackage) {
        this.modelPackage = modelPackage;
    }

    public String getInterfacePackage() {
        return interfacePackage;
    }

    public void setInterfacePackage(String interfacePackage) {
        this.interfacePackage = interfacePackage;
    }

    public String getImplPackage() {
        return implPackage;
    }

    public void setImplPackage(String implPackage) {
        this.implPackage = implPackage;
    }

    public String getMapperPackage() {
        return mapperPackage;
    }

    public void setMapperPackage(String mapperPackage) {
        this.mapperPackage = mapperPackage;
    }

    public String getXmlPackage() {
        return xmlPackage;
    }

    public void setXmlPackage(String xmlPackage) {
        this.xmlPackage = xmlPackage;
    }

    public String getControllerModule() {
        return controllerModule;
    }

    public void setControllerModule(String controllerModule) {
        this.controllerModule = controllerModule;
    }

    public String getModelModule() {
        return modelModule;
    }

    public void setModelModule(String modelModule) {
        this.modelModule = modelModule;
    }

    public String getInterfaceModule() {
        return interfaceModule;
    }

    public void setInterfaceModule(String interfaceModule) {
        this.interfaceModule = interfaceModule;
    }

    public String getImplModule() {
        return implModule;
    }

    public void setImplModule(String implModule) {
        this.implModule = implModule;
    }

    public String getMapperModule() {
        return mapperModule;
    }

    public void setMapperModule(String mapperModule) {
        this.mapperModule = mapperModule;
    }

    public String getXmlModule() {
        return xmlModule;
    }

    public void setXmlModule(String xmlModule) {
        this.xmlModule = xmlModule;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public String toString() {
        return "MybatisFlexConfig{" +
                "author='" + author + '\'' +
                ", since='" + since + '\'' +
                ", tablePrefix='" + tablePrefix + '\'' +
                ", builder=" + builder +
                ", data=" + data +
                ", allArgsConstructor=" + allArgsConstructor +
                ", noArgsConstructor=" + noArgsConstructor +
                ", swagger=" + swagger +
                ", controllerTemplate='" + controllerTemplate + '\'' +
                ", modelTemplate='" + modelTemplate + '\'' +
                ", interfaceTempalate='" + interfaceTempalate + '\'' +
                ", implTemplate='" + implTemplate + '\'' +
                ", mapperTemplate='" + mapperTemplate + '\'' +
                ", xmlTemplate='" + xmlTemplate + '\'' +
                ", controllerPackage='" + controllerPackage + '\'' +
                ", modelPackage='" + modelPackage + '\'' +
                ", interfacePackage='" + interfacePackage + '\'' +
                ", implPackage='" + implPackage + '\'' +
                ", mapperPackage='" + mapperPackage + '\'' +
                ", xmlPackage='" + xmlPackage + '\'' +
                ", controllerModule='" + controllerModule + '\'' +
                ", modelModule='" + modelModule + '\'' +
                ", interfaceModule='" + interfaceModule + '\'' +
                ", implModule='" + implModule + '\'' +
                ", mapperModule='" + mapperModule + '\'' +
                ", xmlModule='" + xmlModule + '\'' +
                ", sync=" + sync +
                ", controllerSuffix='" + controllerSuffix + '\'' +
                ", interfaceSuffix='" + interfaceSuffix + '\'' +
                ", implSuffix='" + implSuffix + '\'' +
                ", modelSuffix='" + modelSuffix + '\'' +
                ", mapperSuffix='" + mapperSuffix + '\'' +
                ", idType='" + idType + '\'' +
                ", cache=" + cache +
                '}';
    }
}
