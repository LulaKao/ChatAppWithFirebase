package com.quarterlife.chatappwithfirebase.Fragments;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.quarterlife.chatappwithfirebase.Adapter.UserAdapter;
import com.quarterlife.chatappwithfirebase.Model.Chat;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.R;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<Object> usersList;
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
        usersList = new ArrayList<Object>();

        // 取得聊天內容的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Chats");

        // 聊天內容的 Database 參考 addValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear(); // 清空 List<String>

                // 跑迴圈取得聊天資訊
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class); // 取得聊天資訊

                    // 把跟使用者有聊天過的對象都加到 usersList 裡
                    if(chat.getSender().equals(firebaseUser.getUid())){ // 如果發送者是使用者自己
                        usersList.add(chat.getReceiver()); // 把接收者加到 usersList 裡
                    }
                    if(chat.getReceiver().equals(firebaseUser.getUid())){ // 如果接收者是使用者自己
                        usersList.add(chat.getSender()); // 把發送者加到 usersList 裡
                    }
                }

                // usersList 排除重複的對象
                usersList = usersList.stream().distinct().collect(Collectors.toList());
                // 讀取聊天對象
                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
    //========= onCreateView END =========//

    //========= 讀取聊天對象 START =========//
    private void readChats() {
        mUsers = new CopyOnWriteArrayList<>(); // 創建 ArrayList

        // 取得所有使用者的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Users");

        // 新增 Database 的 ValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear(); // 清空 List<User>

                // 跑迴圈取得使用者資訊
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class); // 取得使用者資訊

                    for(Object id : usersList){ // 跑迴圈取得 usersList
                        if(user.getId().equals(id)){ // 若現在取得的使用者資訊是有和目前使用者聊天的對象
                            mUsers.add(user); // 增加該使用者到 List<User>
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers, true); // 創建 UserAdapter
                recyclerView.setAdapter(userAdapter); // 綁定 userAdapter 到 recyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 讀取聊天對象 END =========//
}