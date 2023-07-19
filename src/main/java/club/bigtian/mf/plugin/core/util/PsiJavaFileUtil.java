package club.bigtian.mf.plugin.core.util;

import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaFile;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PsiJavaFileUtil {
    public static Set<String> getImportSet(PsiJavaFile psiJavaFile) {
        return Arrays.stream(Objects.requireNonNull(psiJavaFile.getImportList()).getAllImportStatements())
                .map(PsiImportStatementBase::getText)
                .collect(Collectors.toSet());
    }
}
