package com.quarterlife.chatappwithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText send_email;
    private Button btn_reset;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // 設置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // 宣告 Toolbar
        setSupportActionBar(toolbar); // 設置使用 Toolbar
        getSupportActionBar().setTitle("Reset Password"); // 設置 title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 添加返回按鈕

        // 宣告元件
        send_email = findViewById(R.id.send_email);
        btn_reset = findViewById(R.id.btn_reset);

        // 取得 Firebase 的認證
        firebaseAuth = FirebaseAuth.getInstance();

        // 重設密碼的監聽事件
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 取得使用者的 email
                String email = send_email.getText().toString();

                if(email.equals("")){ // 若使用者沒有輸入東西
                    Toast.makeText(ResetPasswordActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();

                } else { // 若使用者有輸入 email

                    // 發送重設密碼的 email
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){ // 若任務成功
                                Toast.makeText(ResetPasswordActivity.this, "Please check your Email", Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                finish(); // 結束此頁

                            } else { // 若任務失敗
                                String error = task.getException().getMessage(); // 取得錯誤訊息
                                Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_SHORT).show(); // 顯示錯誤訊息
                            }
                        }
                    });
                }
            }
        });
    }
}