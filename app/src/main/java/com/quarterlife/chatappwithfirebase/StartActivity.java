package com.quarterlife.chatappwithfirebase;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {
    private Button login,register;
    private FirebaseUser firebaseUser;

    //========= onStart START =========//
    @Override
    protected void onStart() {
        super.onStart();

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 確認用戶是否為 null
        if(firebaseUser != null){ // 若已有用戶
            startActivity(new Intent(StartActivity.this, MainActivity.class)); // 跳轉到 MainActivity
            finish(); // 結束此頁
        }
    }
    //========= onStart END =========//

    //========= onCreate START =========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // 宣告元件
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);

        // 登入按鈕的監聽事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        // 註冊按鈕的監聽事件
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
    //========= onCreate END =========//
}