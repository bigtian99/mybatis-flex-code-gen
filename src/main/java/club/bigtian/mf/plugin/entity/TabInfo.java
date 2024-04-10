package club.bigtian.mf.plugin.entity;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;

public class TabInfo {
    private String title;
    private String content;

    private String genPath;

    private String suffix;

    @JSONField(serialize = false)
    private Editor textField;

    @JSONField(serialize = false)
    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Editor getTextField() {
        return textField;
    }

    public void setTextField(Editor textField) {
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

    public TabInfo(String title, String content, String genPath, String suffix, Editor editor) {
        this.title = title;
        this.content = content;
        this.genPath = genPath;
        this.suffix = suffix;
        this.textField = editor;
        if (ObjectUtil.isNotNull(editor)) {
            this.document = editor.getDocument();
        }
    }

    public TabInfo(String title, String content, String suffix) {
        this.title = title;
        this.content = content;
        this.genPath = genPath;
        this.suffix = suffix;

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
