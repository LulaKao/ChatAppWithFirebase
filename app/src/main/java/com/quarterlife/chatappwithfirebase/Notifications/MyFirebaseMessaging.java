package com.quarterlife.chatappwithfirebase.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.quarterlife.chatappwithfirebase.MessageActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 取得接收者 --> 是嗎？
        String sent = remoteMessage.getData().get("sent");
        System.out.println("=== ASG 看一下 sent 是什麼 = " + sent);
        // 取得目前的使用者
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // 如果目前的使用者不為 null，且接收者和目前使用者的 id 一樣 --> 是嗎？
        if(firebaseUser != null && sent.equals(firebaseUser.getUid())){
            sendNotification(remoteMessage); // 發送通知
        }
    }

    //========= 發送通知 START =========//
    private void sendNotification(RemoteMessage remoteMessage) {
        // 取得 user、icon、title、body
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        System.out.println("=== ASG 看一下 user 是什麼 = " + user);

        // 取得 Notification
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        // 去掉不是數字的字元 --> 只保留數字
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        /*  [\D]：取代掉不是數字的字元（"[\\D]"）
        *   [] means one of these..
        *   [^ ..] means (not one of these)
        *   \  is escape character (in Java we need to escape it)
        *   \d means digit  */

        // 跳轉到 MessageActivity
        Intent intent = new Intent(this, MessageActivity.class); // 創建 Intent
        Bundle bundle = new Bundle(); // 創建 Bundle
        bundle.putString("userid", user); // 打包要傳入的值
        intent.putExtras(bundle); // 打包進 intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 新增 flag
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT); // 創建待定意圖

        /*  Intent.FLAG_ACTIVITY_CLEAR_TOP：
            銷毀目標 Activity 和它之上的所有 Activity，
            並重新創建目標 Activity   */

        /*  PendingIntent 相當於對 Intent 執行了包裝，我們不一定要馬上執行它，
            我們將其包裝後傳遞給其他 Activity 或 Application 。
            這時獲取到 PendingIntent 的 Application 能夠根據裡面的 Intent 來得知發出者的意圖選擇執行 。    */

        /*  FLAG_ONE_SHOT：該 PendingIntent 只作用一次 。   */

        // 使用 RingtoneManager 獲取 Notification 的預設提示音
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 創建 NotificationCompat
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon)) // 設置 icon
                .setContentTitle(title) // 設置 title
                .setContentText(body) // 設置訊息
                .setAutoCancel(true) // 點擊通知後，通知自動消失
                .setSound(defaultSound) // 設置提示音
                .setContentIntent(pendingIntent); // 設置意圖

        // 取得 NOTIFICATION_SERVICE
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if(j > 0){ // 若 j > 0
            i = j; // 把 j 的值設定給 i
        }

        // 使用 i 為編號發出通知，當已有編號 i 的通知時就會更新其內容
        manager.notify(i, builder.build());
    }
    //========= 發送通知 END =========//
}
