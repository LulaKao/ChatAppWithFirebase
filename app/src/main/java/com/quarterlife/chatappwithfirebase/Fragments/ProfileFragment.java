package com.quarterlife.chatappwithfirebase.Fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private CircleImageView image_profile;
    private TextView username;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 宣告元件
        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);

        // 取得上傳的存儲參考
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // 取得目前的使用者
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // 取得目前的使用者的 Database 參考
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        // 讓目前的使用者的 Database 參考 addValueEventListener
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class); // 取得 User 的資訊
                username.setText(user.getUsername()); // 設置使用者的名稱

                // 設置使用者的大頭照
                if(user.getImageURL().equals("default")){ // 如果是 default
                    image_profile.setImageResource(R.mipmap.ic_launcher); // 設置預設圖像
                } else { // 如果有 image
                    Glide.with(getContext()).load(user.getImageURL()).into(image_profile); // 設置 image 到 profile_image 上
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}