package com.quarterlife.chatappwithfirebase.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.quarterlife.chatappwithfirebase.Adapter.UserAdapter;
import com.quarterlife.chatappwithfirebase.Model.Chatlist;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.Notifications.Token;
import com.quarterlife.chatappwithfirebase.R;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<Chatlist> usersList;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    //========= onCreateView START =========//
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // 設定 RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 創建 usersList
        usersList = new ArrayList<>();

        // 取得目前使用者的 Chatlist 參考
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());

        // 為目前使用者的 Chatlist 參考增添 ValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear(); // 清除 ArrayList

                // 跑迴圈取得 Chatlist 的資訊
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class); // 取得 Chatlist 的資訊
                    usersList.add(chatlist); // 添加 chatlist 的資訊到 usersList 裡
                }

                chatList(); // 設置 chatList
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // 更新 token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }
    //========= onCreateView END =========//

    //========= 更新 token START =========//
    private void updateToken(String token){
        // 取得 Tokens 的 database 參考
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        // 創建 Token
        Token token1 = new Token(token);
        // 設置 token 的值到 Tokens > firebaseUser.getUid() 裡
        reference.child(firebaseUser.getUid()).setValue(token1);
    }
    //========= 更新 token END =========//

    //========= 設置 chatList START =========//
    private void chatList(){
        // 創建 ArrayList
        mUsers = new ArrayList<>();
        // 取得使用者的資料庫參考
        reference = FirebaseDatabase.getInstance().getReference("Users");
        // 為使用者的資料庫參考添加 ValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear(); // 清空 ArrayList

                // 跑迴圈取得使用者資料
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class); // 取得使用者的資料

                    // 跑迴圈取得 Chatlist 的資料
                    for(Chatlist chatlist : usersList){
                        if(user.getId().equals(chatlist.getId())){ // 如果 user id 和 chatlist id 一樣
                            mUsers.add(user); // 把使用者資料加進 ArrayList
                        }
                    }
                }

                // 創建 UserAdapter
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                // 讓 recyclerView 綁定 UserAdapter
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 設置 chatList END =========//
}