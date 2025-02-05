package com.quarterlife.chatappwithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.quarterlife.chatappwithfirebase.Adapter.MessageAdapter;
import com.quarterlife.chatappwithfirebase.Fragments.APIService;
import com.quarterlife.chatappwithfirebase.Model.Chat;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.Notifications.Client;
import com.quarterlife.chatappwithfirebase.Notifications.Data;
import com.quarterlife.chatappwithfirebase.Notifications.MyResponse;
import com.quarterlife.chatappwithfirebase.Notifications.Sender;
import com.quarterlife.chatappwithfirebase.Notifications.Token;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView profile_image;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Intent intent;
    private ImageButton btn_send;
    private EditText text_send;
    private MessageAdapter messageAdapter;
    private List<Chat> mChat;
    private RecyclerView recyclerView;
    private ValueEventListener seenListener;
    private String userid;
    private APIService apiService;
    private boolean notify = false;

    //========= onCreate START =========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // 設置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // 宣告 Toolbar
        setSupportActionBar(toolbar); // 設置使用 Toolbar
        getSupportActionBar().setTitle(""); // 設置 title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 添加返回按鈕

        // 設置 Toolbar 導航欄的點擊事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                /*  Intent.FLAG_ACTIVITY_CLEAR_TOP：
                    銷毀目標 Activity 和它之上的所有 Activity，
                    並重新創建目標 Activity   */

                finish(); // 結束此頁
            }
        });

        // 取得 API Service
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        // 宣告元件
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        // 設定 RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // 從底部開始向上展示，數據會逆序添加。頁面定位在布局底部。
//        linearLayoutManager.setReverseLayout(true); // 將數據從布局的底部由下往上排列。上滑加載後面的數據。頁面定位在布局底部。
        recyclerView.setLayoutManager(linearLayoutManager);

        // 取得 intent
        intent = getIntent();

        // 取得接收者的 id
        userid = intent.getStringExtra("userid");

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 設置發送按鈕的點擊事件
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true; // 設置 notify = true
                String msg = text_send.getText().toString(); // 取得使用者輸入的文字

                if(!msg.equals("")){ // 如果有輸入東西
                    sendMessage(firebaseUser.getUid(), userid, msg); // 發送訊息（發送者 / 接收者 / 訊息）
                } else { // 如果沒有輸入東西
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }

                text_send.setText(""); // 清空輸入欄
            }
        });

        // 取得接收者的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        // 讓接收者的 Database 參考 addValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // 取得接收者的資訊
                username.setText(user.getUsername()); // 設置接收者的 name

                // 設置接收者的 profile_image
                if(user.getImageURL().equals("default")){ // 若沒有圖
                    profile_image.setImageResource(R.mipmap.ic_launcher); // 設置預設圖
                } else { // 若有圖
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image); // 設置接收者的大頭照
                }

                // 讀取聊天紀錄
                readMessages(firebaseUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid); // 設置已讀訊息
    }
    //========= onCreate END =========//

    //========= 設置已讀訊息 START =========//
    private void seenMessage(final String user_id){
        // 取得聊天內容的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        // add ValueEventListener
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 跑迴圈取得聊天資訊
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class); // 取得聊天資訊

                    // 若接收者是使用者自己，且發送者是點選的聊天對象 --> 確認我（使用者自己）是否有看到訊息（已開啟 MessageActivity 代表已讀）
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(user_id)){
                        HashMap<String, Object> hashMap = new HashMap<>(); // 創建 HashMap
                        hashMap.put("is_seen", true); // 放置 [已讀] 到 HashMap 裡
                        snapshot.getRef().updateChildren(hashMap); // 把 hashMap 的值設定給各個項目的參考
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 設置已讀訊息 END =========//

    //========= 發送訊息（發送者 / 接收者 / 訊息） START =========//
    private void sendMessage(String sender, final String receiver, String message){
        // 取得 Database 參考
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>(); // 創建 HashMap
        hashMap.put("sender", sender); // 放置 [發送者] 到 HashMap 裡
        hashMap.put("receiver", receiver); // 放置 [接收者] 到 HashMap 裡
        hashMap.put("message", message); // 放置 [訊息] 到 HashMap 裡
        hashMap.put("is_seen", false); // 放置 [訊息讀取狀態] 到 HashMap 裡

        // 把 hashMap 的值設定給 Chats 的 Database 參考
        reference.child("Chats").push().setValue(hashMap);

        //===================== 把聊天對象設置到 Chatlist 裡 START =====================//
        // 取得 Chatlist > firebaseUser.getUid() > userid 的參考
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);

        // 為單一事件添加監聽器
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){ // 如果值不存在
                    chatRef.child("id").setValue(userid); // 設置聊天對象的 id
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //===================== 把聊天對象設置到 Chatlist 裡 END =====================//

        //========= 發送 Notification START =========//
        // 把 message 的值給 msg
        final String msg = message;
        // 取得目前使用者的資料庫參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        // 為目前使用者的資料庫參考增加 ValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // 取得目前使用者的資訊
                if(notify) sendNotification(receiver, user.getUsername(), msg); // 若 notify = true --> 發送通知
                notify = false; // 設置 notify = false
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //========= 發送 Notification END =========//
    }
    //========= 發送訊息（發送者 / 接收者 / 訊息） END =========//

    //========= 發送通知（接收者 / 目前使用者的名稱 / 訊息） START =========//
    private void sendNotification(String receiver, final String username, final String message) {
        // 取得 Tokens 的資料庫參考
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        // 查詢接收者的 token
        Query query = tokens.orderByKey().equalTo(receiver);
        // 查詢接收者的 token addValueEventListener
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 跑迴圈取得 Tokens 的資料
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // 取得 Token 的資料
                    Token token = snapshot.getValue(Token.class);
                    // 創建 Data
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher,
                            username + ": " + message, "New Message", userid);
                    // 創建 Sender
                    Sender sender = new Sender(data, token.getToken());
                    // 讓 APIService 發送通知
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){ // 若 response code 是 200
                                        if(response.body().success != 1){ // 若 success 不是 1
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show(); // 顯示失敗的 Toast
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 發送通知 END =========//

    //========= 讀取訊息 START =========//
    private void readMessages(final String my_id, final String user_id, final String image_url){
        // 創建 ArrayList
        mChat = new ArrayList<>();
        // 取得聊天內容的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        // 讓聊天內容的 Database 參考 addValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear(); // 清空 ArrayList

                // 跑迴圈取得聊天資訊
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class); // 取得聊天資訊

                    // 篩選只屬於目前使用者和該聊天對象的聊天紀錄
                    if(chat.getReceiver().equals(my_id) && chat.getSender().equals(user_id) ||
                            chat.getReceiver().equals(user_id) && chat.getSender().equals(my_id)){
                        mChat.add(chat); // 符合條件的話就添加進 ArrayList
                    }

                    // 創建 MessageAdapter
                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, image_url);

                    // 綁定適配器到 RecyclerView 上
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 讀取訊息 END =========//

    //========= 紀錄目前正在聊天的對象到 SharedPreferences 裡 START =========//
    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userid);
        editor.apply();
    }
    //========= 紀錄目前正在聊天的對象到 SharedPreferences 裡 END =========//

    //========= 設置使用者狀態 START =========//
    private void setStatus(String status){
        // 取得現在使用者的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>(); // 創建 HashMap
        hashMap.put("status", status); // 放置 [使用者的狀態] 到 HashMap 裡

        reference.updateChildren(hashMap); // 把 hashMap 的值設定給目前使用者的 Database 參考
    }
    //========= 設置使用者狀態 END =========//

    //========= onResume START =========//
    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online"); // 設置使用者狀態為上線
        currentUser(userid); // 紀錄目前正在聊天的對象到 SharedPreferences 裡
    }
    //========= onResume END =========//

    //========= onPause START =========//
    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener); // 移除 seenListener --> onResume 時不用恢復嗎？
        setStatus("offline"); // 設置使用者狀態為下線
        currentUser("none"); // 紀錄目前正在聊天的對象到 SharedPreferences 裡
    }
    //========= onPause END =========//
}