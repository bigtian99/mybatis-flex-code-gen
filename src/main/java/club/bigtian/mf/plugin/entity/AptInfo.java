package club.bigtian.mf.plugin.entity;

public class AptInfo {
    private String name;
    private String columnName;

    public AptInfo(String columnName, String name) {
        this.name = name;
        this.columnName = columnName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
