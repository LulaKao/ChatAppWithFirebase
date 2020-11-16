package com.quarterlife.chatappwithfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {
    private MaterialEditText email, password;
    private Button btn_login;
    private FirebaseAuth auth;

    //========= onCreate START =========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 設置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // 宣告 Toolbar
        setSupportActionBar(toolbar); // 設置使用 Toolbar
        getSupportActionBar().setTitle("Login"); // 設置 title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 添加返回按鈕

        // 取得 Firebase 的認證
        auth = FirebaseAuth.getInstance();

        // 宣告元件
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_login = findViewById(R.id.btn_login);

        // 登入按鈕的監聽事件
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){ // 若有欄位沒有填寫
                    Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();

                } else { // 若欄位都有填寫 --> 登入

                    // 用 E-mail 和 Password 登入
                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            // 新增登入完成的監聽事件
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){ // 若任務完成
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class); // 創建 Intent
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // 清掉舊的 Activity
                                        startActivity(intent); // 跳轉到 MainActivity
                                        finish(); // 結束 RegisterActivity

                                    } else { // 若任務失敗
                                        Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
    //========= onCreate END =========//
}