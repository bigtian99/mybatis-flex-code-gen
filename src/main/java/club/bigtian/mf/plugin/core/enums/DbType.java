package club.bigtian.mf.plugin.core.enums;

public enum DbType {

    /**
     * MYSQL
     */
    MYSQL("MYSQL", "MySql 数据库"),

    /**
     * MARIADB
     */
    MARIADB("MARIADB", "MariaDB 数据库"),

    /**
     * ORACLE
     */
    ORACLE("ORACLE", "Oracle11g 及以下数据库"),

    /**
     * oracle12c
     */
    ORACLE_12C("ORACLE_12C", "Oracle12c 及以上数据库"),

    /**
     * DB2
     */
    DB2("DB2", "DB2 数据库"),

    /**
     * H2
     */
    H2("H2", "H2 数据库"),

    /**
     * HSQL
     */
    HSQL("HSQL", "HSQL 数据库"),

    /**
     * SQLITE
     */
    SQLITE("SQLITE", "SQLite 数据库"),

    /**
     * POSTGRE
     */
    POSTGRE_SQL("POSTGRE_SQL", "PostgreSQL 数据库"),

    /**
     * SQLSERVER
     */
    SQLSERVER("SQLSERVER", "SQLServer 数据库"),

    /**
     * SqlServer 2005 数据库
     */
    SQLSERVER_2005("SQLSERVER_2005", "SQLServer 数据库"),

    /**
     * DM
     */
    DM("DM", "达梦数据库"),

    /**
     * xugu
     */
    XUGU("XUGU", "虚谷数据库"),

    /**
     * Kingbase
     */
    KINGBASE_ES("KINGBASE_ES", "人大金仓数据库"),

    /**
     * Phoenix
     */
    PHOENIX("PHOENIX", "Phoenix HBase 数据库"),

    /**
     * Gauss
     */
    GAUSS("GAUSS", "Gauss 数据库"),

    /**
     * ClickHouse
     */
    CLICK_HOUSE("CLICK_HOUSE", "clickhouse 数据库"),

    /**
     * GBase
     */
    GBASE("GBASE", "南大通用(华库)数据库"),

    /**
     * GBase-8s
     */
    GBASE_8S("GBASE_8S", "南大通用数据库 GBase 8s"),

    /**
     * Oscar
     */
    OSCAR("OSCAR", "神通数据库"),

    /**
     * Sybase
     */
    SYBASE("SYBASE", "Sybase ASE 数据库"),

    /**
     * OceanBase
     */
    OCEAN_BASE("OCEAN_BASE", "OceanBase 数据库"),

    /**
     * Firebird
     */
    FIREBIRD("FIREBIRD", "Firebird 数据库"),

    /**
     * derby
     */
    DERBY("DERBY", "Derby 数据库"),

    /**
     * HighGo
     */
    HIGH_GO("HIGH_GO", "瀚高数据库"),

    /**
     * CUBRID
     */
    CUBRID("CUBRID", "CUBRID 数据库"),

    /**
     * GOLDILOCKS
     */
    GOLDILOCKS("GOLDILOCKS", "GOLDILOCKS 数据库"),

    /**
     * CSIIDB
     */
    CSIIDB("CSIIDB", "CSIIDB 数据库"),

    /**
     * CSIIDB
     */
    SAP_HANA("SAP_HANA", "SAP_HANA 数据库"),

    /**
     * Impala
     */
    IMPALA("IMPALA", "impala 数据库"),

    /**
     * Vertica
     */
    VERTICA("VERTICA", "vertica数据库"),

    /**
     * 东方国信 xcloud
     */
    XCloud("XCloud", "行云数据库"),

    /**
     * redshift
     */
    REDSHIFT("REDSHIFT", "亚马逊 redshift 数据库"),

    /**
     * openGauss
     */
    OPENGAUSS("OPENGAUSS", "华为 openGauss 数据库"),

    /**
     * TDengine
     */
    TDENGINE("TDENGINE", "TDengine 数据库"),

    /**
     * Informix
     */
    INFORMIX("INFORMIX", "Informix 数据库"),

    /**
     * sinodb
     */
    SINODB("SINODB", "SinoDB 数据库"),

    /**
     * uxdb
     */
    UXDB("UXDB", "优炫数据库"),

    /**
     * greenplum
     */
    GREENPLUM("GREENPLUM", "greenplum 数据库"),

    /**
     * lealone
     */
    LEALONE("LEALONE", "lealone 数据库"),

    /**
     * Hive SQL
     */
    HIVE("HIVE", "Hive SQL"),

    /**
     * Doris 兼容 Mysql，使用 MySql 驱动和协议
     */
    DORIS("DORIS", "doris 数据库"),

    /**
     * UNKNOWN DB
     */
    OTHER("OTHER", "其他数据库");

    /**
     * 数据库名称
     */
    private final String name;

    /**
     * 描述
     */
    private final String remarks;

    public String getRemarks() {
        return remarks;
    }


    DbType(String name, String remarks) {
        this.name = name;
        this.remarks = remarks;
    }

    public String getName() {
        return name;
    }
}
