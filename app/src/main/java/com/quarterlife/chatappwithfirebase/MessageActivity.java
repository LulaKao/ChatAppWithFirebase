package com.quarterlife.chatappwithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quarterlife.chatappwithfirebase.Model.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView profile_image;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // 設置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // 宣告 Toolbar
        setSupportActionBar(toolbar); // 設置使用 Toolbar
        getSupportActionBar().setTitle(""); // 設置 title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 添加返回按鈕

        // setNavigationOnClickListener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // 結束此頁
            }
        });

        // 宣告元件
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        // 取得 intent
        intent = getIntent();

        // 取得 user id
        String userid = intent.getStringExtra("userid");

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 取得目前使用者的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        // addValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // 取得 user 的資訊
                username.setText(user.getUsername()); // 設置 user name

                // 設置 profile_image
                if(user.getImageURL().equals("default")){ // 若沒有圖
                    profile_image.setImageResource(R.mipmap.ic_launcher); // 設置預設圖
                } else { // 若有圖
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_image); // 設置使用者的大頭照
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}