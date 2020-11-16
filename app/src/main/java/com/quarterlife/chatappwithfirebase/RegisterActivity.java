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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private MaterialEditText username, email, password;
    private Button btn_register;
    private FirebaseAuth auth; // Firebase 認證
    private DatabaseReference reference; // Database 參考

    //========= onCreate START =========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 設置 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // 宣告 Toolbar
        setSupportActionBar(toolbar); // 設置使用 Toolbar
        getSupportActionBar().setTitle("Register"); // 設置 title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 添加返回按鈕

        // 宣告元件
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);

        // 註冊按鈕的監聽事件
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if(TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) { // 若有欄位沒有填寫
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();

                } else if (txt_password.length() < 6) { // 若密碼小於六位
                    Toast.makeText(RegisterActivity.this, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();

                } else { // 若以上兩個條件都有達成 --> 去註冊
                    register(txt_username, txt_email, txt_password);
                }
            }
        });
    }
    //========= onCreate END =========//

    //========= 註冊 START =========//
    private void register(final String username, String email, String password){
        // 用 Email 和 Password 創建用戶
        auth.createUserWithEmailAndPassword(email, password)
                // 新增註冊完成的監聽事件
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ // 若任務完成
                            FirebaseUser firebaseUser = auth.getCurrentUser(); // 取得 user
                            String user_id = firebaseUser.getUid(); // 取得 user id

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(user_id); // 取得 Database 的參考

                            HashMap<String, String> hashMap = new HashMap<>(); // 創建 HashMap
                            hashMap.put("id", user_id); // 放置 id 到 HashMap 裡
                            hashMap.put("username", username); // 放置 user name 到 HashMap 裡
                            hashMap.put("imageURL", "default"); // 放置 image URL 到 HashMap 裡

                            // 設置值到 Database 的參考裡，並新增完成的監聽事件
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){ // 若任務完成
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class); // 創建 Intent
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // 清掉舊的 Activity
                                        startActivity(intent); // 跳轉到 MainActivity
                                        finish(); // 結束 RegisterActivity

                                        /*  Intent.FLAG_ACTIVITY_CLEAR_TASK：
                                        這個 Flag 能造成在新的 Activity 啟動前，與舊的 Activity 相關聯的任務被清空。
                                        意即，新的 Activity 成為新任務的根，舊的 Activity 都被結束了。
                                        FLAG_ACTIVITY_CLEAR_TASK 只能與 FLAG_ACTIVITY_NEW_TASK 一起使用。   */
                                    }
                                }
                            });

                        } else { // 若任務失敗
                            Toast.makeText(RegisterActivity.this, "You can't register with this e-mail or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    //========= 註冊 END =========//
}