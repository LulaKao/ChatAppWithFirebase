package com.quarterlife.chatappwithfirebase.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.quarterlife.chatappwithfirebase.Model.User;
import com.quarterlife.chatappwithfirebase.R;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private CircleImageView image_profile;
    private TextView username;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    //========= onCreateView START =========//
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

        // 設置 image_profile 的點擊監聽事件
        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage(); // 打開 Gallery 顯示所有照片
            }
        });

        return view;
    }
    //========= onCreateView END =========//

    //========= 打開 Gallery 顯示所有照片 START =========//
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // image 資料夾下的所有照片
        intent.setAction(Intent.ACTION_GET_CONTENT); // 取得內容
        startActivityForResult(intent, IMAGE_REQUEST); // 打開 Gallery
    }
    //========= 打開 Gallery 顯示所有照片 END =========//

    //========= 在 onActivityResult 中接收回調 START =========//
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){ // 若有成功選擇 image
            imageUri = data.getData(); // 取得圖片的路徑

            if(uploadTask != null && uploadTask.isInProgress()){ // uploadTask 正在執行
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show(); // 顯示正在上傳的訊息
            } else { // uploadTask 沒有在執行
                uploadImage(); // 上傳照片
            }
        }
    }
    //========= 在 onActivityResult 中接收回調 END =========//

    //========= 上傳照片 START =========//
    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext()); // 創建 ProgressDialog
        pd.setMessage("Uploading"); // 顯示正在上傳的訊息
        pd.show(); // 顯示 ProgressDialog

        if(imageUri != null){ // 若圖片的路徑不為 null

            // 檔案的存儲參考 = Firebase 的存儲參考 + 現在的時間 + 檔案副檔名
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            /*  imageUri = content://com.android.providers.media.documents/document/image%3A90
            *   storageReference = gs://chatappwithfirebase-1f46d.appspot.com/uploads
            *   fileReference = gs://chatappwithfirebase-1f46d.appspot.com/uploads/1605859595573.jpg    */

            // 創建 StorageTask
            uploadTask = fileReference.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() { // 繼續執行任務
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){ // 如果任務沒有成功
                        throw task.getException(); // 拋出訊息
                    }

                    /*  fileReference.getDownloadUrl() = com.google.android.gms.tasks.zzu@3e56ff8   */
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() { // 新增任務完成的監聽事件
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){ // 如果任務成功
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        /*  downloadUri = https://firebasestorage.googleapis.com/v0/b/chatappwithfirebase-1f46d.appspot.com/o/uploads%2F1605862087898.jpg?alt=media&token=a747ad6a-2ec5-4321-b606-ba184c91ae6b
                        *   mUri = https://firebasestorage.googleapis.com/v0/b/chatappwithfirebase-1f46d.appspot.com/o/uploads%2F1605862087898.jpg?alt=media&token=a747ad6a-2ec5-4321-b606-ba184c91ae6b    */

                        // 取得目前使用者的 Database 參考
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>(); // 創建 HashMap
                        hashMap.put("imageURL", mUri); // 放置圖片的網址到 HashMap 裡
                        reference.updateChildren(hashMap); // 把 hashMap 的值設定給目前使用者的 Database 參考

                        /*  'setValue' method is totally replacing the document (specified reference) with new data.
                            'updateChildren' method is just updating particular fields or add such fields if they did not exist before. */

                    } else { // 如果任務失敗
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show(); // 顯示失敗的訊息
                    }

                    pd.dismiss(); // 關閉 ProgressDialog
                }
            }).addOnFailureListener(new OnFailureListener() { // 新增任務失敗的監聽事件
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show(); // 顯示錯誤訊息
                    pd.dismiss(); // 關閉 ProgressDialog
                }
            });

        } else { // 若圖片的路徑為 null
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show(); // 顯示沒有選擇圖片的訊息
        }
    }
    //========= 上傳照片 END =========//

    //========= 獲取文件副檔名（文件類型） START =========//
    private String getFileExtension(Uri uri){

        /*  ContentProvider（內容提供者）是負責組織應用程式的資料。
         *   ContentResolver（內容解析器）是通過 ContentProvider 來獲取與其他應用程式共享的資料。*/

        // 創建並取得 ContentResolver
        ContentResolver contentResolver = getContext().getContentResolver();
        // 利用 MimeTypeMap 判斷檔案為何種格式
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        // 返回文件擴展名所對應的 Mime 類型（副檔名）
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    //========= 獲取文件副檔名（文件類型） END =========//
}