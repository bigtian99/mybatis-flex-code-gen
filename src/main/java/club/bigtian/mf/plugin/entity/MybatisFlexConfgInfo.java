package club.bigtian.mf.plugin.entity;

import java.util.List;

public class MybatisFlexConfgInfo {

    private List<String> value;

    private String description;


    public MybatisFlexConfgInfo(List<String> value, String description) {
        this.value = value;
        this.description = description;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
