
package club.bigtian.mf.plugin.core.util;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.util.PackageUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileChooser.ex.TextFieldAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.PackageChooser;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NlsContexts.DialogTitle;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JavaReferenceEditorUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Alarm;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;


@SuppressWarnings("deprecation")
public class PackageChooserDialogCustom extends PackageChooser {
    private static final Logger LOG = Logger.getInstance(PackageChooserDialogCustom.class);
    private Tree myTree;
    private DefaultTreeModel myModel;
    private final Project myProject;
    private final @DialogTitle String myTitle;
    private Module myModule;
    private EditorTextField myPathEditor;
    private final Alarm myAlarm;

    public PackageChooserDialogCustom(@DialogTitle String title, @NotNull Module module) {
        super(module.getProject(), true);
        this.myAlarm = new Alarm(this.getDisposable());
        this.setTitle(title);
        this.myTitle = title;
        this.myProject = module.getProject();
        this.myModule = module;
        this.init();
    }

    public PackageChooserDialogCustom(@DialogTitle String title, Project project) {
        super(project, true);
        this.myAlarm = new Alarm(this.getDisposable());
        this.setTitle(title);
        this.myTitle = title;
        this.myProject = project;
        this.init();
    }

    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.myModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        this.createTreeModel();
        this.myTree = new Tree(this.myModel);
        this.myTree.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                this.setIcon(PlatformIcons.PACKAGE_ICON);
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    Object object = node.getUserObject();
                    if (object instanceof PsiPackage) {
                        String name = ((PsiPackage) object).getName();
                        if (name != null && name.length() > 0) {
                            this.setText(name);
                        } else {
                            this.setText("<default>");
                        }
                    }
                }

                return this;
            }
        });
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(this.myTree);
        scrollPane.setPreferredSize(JBUI.size(500, 300));
        new TreeSpeedSearch(this.myTree, (path) -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            Object object = node.getUserObject();
            return object instanceof PsiPackage ? ((PsiPackage) object).getName() : "";
        });
        this.myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                PsiPackage selection = PackageChooserDialogCustom.this.getTreeSelection();
                if (selection != null) {
                    String name = selection.getQualifiedName();
                    String var10001 = PackageChooserDialogCustom.this.myTitle;
                    PackageChooserDialogCustom.this.setTitle(var10001 + " - " + (name.isEmpty() ? IdeBundle.message("node.default.package", new Object[0]) : name));
                } else {
                    PackageChooserDialogCustom.this.setTitle(PackageChooserDialogCustom.this.myTitle);
                }

                PackageChooserDialogCustom.this.updatePathFromTree();
            }
        });
        panel.add(scrollPane, "Center");
        DefaultActionGroup group = this.createActionGroup(this.myTree);
        JPanel northPanel = new JPanel(new BorderLayout());
        panel.add(northPanel, "North");
        ActionToolbar toolBar = ActionManager.getInstance().createActionToolbar(PackageChooserDialogCustom.class.getSimpleName(), group, true);
        northPanel.add(toolBar.getComponent(), "West");
        this.setupPathComponent(northPanel);
        return panel;
    }

    private void setupPathComponent(final JPanel northPanel) {
        northPanel.add(new TextFieldAction() {
            public void linkSelected(LinkLabel aSource, Object aLinkData) {
                PackageChooserDialogCustom.this.toggleShowPathComponent(northPanel, this);
            }
        }, "East");
        this.myPathEditor = new EditorTextField(JavaReferenceEditorUtil.createDocument("", this.myProject, false), this.myProject, JavaFileType.INSTANCE);
        this.myPathEditor.addDocumentListener(new DocumentListener() {
            public void documentChanged(@NotNull DocumentEvent e) {

                PackageChooserDialogCustom.this.myAlarm.cancelAllRequests();
                PackageChooserDialogCustom.this.myAlarm.addRequest(() -> {
                    PackageChooserDialogCustom.this.updateTreeFromPath();
                }, 300);
            }
        });
        this.myPathEditor.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        northPanel.add(this.myPathEditor, "South");
    }

    private void toggleShowPathComponent(JPanel northPanel, TextFieldAction fieldAction) {
        boolean toShowTextField = !isPathShowing();
        PropertiesComponent.getInstance().setValue("FileChooser.ShowPath", toShowTextField, true);
        this.myPathEditor.setVisible(toShowTextField);
        fieldAction.update();
        northPanel.revalidate();
        northPanel.repaint();
        this.updatePathFromTree();
    }

    private static boolean isPathShowing() {
        return PropertiesComponent.getInstance().getBoolean("FileChooser.ShowPath", true);
    }

    private void updatePathFromTree() {
        if (isPathShowing()) {
            PsiPackage selection = this.getTreeSelection();
            this.myPathEditor.setText(selection != null ? selection.getQualifiedName() : "");
        }
    }

    private void updateTreeFromPath() {
        this.selectPackage(this.myPathEditor.getText().trim());
    }

    private DefaultActionGroup createActionGroup(JComponent component) {
        DefaultActionGroup group = new DefaultActionGroup();
        DefaultActionGroup temp = new DefaultActionGroup();
        NewPackageAction newPackageAction = new NewPackageAction();
        newPackageAction.enableInModalConext();
        newPackageAction.registerCustomShortcutSet(ActionManager.getInstance().getAction("NewElement").getShortcutSet(), component);
        temp.add(newPackageAction);
        group.add(temp);
        return group;
    }

    public String getDimensionServiceKey() {
        return "#com.intellij.ide.util.PackageChooserDialogBigtian";
    }

    public JComponent getPreferredFocusedComponent() {
        return this.myTree;
    }

    public PsiPackage getSelectedPackage() {
        return this.getExitCode() == 1 ? null : this.getTreeSelection();
    }

    public List<PsiPackage> getSelectedPackages() {
        return TreeUtil.collectSelectedObjectsOfType(this.myTree, PsiPackage.class);
    }

    public void selectPackage(String qualifiedName) {
        DefaultMutableTreeNode node = this.findNodeForPackage(qualifiedName);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            TreeUtil.selectPath(this.myTree, path);
        }
    }

    private @Nullable PsiPackage getTreeSelection() {
        if (this.myTree == null) {
            return null;
        } else {
            TreePath path = this.myTree.getSelectionPath();
            if (path == null) {
                return null;
            } else {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                return (PsiPackage) node.getUserObject();
            }
        }
    }

    private void createTreeModel() {
        PsiManager psiManager = PsiManager.getInstance(this.myProject);
        FileIndex fileIndex = this.myModule != null ? ModuleRootManager.getInstance(this.myModule).getFileIndex() : ProjectRootManager.getInstance(this.myProject).getFileIndex();
        fileIndex.iterateContent(fileOrDir -> {
            if (fileOrDir.isDirectory() && (fileIndex.isUnderSourceRootOfType(fileOrDir, JavaModuleSourceRootTypes.SOURCES) || fileIndex.isUnderSourceRootOfType(fileOrDir, JavaModuleSourceRootTypes.RESOURCES))) {
                PsiDirectory psiDirectory = psiManager.findDirectory(fileOrDir);
                LOG.assertTrue(psiDirectory != null);
                PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
                if (aPackage != null) {
                    this.addPackage(aPackage);
                }
            }
            return true;
        });
        TreeUtil.sort(this.myModel, (o1, o2) -> {
            DefaultMutableTreeNode n1 = (DefaultMutableTreeNode) o1;
            DefaultMutableTreeNode n2 = (DefaultMutableTreeNode) o2;
            PsiNamedElement element1 = (PsiNamedElement) n1.getUserObject();
            PsiNamedElement element2 = (PsiNamedElement) n2.getUserObject();
            return element1.getName().compareToIgnoreCase(element2.getName());
        });
    }

    public @NotNull DefaultMutableTreeNode addPackage(PsiPackage aPackage) {
        String qualifiedPackageName = aPackage.getQualifiedName();
        PsiPackage parentPackage = aPackage.getParentPackage();
        DefaultMutableTreeNode rootNode;
        DefaultMutableTreeNode packageNode;
        if (parentPackage == null) {
            rootNode = (DefaultMutableTreeNode) this.myModel.getRoot();
            if (qualifiedPackageName.length() == 0) {
                rootNode.setUserObject(aPackage);
                return rootNode;
            } else {
                packageNode = findPackageNode(rootNode, qualifiedPackageName);
                if (packageNode != null) {
                    return packageNode;
                } else {
                    packageNode = new DefaultMutableTreeNode(aPackage);
                    rootNode.add(packageNode);


                    return packageNode;
                }
            }
        } else {
            rootNode = this.addPackage(parentPackage);
            packageNode = findPackageNode(rootNode, qualifiedPackageName);
            if (packageNode != null) {
                return packageNode;
            } else {
                packageNode = new DefaultMutableTreeNode(aPackage);
                rootNode.add(packageNode);


                return packageNode;
            }
        }
    }

    private static @Nullable DefaultMutableTreeNode findPackageNode(DefaultMutableTreeNode rootNode, String qualifiedName) {
        for (int i = 0; i < rootNode.getChildCount(); ++i) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            PsiPackage nodePackage = (PsiPackage) child.getUserObject();
            if (nodePackage != null && Objects.equals(nodePackage.getQualifiedName(), qualifiedName)) {
                return child;
            }
        }

        return null;
    }

    private DefaultMutableTreeNode findNodeForPackage(String qualifiedPackageName) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.myModel.getRoot();
        Enumeration enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            Object o = enumeration.nextElement();
            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                PsiPackage nodePackage = (PsiPackage) node.getUserObject();
                if (nodePackage != null && Objects.equals(nodePackage.getQualifiedName(), qualifiedPackageName)) {
                    return node;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    private void createNewPackage() {
        PsiPackage selectedPackage = this.getTreeSelection();
        if (selectedPackage != null) {
            String newPackageName = Messages.showInputDialog(this.myProject, IdeBundle.message("prompt.enter.a.new.package.name", new Object[0]), IdeBundle.message("title.new.package", new Object[0]), Messages.getQuestionIcon(), "", new InputValidator() {
                public boolean checkInput(String inputString) {
                    return inputString != null && inputString.length() > 0;
                }

                public boolean canClose(String inputString) {
                    return this.checkInput(inputString);
                }
            });
            if (newPackageName != null) {
                CommandProcessor.getInstance().executeCommand(this.myProject, () -> {
                    Runnable action = () -> {
                        try {
                            String newQualifiedName = selectedPackage.getQualifiedName();
                            if (!Comparing.strEqual(newQualifiedName, "")) {
                                newQualifiedName = newQualifiedName + ".";
                            }
                            newQualifiedName = newQualifiedName + newPackageName;
                            PsiDirectory dir = PackageUtil.findOrCreateDirectoryForPackage(myModule, newQualifiedName, null, false);
                            if (dir == null) {
                                return;
                            }
                            PsiPackage newPackage = JavaDirectoryService.getInstance().getPackage(dir);
                            if (newPackage != null) {
                                DefaultMutableTreeNode newChild = this.addPackage(newPackage);
                                DefaultTreeModel model = (DefaultTreeModel) this.myTree.getModel();
                                model.nodeStructureChanged((TreeNode) model.getRoot());
                                TreePath path = new TreePath(newChild.getPath());
                                this.myTree.setSelectionPath(path);
                                this.myTree.scrollPathToVisible(path);
                                this.myTree.expandPath(path);
                            }
                        } catch (Exception var9) {
                            Messages.showMessageDialog(this.getContentPane(), StringUtil.getMessage(var9), CommonBundle.getErrorTitle(), Messages.getErrorIcon());
                            LOG.debug(var9);
                        }

                    };
                    ApplicationManager.getApplication().runReadAction(action);
                }, IdeBundle.message("command.create.new.package", new Object[0]), (Object) null);
            }
        }
    }

    private class NewPackageAction extends AnAction {
        NewPackageAction() {
            super(IdeBundle.messagePointer("action.new.package", new Object[0]), IdeBundle.messagePointer("action.description.create.new.package", new Object[0]), Actions.NewFolder);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            PackageChooserDialogCustom.this.createNewPackage();
        }

        public void update(@NotNull AnActionEvent event) {
            Presentation presentation = event.getPresentation();
            presentation.setEnabled(PackageChooserDialogCustom.this.getTreeSelection() != null);
        }

        public void enableInModalConext() {
            this.setEnabledInModalContext(true);
        }
    }
}
