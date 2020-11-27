package com.quarterlife.chatappwithfirebase.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

public class OreoNotification extends ContextWrapper {
    private static final String CHANNEL_ID = "com.quarterlife.chatappwithfirebase";
    private static final String CHANNEL_NAME = "chatapp";
    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){ // 若版本是 8.0 以上
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel(){
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);

        /*  IMPORTANCE_NONE：關閉通知
         *  IMPORTANCE_MIN：開啟通知，不會彈出，但没有提示音，狀態欄中無顯示
         *  IMPORTANCE_LOW：開啟通知，不會彈出，不發出提示音，狀態欄中顯示
         *  IMPORTANCE_DEFAULT：開啟通知，不會彈出，發出提示音，狀態欄中顯示
         *  IMPORTANCE_HIGH：開啟通知，會彈出，發出提示音，狀態欄中顯示    */

        channel.canShowBadge(); // 桌面 launcher 的消息角標
        channel.enableLights(true); // 是否在桌面 icon 右上角展示小紅點
        channel.setLightColor(Color.RED); // 閃光燈的燈光顏色
        channel.enableVibration(true); // 是否允許震動
        channel.setShowBadge(false); // 是否在久按桌面圖標時顯示此渠道的通知
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE); // 鎖屏顯示通知
        channel.getAudioAttributes(); // 獲取系統通知鈴聲的配置
        channel.setBypassDnd(true); // 是否繞過請勿打擾模式
        channel.canBypassDnd(); // 設置可繞過請勿打擾的模式

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if(notificationManager == null){
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title, String body,
                                                    PendingIntent pendingIntent, Uri soundUri, String icon){
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pendingIntent) // 設置意圖
                .setContentTitle(title) // 設置 title
                .setContentText(body) // 設置訊息
                .setSmallIcon(Integer.parseInt(icon)) // 設置 icon
                .setSound(soundUri) // 設置提示音
                .setAutoCancel(true); // 點擊通知後，通知自動消失
    }
}
