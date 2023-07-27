package club.bigtian.mf.plugin.entity;

import java.util.List;

public class MybatisFlexConfgInfo {


    private List<String> value;

    private String description;

    private boolean moveCaret;

    public MybatisFlexConfgInfo(List<String> value, String description, boolean moveCaret) {
        this.value = value;
        this.description = description;
        this.moveCaret = moveCaret;
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

    public boolean isMoveCaret() {
        return moveCaret;
    }

    public void setMoveCaret(boolean moveCaret) {
        this.moveCaret = moveCaret;
    }
}
