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
    private String fileName;

    private int sort;

    @JSONField(serialize = false)
    private Editor textField;

    @JSONField(serialize = false)
    private Document document;

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

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

    public TabInfo(String title, String content, String suffix, int sort) {
        this.title = title;
        this.content = content;
        this.suffix = suffix;
        this.sort = sort;
    }

    public TabInfo(String title, String content, String genPath, String suffix, int sort, String fileName) {
        this.title = title;
        this.content = content;
        this.suffix = suffix;
        this.genPath = genPath;
        this.fileName = fileName;
        this.sort = sort;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
