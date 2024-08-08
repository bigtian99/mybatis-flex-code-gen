package club.bigtian.mf.plugin.core.contributor;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.ide.ui.AntialiasingType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.impl.FontInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.GraphicsUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static club.bigtian.mf.plugin.core.contributor.MybatisFlexTableDefCompletionContributor.GrayCodeRenderer.addInlayExample;


public class MybatisFlexTableDefCompletionContributor extends CompletionContributor {
    private Inlay<?> currentInlay;
    private String suggestedCode = "suggestedCodeSnippet";  // Example suggested code
    private KeyAdapter keyAdapter;
    private static int column;

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (isCompletionActive(parameters.getPosition().getProject())) {
            result.stopHere();
        }

    }

    public boolean isCompletionActive(Project project) {
        return LookupManager.getInstance(project).getActiveLookup() != null;
    }
    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);

    }


    private void removeInlay(Editor editor) {
        InlayModel inlayModel = editor.getInlayModel();
        inlayModel.getInlineElementsInRange(0, editor.getDocument().getTextLength()).forEach(Disposer::dispose);
        inlayModel.getBlockElementsInRange(0, editor.getDocument().getTextLength()).forEach(Disposer::dispose);
    }

    public static class GrayCodeRenderer<T> implements EditorCustomElementRenderer {
        private final Editor editor;
        private final T renderText;
        private final float wordCount;
        private final Font font;

        public GrayCodeRenderer(Editor editor, T renderText) {
            this(editor, renderText, 0f);
        }

        public GrayCodeRenderer(Editor editor, T renderText, float wordCount) {
            this.editor = editor;
            this.renderText = renderText;
            this.wordCount = wordCount;
            this.font = new Font("Microsoft YaHei", Font.ITALIC, editor.getColorsScheme().getEditorFontSize());
        }


        @Override
        public int calcWidthInPixels(@NotNull Inlay inlay) {
            Editor editor = inlay.getEditor();
            FontMetrics metrics = editor.getContentComponent().getFontMetrics(editor.getColorsScheme().getFont(EditorFontType.PLAIN));
            return metrics.stringWidth("庄周de蝴蝶");
        }

        @Override
        public void paint(@NotNull Inlay inlay, Graphics g, Rectangle targetRegion, TextAttributes textAttributes) {
            Graphics2D g2 = (Graphics2D) g.create();
            GraphicsUtil.setupAAPainting(g2);
            textAttributes.setForegroundColor(JBColor.GRAY);

            double lineHeight = editor.getLineHeight();
            GlyphVector gv = font.createGlyphVector(getFontMetrics().getFontRenderContext(), "中文");
            Rectangle2D visualBounds = gv.getVisualBounds();
            double fontBaseline = Math.ceil(visualBounds.getHeight());
            double linePadding = (lineHeight - fontBaseline) / 2.0;

            int offsetX = targetRegion.x;
            double offsetY = targetRegion.y + fontBaseline + linePadding;
            g2.setFont(font);
            g2.setColor(JBColor.GRAY);

            if (renderText instanceof String) {
                g2.drawString((String) renderText, offsetX, (float) offsetY);
            } else if (renderText instanceof java.util.List) {
                ArrayList renderList = (ArrayList) renderText;
                int tabSize = editor.getSettings().getTabSize(editor.getProject());
                for (Object line : renderList) {
                    float startOffset = calcTextWidth(line.toString()) * (wordCount + tabSize);
                    g2.drawString(line.toString(), startOffset, (float) offsetY);
                    g2.translate(0, lineHeight);
                }
            }
            g2.dispose();
        }

        private int calcTextWidth(String text) {
            return getFontMetrics().stringWidth(text);
        }

        private FontMetrics getFontMetrics() {
            FontRenderContext editorContext = FontInfo.getFontRenderContext(editor.getContentComponent());
            FontRenderContext context = new FontRenderContext(
                    editorContext.getTransform(),
                    AntialiasingType.getKeyForCurrentScope(false),
                    editorContext.getFractionalMetricsHint()
            );
            return FontInfo.getFontMetrics(font, context);
        }

        public static void addInlayExample(Editor editor) {
            InlayModel inlayModel = editor.getInlayModel();
            // 清除所有嵌入信息
            inlayModel.getInlineElementsInRange(0, editor.getDocument().getTextLength()).forEach(Disposer::dispose);
            inlayModel.getBlockElementsInRange(0, editor.getDocument().getTextLength()).forEach(Disposer::dispose);

            // 分别增加单行和多行嵌入信息
            int offset = editor.getCaretModel().getOffset();
            column = editor.getCaretModel().getVisualPosition().column;
            inlayModel.addInlineElement(offset, new GrayCodeRenderer<>(editor, "庄周de蝴蝶"));


            // 移动光标位置到初始位置
            editor.getCaretModel().moveToVisualPosition(new VisualPosition(editor.getCaretModel().getVisualPosition().line, column));
        }
    }
}


class KeyEventDispatcherImpl implements KeyEventDispatcher {

    private final Editor editor;
    private final String suggestedCode;

    private static int offset;

    public KeyEventDispatcherImpl(Editor editor, String suggestedCode) {
        this.editor = editor;
        this.suggestedCode = suggestedCode;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            InlayModel inlayModel = editor.getInlayModel();
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                // 确认提示
                insertSuggestedCode(editor, suggestedCode);
                removeInlay(editor);
                e.consume();
                return true;
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE || isPrintableChar(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                // 取消提示
                removeInlay(editor);
                return true;
            }
        }
        return false;
    }

    private boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED && block != null && block != Character.UnicodeBlock.SPECIALS;
    }

    private void insertSuggestedCode(Editor editor, String suggestedCode) {
        Project project = editor.getProject();
        if (project != null) {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                int tmpOffset = offset;
                editor.getDocument().insertString(offset, suggestedCode);
                //     移动光标到当前行最后
                editor.getCaretModel().moveToOffset(tmpOffset + suggestedCode.length());
            });

        }
    }

    private static void removeInlay(Editor editor) {
        InlayModel inlayModel = editor.getInlayModel();
        java.util.@NotNull List<Inlay<?>> inlays = inlayModel.getInlineElementsInRange(0, editor.getDocument().getTextLength());
        for (Inlay inlay : inlays) {
            Disposer.dispose(inlay);
        }
    }

    public static void registerKeyEventDispatcher(Editor editor, String suggestedCode) {
        KeyEventDispatcher dispatcher = new KeyEventDispatcherImpl(editor, suggestedCode);
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                offset = editor.getCaretModel().getOffset();
                removeInlay(editor);
            }
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                addInlayExample(editor);
                registerKeyEventDispatcher(editor, "庄周de蝴蝶");
            }
        });
        editor.addEditorMouseListener(new EditorMouseListener() {
            @Override
            public void mouseClicked(@NotNull EditorMouseEvent e) {
                removeInlay(editor);
            }
        });
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }
}
