package com.quarterlife.chatappwithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
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
import com.google.firebase.database.ValueEventListener;
import com.quarterlife.chatappwithfirebase.Model.User;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView profile_image;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Intent intent;
    private ImageButton btn_send;
    private EditText text_send;

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
                finish(); // 結束此頁
            }
        });

        // 宣告元件
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        // 取得 intent
        intent = getIntent();

        // 取得接收者的 id
        final String userid = intent.getStringExtra("userid");

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 設置發送按鈕的點擊事件
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_image); // 設置接收者的大頭照
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= onCreate END =========//

    //========= 發送訊息（發送者 / 接收者 / 訊息） START =========//
    private void sendMessage(String sender, String receiver, String message){
        // 取得 Database 參考
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>(); // 創建 HashMap
        hashMap.put("sender", sender); // 放置 [發送者] 到 HashMap 裡
        hashMap.put("receiver", receiver); // 放置 [接收者] 到 HashMap 裡
        hashMap.put("message", message); // 放置 [訊息] 到 HashMap 裡

        // 把 hashMap 的值設定給 Chats 的 Database 參考
        reference.child("Chats").push().setValue(hashMap);
    }
    //========= 發送訊息（發送者 / 接收者 / 訊息） END =========//
}