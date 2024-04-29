package club.bigtian.mf.plugin.core.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class XmlFileUtil {

    /**
     * 获取第一个resultMap
     *
     * @param xmlFile XML文件
     * @return {@link PsiElement}
     */
    public static XmlTag getResultMap(XmlFile xmlFile) {
        return Objects.requireNonNull(xmlFile.getRootTag()).findFirstSubTag("resultMap");
    }

    /**
     * 获取第所有的resultMap
     *
     * @param xmlFile XML文件
     * @return {@link PsiElement}
     */
    public static List<XmlTag> getResultMaps(XmlFile xmlFile) {
        return Arrays.asList(Objects.requireNonNull(xmlFile.getRootTag()).findSubTags("resultMap"));
    }

}
