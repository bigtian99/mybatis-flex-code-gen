package club.bigtian.mf.plugin.core.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.kotlin.idea.actions.JavaToKotlinAction;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.*;
import java.util.stream.Collectors;

public class KtFileUtil {

    public static Set<String> getImportSet(KtFile ktFile) {
        return Objects.requireNonNull(ktFile.getImportList()).getImports().stream()
                .map(el -> Objects.requireNonNull(el.getImportedFqName()).asString())
                .collect(Collectors.toSet());
    }

    /**
     * 转换kt文件
     *
     * @param psiJavaFiles PSI Java文件
     * @return {@link List}<{@link KtFile}>
     */
    public static List<KtFile> convertKtFile(List<PsiJavaFile> psiJavaFiles) {
        List<KtFile> ktFiles = new ArrayList<>();
        WriteCommandAction.runWriteCommandAction(ProjectUtils.getCurrentProject(), () -> {
            for (PsiJavaFile psiFile : psiJavaFiles) {
                // 转换为kt文件
                List<KtFile> ktFiles1 = JavaToKotlinAction.Handler.INSTANCE.convertFiles(
                        Collections.singletonList(psiFile),
                        Objects.requireNonNull(ProjectUtils.getCurrentProject()),
                        Modules.getModuleFromDirectory(psiFile.getContainingDirectory()),
                        false,
                        false,
                        false
                );
                ktFiles.addAll(ktFiles1);
            }

        });
        return ktFiles;
    }
}
