package com.quarterlife.chatappwithfirebase;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class MainActivity extends AppCompatActivity {
    private CircleImageView profile_image;
    private TextView username;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    //========= onCreate START =========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 設置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // 宣告 Toolbar
        setSupportActionBar(toolbar); // 設置使用 Toolbar
        getSupportActionBar().setTitle(""); // 設置 title

        // 宣告元件
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        // 取得用戶
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 取得 Database 的參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        // 新增 Database 的 ValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // 取得 User 的資訊
                username.setText(user.getUsername()); // 取得 username 並設置

                // 取得 user 的 image
                if(user.getImageURL().equals("default")){ // 如果是 default
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else { // 如果有 image
                    Glide.with(MainActivity.this).load(user.getImageURL()).into(profile_image); // 設置 image 到 profile_image 上
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= onCreate END =========//

    //========= 設置 Menu START =========//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu); // 設置 Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            // 點選登出按鈕
            case R.id.logout:
                FirebaseAuth.getInstance().signOut(); // 在 Firebase 登出
                startActivity(new Intent(MainActivity.this, StartActivity.class)); // 跳到 StartActivity
                finish(); // 結束此頁
                return true;
        }

        return false;
    }
    //========= 設置 Menu END =========//
}