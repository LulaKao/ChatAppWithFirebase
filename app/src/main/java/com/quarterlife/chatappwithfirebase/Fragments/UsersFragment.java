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
import com.quarterlife.chatappwithfirebase.Adapter.UserAdapter;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.R;
import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    //========= onCreateView START =========//
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // 設定 recyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        /*  當我們確定 Item 的改變不會影響 RecyclerView 的寬高的時候，
            可以設置 setHasFixedSize(true)，並通過 Adapter 的增刪改查方法去刷新 RecyclerView，
            而不是通過 notifyDataSetChanged()。
            （其實可以直接設置爲 true，當需要改變寬高的時候就用 notifyDataSetChanged() 去整體刷新一下）

            增刪改查方法：
            onItemRangeChanged()
            onItemRangeInserted()
            onItemRangeRemoved()
            onItemRangeMoved()  */

        mUsers = new ArrayList<>();

        // 讀取使用者
        readUsers();

        return view;
    }
    //========= onCreateView END =========//

    //========= 讀取使用者 START =========//
    private void readUsers() {
        // 取得目前的使用者
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // 取得所有使用者的 Database 參考
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        // addValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 清除 List<User>
                mUsers.clear();

                // 跑迴圈取得 User 的資訊
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class); // 取得 User 的資訊

                    /*  assert：java 關鍵字，表示斷言
                    *   如果 <boolean表示式> 為 true，則程式繼續執行。
                    *   如果為 false，則程式丟擲 AssertionError，並終止執行。  */
                    assert user != null;
                    assert firebaseUser != null;

                    // 排除使用者自己
                    if(!user.getId().equals(firebaseUser.getUid())){ // 若非用戶自己
                        mUsers.add(user); // 把使用者資料加到 List 裡
                    }
                }

                // 創建 UserAdapter
                userAdapter = new UserAdapter(getContext(), mUsers, false);

                // 綁定適配器並呈現在 recyclerView 上
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 讀取使用者 END =========//
}