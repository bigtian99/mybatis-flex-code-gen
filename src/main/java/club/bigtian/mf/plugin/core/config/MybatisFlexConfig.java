package club.bigtian.mf.plugin.core.config;


import club.bigtian.mf.plugin.core.Template;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.entity.TabInfo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import java.util.*;
import java.util.stream.Collectors;

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

    private boolean swagger3;



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
    private String interfacePre;
    private String implSuffix;
    private String modelSuffix;
    private String mapperSuffix;

    private String idType;

    private boolean cache;

    private boolean overrideCheckBox;

    /**
     * 生成结果类型：new/static
     */
    private String resultType;

    /**
     * 统一返回对象
     */
    private String qualifiedName;

    /**
     * 方法
     */
    private String methodName;

    /**
     * 是否泛型
     */
    private boolean genericity;

    /**
     * 逻辑删除字段
     */
    private String logicDeleteField;


    private String contrPath;
    private String servicePath;
    private String implPath;
    private String domainPath;
    private String xmlPath;
    private String mapperPath;

    private boolean accessors;

    /**
     * ar模式
     */
    private boolean activeRecord;
    private boolean requiredArgsConstructor;

    /**
     * 租户
     */
    private String tenant;

    /**
     * 乐观锁
     */
    private String version;

    private String modelSuperClass;

    private String dataSource;
    private String insertValue;
    private String updateValue;

    private String onInsert;
    private String onUpdate;
    private String onSet;

    private Boolean fromCheck;

    /**
     * sql 方言
     */
    private String sqlDialect;

    /**
     * mapper xml 生成类型（java/resource）
     */
    private String mapperXmlType;

    private String tabList;

    private Boolean enableDebug;

    private boolean ktFile;

    private String remoteDataUrl;
    private String remoteHeader;
    private String remoteDataToken;
    private String resultField;
    private boolean databaseConfig;
    private boolean isExecuteSql;

    private boolean remoteInterface;
    private boolean navigationMapper;

    private String preparing;
    private String parameters;

    public boolean isNavigationMapper() {
        return navigationMapper;
    }

    public void setNavigationMapper(boolean navigationMapper) {
        this.navigationMapper = navigationMapper;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getPreparing() {
        return preparing;
    }

    public void setPreparing(String preparing) {
        this.preparing = preparing;
    }

    public boolean isExecuteSql() {
        return isExecuteSql;
    }

    public void setExecuteSql(boolean executeSql) {
        isExecuteSql = executeSql;
    }

    public boolean isDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(boolean databaseConfig) {
        this.databaseConfig = databaseConfig;
    }


    public boolean isRemoteInterface() {
        return remoteInterface;
    }

    public void setRemoteInterface(boolean remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    public String getResultField() {
        return resultField;
    }

    public void setResultField(String resultField) {
        this.resultField = resultField;
    }

    public Boolean getEnableDebug() {
        return enableDebug;
    }

    public String getRemoteDataToken() {
        return remoteDataToken;
    }

    public void setRemoteDataToken(String remoteDataToken) {
        this.remoteDataToken = remoteDataToken;
    }

    public String getRemoteDataUrl() {
        return remoteDataUrl;
    }

    public void setRemoteDataUrl(String remoteDataUrl) {
        this.remoteDataUrl = remoteDataUrl;
    }

    public String getRemoteHeader() {
        return remoteHeader;
    }

    public void setRemoteHeader(String remoteHeader) {
        this.remoteHeader = remoteHeader;
    }

    public void setTabList(String tabList) {
        this.tabList = tabList;
    }

    public boolean isKtFile() {
        return ktFile;
    }

    public void setKtFile(boolean ktFile) {
        this.ktFile = ktFile;
    }

    public Boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(Boolean enableDebug) {
        this.enableDebug = enableDebug;
    }


    public List<TabInfo> getTabList() {
        List<TabInfo> infoList = Optional.ofNullable(JSON.parseArray(tabList, TabInfo.class)).orElse(new ArrayList<>());
        if (CollUtil.isEmpty(infoList) || infoList.size() < 6) {
            infoList.add(0, new TabInfo(MybatisFlexConstant.CONTROLLER, Template.getVmCode(MybatisFlexConstant.CONTROLLER_TEMPLATE), ".java", 0));
            infoList.add(1, new TabInfo(MybatisFlexConstant.SERVICE, Template.getVmCode(MybatisFlexConstant.INTERFACE_TEMPLATE), ".java", 1));
            infoList.add(2, new TabInfo(MybatisFlexConstant.SERVICE_IMPL, Template.getVmCode(MybatisFlexConstant.IMPL_TEMPLATE), ".java", 2));
            infoList.add(3, new TabInfo(MybatisFlexConstant.ENTITY, Template.getVmCode(MybatisFlexConstant.MODEL_TEMPLATE), ".java", 3));
            infoList.add(4, new TabInfo(MybatisFlexConstant.MAPPER, Template.getVmCode(MybatisFlexConstant.MAPPER_TEMPLATE), ".java", 4));
            infoList.add(5, new TabInfo(MybatisFlexConstant.XML, Template.getVmCode(MybatisFlexConstant.XML_TEMPLATE), ".xml", 5));
        }
        return infoList;
    }


    public void setTabList(List<TabInfo> tabList) {
        this.tabList = JSON.toJSONString(tabList);
    }

    public Boolean isFromCheck() {
        return fromCheck;
    }

    public void setFromCheck(Boolean fromCheck) {
        this.fromCheck = fromCheck;
    }

    public String getOnInsert() {
        return onInsert;
    }

    public void setOnInsert(String onInsert) {
        this.onInsert = onInsert;
    }

    public String getOnUpdate() {
        return onUpdate;
    }

    public void setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
    }

    public String getOnSet() {
        return onSet;
    }

    public void setOnSet(String onSet) {
        this.onSet = onSet;
    }

    public String getInsertValue() {
        return insertValue;
    }

    public void setInsertValue(String insertValue) {
        this.insertValue = insertValue;
    }

    public String getUpdateValue() {
        return updateValue;
    }

    public void setUpdateValue(String updateValue) {
        this.updateValue = updateValue;
    }

    public String getModelSuperClass() {
        return modelSuperClass;
    }

    public void setModelSuperClass(String modelSuperClass) {
        this.modelSuperClass = modelSuperClass;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    @JSONField(serialize = false)
    public Map<String, String> getSuffix() {
        Map<String, String> data = new HashMap<>();

        data.put(MybatisFlexConstant.CONTROLLER, ObjectUtil.defaultIfBlank(controllerSuffix, ""));
        data.put(MybatisFlexConstant.ENTITY, ObjectUtil.defaultIfBlank(modelSuffix, ""));
        data.put(MybatisFlexConstant.SERVICE, ObjectUtil.defaultIfBlank(interfaceSuffix, ""));
        data.put(MybatisFlexConstant.SERVICE_IMPL, ObjectUtil.defaultIfBlank(implSuffix, ""));
        data.put(MybatisFlexConstant.MAPPER, ObjectUtil.defaultIfBlank(mapperSuffix, ""));
        data.put(MybatisFlexConstant.XML, ObjectUtil.defaultIfBlank(mapperSuffix, ""));
        return data;
    }

    @JSONField(serialize = false)
    public Map<String, String> getTemplates() {
        Map<String, String> templateMap = getTabList()
                .stream()
                .collect(Collectors.toMap(TabInfo::getTitle, TabInfo::getContent));
        return templateMap;
    }

    public void setTemplates(Object obj) {

    }

    @JSONField(serialize = false)
    public Map<String, String> getPackages() {
        Map<String, String> data = new HashMap<>();
        data.put(MybatisFlexConstant.CONTROLLER, controllerPackage);
        data.put(MybatisFlexConstant.ENTITY, modelPackage);
        data.put(MybatisFlexConstant.SERVICE, interfacePackage);
        data.put(MybatisFlexConstant.SERVICE_IMPL, implPackage);
        data.put(MybatisFlexConstant.MAPPER, mapperPackage);
        data.put(MybatisFlexConstant.XML, xmlPackage);
        return data;
    }

    @JSONField(serialize = false)
    public Map<String, String> getModules() {
        Map<String, String> data = new HashMap<>();
        data.put(MybatisFlexConstant.CONTROLLER, controllerModule);
        data.put(MybatisFlexConstant.ENTITY, modelModule);
        data.put(MybatisFlexConstant.SERVICE, interfaceModule);
        data.put(MybatisFlexConstant.SERVICE_IMPL, implModule);
        data.put(MybatisFlexConstant.MAPPER, mapperModule);
        data.put(MybatisFlexConstant.XML, xmlModule);
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

    public boolean isSwagger3() {
        return swagger3;
    }

    public void setSwagger3(boolean swagger3) {
        this.swagger3 = swagger3;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isGenericity() {
        return genericity;
    }

    public void setGenericity(boolean genericity) {
        this.genericity = genericity;
    }

    public String getLogicDeleteField() {
        return logicDeleteField;
    }

    public void setLogicDeleteField(String logicDeleteField) {
        this.logicDeleteField = logicDeleteField;
    }

    public String getContrPath() {
        return contrPath;
    }

    public void setContrPath(String contrPath) {
        this.contrPath = contrPath;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getImplPath() {
        return implPath;
    }

    public void setImplPath(String implPath) {
        this.implPath = implPath;
    }

    public String getDomainPath() {
        return domainPath;
    }

    public void setDomainPath(String domainPath) {
        this.domainPath = domainPath;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public String getMapperPath() {
        return mapperPath;
    }

    public void setMapperPath(String mapperPath) {
        this.mapperPath = mapperPath;
    }

    public boolean getAccessors() {
        return accessors;
    }

    public void setAccessors(boolean accessors) {
        this.accessors = accessors;
    }

    public boolean isAccessors() {
        return accessors;
    }

    public boolean isActiveRecord() {
        return activeRecord;
    }

    public void setActiveRecord(boolean activeRecord) {
        this.activeRecord = activeRecord;
    }

    public boolean isRequiredArgsConstructor() {
        return requiredArgsConstructor;
    }

    public void setRequiredArgsConstructor(boolean requiredArgsConstructor) {
        this.requiredArgsConstructor = requiredArgsConstructor;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getFromCheck() {
        return fromCheck;
    }

    public String getSqlDialect() {
        return sqlDialect;
    }

    public void setSqlDialect(String sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    public String getMapperXmlType() {
        return mapperXmlType;
    }

    public void setMapperXmlType(String mapperXmlType) {
        this.mapperXmlType = mapperXmlType;
    }

    public String getInterfacePre() {
        return interfacePre;
    }

    public void setInterfacePre(String interfacePre) {
        this.interfacePre = interfacePre;
    }
}
