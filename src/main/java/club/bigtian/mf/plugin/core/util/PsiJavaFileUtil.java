package club.bigtian.mf.plugin.core.util;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PsiJavaFileUtil {
    public static Set<String> getImportSet(PsiJavaFile psiJavaFile) {
        PsiImportList importList = psiJavaFile.getImportList();
        if (ObjectUtil.isNull(importList)) {
            return new HashSet<>();
        }

        return Arrays.stream(Objects.requireNonNull(importList).getAllImportStatements())
                .map(PsiImportStatementBase::getText)
                .collect(Collectors.toSet());
    }
}
