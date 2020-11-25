package com.quarterlife.chatappwithfirebase.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quarterlife.chatappwithfirebase.MessageActivity;
import com.quarterlife.chatappwithfirebase.Model.Chat;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.R;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    private String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername()); // 設置 username

        // 設置 profile_image
        if(user.getImageURL().equals("default")){ // 若沒有圖
            holder.profile_image.setImageResource(R.mipmap.ic_launcher); // 設置預設圖
        } else { // 若有圖
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image); // 設置使用者的大頭照
        }

        if(isChat){ // 如果是 ChatsFragment --> 顯示用戶是否上線 + 顯示最後一筆對話紀錄

            // 顯示用戶是否上線
            if(user.getStatus().equals("online")){ // 如果使用者在線上
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else { // 如果使用者沒在線上
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }

            // 顯示最後一筆對話紀錄
            lastMessage(user.getId(), holder.last_msg);

        } else { // 如果是 UsersFragment --> 不用顯示用戶是否上線 + 不用顯示最後一筆對話紀錄
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
            holder.last_msg.setVisibility(View.GONE);
        }

        // 設置 itemView 的 OnClickListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 跳轉到 MessageActivity
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId()); // 把點擊的 user id 傳到 MessageActivity
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username, last_msg;
        public ImageView profile_image, img_on, img_off;

        public ViewHolder(View itemView){
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    //========= 顯示最後一筆對話紀錄 START =========//
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default"; // 預設 theLastMessage 為 default
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // 取得目前的使用者
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats"); // 取得聊天紀錄的資料庫參考

        // 讓聊天紀錄的資料庫參考新增 ValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 跑迴圈取得聊天資料
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class); // 取得聊天資訊

                    // 如果有對話紀錄
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())){
                        theLastMessage = chat.getMessage(); // 把聊天內容設置到 theLastMessage 上
                    }
                }

                if(theLastMessage.equals("default")){ // 如果沒有最後一筆聊天紀錄
                    last_msg.setText("No Message"); // 顯示 No Message
                } else { // 如果有最後一筆聊天紀錄
                    last_msg.setText(theLastMessage); // 顯示最後一筆聊天紀錄
                }

                theLastMessage = "default"; // 重設 theLastMessage 為 default
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //========= 顯示最後一筆對話紀錄 END =========//
}
