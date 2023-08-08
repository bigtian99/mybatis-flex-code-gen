package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import club.bigtian.mf.plugin.windows.SupportDialog;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 通知工具类
 */
public class NotificationUtils {
    public static final String URL = "https://plugins.jetbrains.com/plugin/22165-mybatis-flex-helper/reviews";
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private static final String GROUP_LINK = "http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=U0ufvKB9ogRAqBazz1Zxwkf_kMVJjeHB&authKey=wQC13%2Fj5Sr2c1ZaJg%2Fyz5LZNij%2FBK9D98C2OOf3thBFsV4gL6svCUNUPddCLC6cG&noverify=0&group_code=872707845";
    private static final String GITEE_LINK = "https://gitee.com/djxchi/mybatis-flex-code-gen";
    public static final String CONTENT = "如果Mybatis Flex Helper插件能减轻您一些繁琐的工作，麻烦您给个免费的star ⭐️";
    public static boolean isNotified = false;

    /**
     * 通知成功
     *
     * @param content 内容
     * @param title   标题
     * @param project 项目
     */
    public static void notifySuccess(String content, String title, Project project) {
        Notification notification = new Notification(
                MybatisFlexConstant.NOTICE_GROUP_ID,
                title,
                content,
                NotificationType.INFORMATION
        );
        // 在屏幕右下角显示通知
        Notifications.Bus.notify(notification, project);
    }

    public static void notifySuccess(String content, String title) {
        Notification notification = new Notification(
                MybatisFlexConstant.NOTICE_GROUP_ID,
                title,
                content,
                NotificationType.INFORMATION
        );
        // 在屏幕右下角显示通知
        Notifications.Bus.notify(notification, ProjectUtils.getCurrentProject());
    }

    /**
     * 通知成功
     *
     * @param content 内容
     */
    public static void notifySuccess(String content) {
        notifySuccess(content, "提示", ProjectUtils.getCurrentProject());
    }

    public static void notifySuccess(String content, Project project) {
        notifySuccess(content, "提示", project);
    }

    /**
     * 通知警告
     *
     * @param content 内容
     * @param title   标题
     * @param project 项目
     */
    public static void notifyWarning(String content, String title, Project project) {
        Notification notification = new Notification(
                MybatisFlexConstant.NOTICE_GROUP_ID,
                title,
                content,
                NotificationType.WARNING
        );
        // 在屏幕右下角显示通知
        Notifications.Bus.notify(notification, project);
    }

    /**
     * 通知错误
     *
     * @param content 内容
     * @param title   标题
     * @param project 项目
     */
    public static void notifyError(String content, String title, Project project) {
        Notification notification = new Notification(
                MybatisFlexConstant.NOTICE_GROUP_ID,
                title,
                content,
                NotificationType.ERROR
        );
        // 在屏幕右下角显示通知
        Notifications.Bus.notify(notification, project);
    }

    public static void notifyError(String content, String title) {
        Notification notification = new Notification(
                MybatisFlexConstant.NOTICE_GROUP_ID,
                title,
                content,
                NotificationType.ERROR
        );
        // 在屏幕右下角显示通知
        Notifications.Bus.notify(notification, ProjectUtils.getCurrentProject());
    }

    public static void start() {
        Runnable task = () -> {
            supportNotice();
        };
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);

    }

    public static void supportNotice() {
        Notification notification = new Notification(
                "com.mybatisflex.plugin",
                "Mybatis Flex Helper",
                StrUtil.format(CONTENT, GITEE_LINK, GROUP_LINK),
                NotificationType.INFORMATION
        );
        // 添加链接的点击处理事件
        notification.setListener(new NotificationListener.Adapter() {
            @Override
            protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
                BrowserUtil.browse(e.getDescription());
            }
        });
        Notifications.Bus.notify(notification);

        AnAction starAction = new NotificationAction("⭐ 去点star") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                try {
                    Desktop dp = Desktop.getDesktop();
                    if (dp.isSupported(Desktop.Action.BROWSE)) {
                        dp.browse(URI.create(GITEE_LINK));
                    }
                } catch (Exception ex) {
                }
            }
        };

        AnAction reviewsAction = new NotificationAction("\uD83D\uDC4D 五星好评") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                try {
                    Desktop dp = Desktop.getDesktop();
                    if (dp.isSupported(Desktop.Action.BROWSE)) {
                        dp.browse(URI.create(URL));
                    }
                } catch (Exception ex) {
                }
            }
        };

        AnAction payAction = new NotificationAction("\uD83C\uDF57 加个鸡腿") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                SupportDialog supportView = new SupportDialog();
                supportView.show();
            }
        };

        notification.addAction(starAction);
        notification.addAction(reviewsAction);
        notification.addAction(payAction);

    }
}
