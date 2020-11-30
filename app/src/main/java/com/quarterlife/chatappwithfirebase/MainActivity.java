package com.quarterlife.chatappwithfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quarterlife.chatappwithfirebase.Fragments.ChatsFragment;
import com.quarterlife.chatappwithfirebase.Fragments.ProfileFragment;
import com.quarterlife.chatappwithfirebase.Fragments.UsersFragment;
import com.quarterlife.chatappwithfirebase.Model.Chat;
import com.quarterlife.chatappwithfirebase.Model.User;
import java.util.ArrayList;
import java.util.HashMap;
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
        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 取得目前使用者的 Database 參考
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
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image); // 設置 image 到 profile_image 上
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // 取得聊天紀錄的資料庫參考
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        // 新增聊天紀錄的資料庫參考
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 創建 ViewPagerAdapter
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0; // 宣告變數

                // 跑迴圈取得聊天資料
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class); // 取得聊天資料

                    // 如果接收者是使用者自己，且訊息未讀
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIs_seen()){
                        unread++; // 未讀的訊息數 ++
                    }
                }

                if(unread == 0){ // 如果沒有未讀訊息
                    viewPagerAdapter.addFragment(new ChatsFragment(),"Chats"); // 新增 Chats Fragment
                } else { // 如果有未讀訊息
                    viewPagerAdapter.addFragment(new ChatsFragment(),"Chats (" + unread + ")"); // 新增 Chats Fragment
                }

                // 新增 Users 和 Profile Fragment
                viewPagerAdapter.addFragment(new UsersFragment(),"Users");
                viewPagerAdapter.addFragment(new ProfileFragment(),"Profile");
                // 綁定 ViewPagerAdapter 到 ViewPager 上
                viewPager.setAdapter(viewPagerAdapter);
                // 綁定 ViewPager 到 TabLayout 上
                tabLayout.setupWithViewPager(viewPager);
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
//                startActivity(new Intent(getApplicationContext(), StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)); // 跳到 StartActivity

                /*  Intent.FLAG_ACTIVITY_CLEAR_TOP：
                    銷毀目標 Activity 和它之上的所有 Activity，
                    並重新創建目標 Activity   */

                startActivity(new Intent(MainActivity.this, StartActivity.class)); // 跳到 StartActivity
                finish(); // 結束此頁

                return true;
        }

        return false;
    }
    //========= 設置 Menu END =========//

    //========= ViewPagerAdapter START =========//
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
    //========= ViewPagerAdapter END =========//

    //========= 設置使用者狀態 START =========//
    private void setStatus(String status){
        // 取得現在使用者的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>(); // 創建 HashMap
        hashMap.put("status", status); // 放置 [使用者的狀態] 到 HashMap 裡

        reference.updateChildren(hashMap); // 把 hashMap 的值設定給目前使用者的 Database 參考
    }
    //========= 設置使用者狀態 END =========//

    //========= onResume START =========//
    @Override
    protected void onResume() {
        super.onResume();
        setStatus("online");
    }
    //========= onResume END =========//

    //========= onPause START =========//
    @Override
    protected void onPause() {
        super.onPause();
        setStatus("offline");
    }
    //========= onPause END =========//
}