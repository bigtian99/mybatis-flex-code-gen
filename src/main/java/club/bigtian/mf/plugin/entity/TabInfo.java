package club.bigtian.mf.plugin.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.intellij.ui.LanguageTextField;

public class TabInfo {
    private String title;
    private String content;

    private String genPath;

    private String suffix;

    @JSONField(serialize = false)
    private LanguageTextField textField;

    public LanguageTextField getTextField() {
        return textField;
    }

    public void setTextField(LanguageTextField textField) {
        this.textField = textField;
    }

    public String getGenPath() {
        return genPath;
    }

    public void setGenPath(String genPath) {
        this.genPath = genPath;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public TabInfo(String title, String content, String genPath, String suffix, LanguageTextField languageTextField) {
        this.title = title;
        this.content = content;
        this.genPath = genPath;
        this.suffix = suffix;
        this.textField = languageTextField;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
