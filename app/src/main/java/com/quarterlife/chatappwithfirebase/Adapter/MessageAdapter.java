package com.quarterlife.chatappwithfirebase.Adapter;

import android.content.Context;
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
import com.quarterlife.chatappwithfirebase.Model.Chat;
import com.quarterlife.chatappwithfirebase.R;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChat;
    private String imageUrl;
    private FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageUrl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage()); // 設置訊息

        // 設置 profile_image
        if(imageUrl.equals("default")){ // 若沒有圖
            holder.profile_image.setImageResource(R.mipmap.ic_launcher); // 設置預設圖
        } else { // 若有圖
            Glide.with(mContext).load(imageUrl).into(holder.profile_image); // 設置聊天對象的大頭照
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView profile_image;

        public ViewHolder(View itemView){
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // 選擇要顯示什麼 ItemViewType
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){ // 如果是使用者自己
            return MSG_TYPE_RIGHT; // 顯示右邊的 type
        } else { // 非使用者自己（聊天的對象）
            return MSG_TYPE_LEFT; // 顯示左邊的 type
        }
    }
}
