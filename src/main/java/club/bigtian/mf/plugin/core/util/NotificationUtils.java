package club.bigtian.mf.plugin.core.util;

import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;
import cn.hutool.core.util.StrUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.concurrent.TimeUnit;

/**
 * 通知工具类
 */
public class NotificationUtils {

    private static final String GROUP_LINK = "http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=U0ufvKB9ogRAqBazz1Zxwkf_kMVJjeHB&authKey=wQC13%2Fj5Sr2c1ZaJg%2Fyz5LZNij%2FBK9D98C2OOf3thBFsV4gL6svCUNUPddCLC6cG&noverify=0&group_code=872707845";
    private static final String GITEE_LINK = "https://gitee.com/djxchi/mybatis-flex-code-gen";
    public static final String CONTENT = "<p>Mybatis Flex Helper邀请您给个免费的star ⭐️</p><p style=\"margin-top:4px\"><a href='{}'>去start ⭐️</a><a href='{}'> 进群交流</a></p>";

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

        // 在一定时间后关闭通知
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                notification.expire();
            });
        });
    }
}
