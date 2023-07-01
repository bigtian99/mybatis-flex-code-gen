package com.mybatisflex.plugin.core.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.mybatisflex.plugin.core.constant.MybatisFlexConstant;
import com.mybatisflex.plugin.core.functions.SimpleFunction;

/**
 * 通知工具类
 */
public class NotificationUtils {

    public static void notifySuccess(String content, String title, Project project) {
        Notification notification = new Notification(
                MybatisFlexConstant.NOTICE_GROUP_ID,
                title,
                content,
                NotificationType.INFORMATION
        );
        // 在屏幕右下角显示通知
        Notifications.Bus.notify(notification, project);
//        // 可选：激活项目窗口
//        WindowManager.getInstance().getFrame(project).toFront();
    }

    public static void notifySuccess(String content, Project project) {
        notifySuccess(content, "提示", project);
    }

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
