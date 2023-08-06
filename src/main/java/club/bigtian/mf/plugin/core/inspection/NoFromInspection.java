package club.bigtian.mf.plugin.core.inspection;

import cn.hutool.core.util.StrUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

public class NoFromInspection extends LocalInspectionTool {
    public static final Key<Boolean> WARING = Key.create("waring");

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);
                try {
                    if (element instanceof PsiLocalVariable || element instanceof PsiExpressionStatement) {
                        if (hasUncheckedException(element)) {
                            String text = element.getText();
                            holder.registerProblem(element, new TextRange(0, text.indexOf(")")), "请添加from 方法，否则查询可能会出错", new QuickFix());
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // 编写自定义的检查逻辑
    private boolean hasUncheckedException(PsiElement expression) {
        // 获取光标
        PsiFile psiFile = expression.getContainingFile();
        String text = expression.getText();
        boolean flag = StrUtil.containsAny(text, "QueryWrapper.", "UpdateChain.", "QueryChain.", ".queryChain()", ".query()");
        if (!psiFile.isWritable() || !flag || !text.endsWith(";")) {
            return false;
        }
        return !text.contains("from(");
    }

    // 自定义 QuickFix 用于修复问题
    private class QuickFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "添加from 方法";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement expression = descriptor.getPsiElement() instanceof PsiLocalVariable
                    ? descriptor.getPsiElement().getLastChild().getPrevSibling()
                    : descriptor.getPsiElement();

            String text = expression.getText();
            TextRange range = descriptor.getTextRangeInElement();
            PsiElementFactory instance = PsiElementFactory.getInstance(project);
            int idx = text.indexOf(")");
            String prefix = StrUtil.sub(text, range.getStartOffset(), idx + 1);
            String suffix = StrUtil.sub(text, idx + 1, text.length());
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            PsiElement statement = instance.createStatementFromText(prefix + "\n.from()" + suffix, null);
            int offset = expression.getTextOffset() + prefix.length() + 7;
            expression.replace(statement);
            // 同步更新文档
            editor.getCaretModel().moveToOffset(offset);
        }
    }
}