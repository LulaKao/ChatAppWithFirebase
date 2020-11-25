package com.quarterlife.chatappwithfirebase.Notifications;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        // 取得目前的使用者
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // 取得 token
        String refreshToken = FirebaseInstanceId.getInstance().getToken();

        if(firebaseUser != null){ // 如果 firebaseUser 不為 null
            updateToken(refreshToken); // 更新 token
        }
    }

    //========= 更新 token START =========//
    private void updateToken(String refreshToken) {
        // 取得目前的使用者
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // 取得 Tokens 的 database 參考
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        // 創建 token
        Token token = new Token(refreshToken);
        // 設置 token 的值到 Tokens > firebaseUser.getUid() 的 database 參考
        reference.child(firebaseUser.getUid()).setValue(token);
    }
    //========= 更新 token END =========//
}
