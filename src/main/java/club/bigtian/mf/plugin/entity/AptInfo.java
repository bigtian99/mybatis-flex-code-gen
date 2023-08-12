package club.bigtian.mf.plugin.entity;

public class AptInfo {
    private String name;
    private String columnName;

    private boolean large;

    public AptInfo(String columnName, String name,boolean large) {
        this.name = name;
        this.columnName = columnName;
        this.large = large;
    }

    public boolean isLarge() {
        return large;
    }

    public void setLarge(boolean large) {
        large = large;
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
