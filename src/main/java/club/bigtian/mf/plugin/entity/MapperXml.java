package club.bigtian.mf.plugin.entity;

import com.intellij.psi.PsiElement;


public class MapperXml {
    private PsiElement mapper;
    private PsiElement xml;

    public MapperXml(PsiElement mapper) {
        this.mapper = mapper;
    }


    public MapperXml(PsiElement mapper, PsiElement xml) {
        this.mapper = mapper;
        this.xml = xml;
    }

    public PsiElement getXml() {
        return xml;
    }

    public void setXml(PsiElement xml) {
        this.xml = xml;
    }

    public PsiElement getMapper() {
        return mapper;
    }

    public void setMapper(PsiElement mapper) {
        this.mapper = mapper;
    }
}
