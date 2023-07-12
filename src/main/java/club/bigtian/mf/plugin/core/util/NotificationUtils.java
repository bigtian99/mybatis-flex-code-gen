package club.bigtian.mf.plugin.core.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import club.bigtian.mf.plugin.core.constant.MybatisFlexConstant;

/**
 * 通知工具类
 */
public class NotificationUtils {

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

    /**
     * 通知成功
     *
     * @param content 内容
     * @param project 项目
     */
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


}
