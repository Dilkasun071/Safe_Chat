package com.example.kasun.dilchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {
    //Realtime Firebase Database
    private DatabaseReference mDatabase;
    //Current User
    private FirebaseUser mcurrentuser;
    //ImageView,TextView,Button
    private ImageView imgView;
    private TextView name,status;
    private Button chngImg,chngSts;
    //For Image Retrive
    private static final int GALLERY_PICK = 1;
    //Strorage Firebase Database
    private StorageReference mStorageRef;
    //Progress Dialog
    private ProgressDialog mprogressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //Fields
        imgView = (ImageView)findViewById(R.id.imageView);
        name = (TextView)findViewById(R.id.setting_disname);
        status = (TextView)findViewById(R.id.setting_status);
        chngSts = (Button)findViewById(R.id.setting_chg_status);
        chngImg = (Button)findViewById(R.id.setting_chg_image);
        //Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //Current User
        mcurrentuser = FirebaseAuth.getInstance().getCurrentUser();
        //Current User Id
        String uid = mcurrentuser.getUid();
        //Database Reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.keepSynced(true);
        //Retrive Data
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Save Data into Variable
                String Sname = dataSnapshot.child("name").getValue().toString();
                String Sstatus = dataSnapshot.child("status").getValue().toString();
                final String Simg = dataSnapshot.child("image").getValue().toString();
                String Sthmub = dataSnapshot.child("thumb_image").getValue().toString();
                //Set Text
                name.setText(Sname);
                status.setText(Sstatus);
                //Set Image
                if(!Simg.equals("de")){
                    Picasso.with(SettingActivity.this).load(Simg)
                       .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.de)
                       .into(imgView, new Callback() {
                       @Override
                       public void onSuccess() {
                       }
                       @Override
                       public void onError() {
                          Picasso.with(SettingActivity.this).load(Simg).placeholder(R.drawable.de)
                             .into(imgView);
                       }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //Status Activity Clicked
        chngSts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status_value = status.getText().toString();
                Intent in = new Intent(SettingActivity.this,StatusActivity.class);
                in.putExtra("status_value",status_value);
                startActivity(in);
            }
        });
        //Gallery Activity Clicked
        chngImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            // To start a image picker, pass null as imageUri
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                //Progress Bar
                mprogressbar = new ProgressDialog(this);
                mprogressbar.setTitle("Loading User Data");
                mprogressbar.setMessage("Please Wait");
                mprogressbar.show();

                //URL
                Uri resultUri = result.getUri();

                //image Comprese
                File thum_filePath = new File(resultUri.getPath());
                Bitmap thum_bitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thum_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thum_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thum_byte = baos.toByteArray();
                //Current User id
                String current_user_id = mcurrentuser.getUid();
                //Storage
                StorageReference filepath = mStorageRef.child("profile_pictures").child(current_user_id+" .jpg");
                final StorageReference thub_filepath = mStorageRef.child("profile_pictures").child("thumbs").child(current_user_id+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thub_filepath.putBytes(thum_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thum_task){
                                    String thum_downloader = thum_task.getResult().getDownloadUrl().toString();
                                    if(thum_task.isSuccessful()){
                                        Map update_hashmap = new HashMap<>();
                                        update_hashmap.put("image",download_url);
                                        update_hashmap.put("thumb_image",thum_downloader);
                                        mDatabase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    mprogressbar.dismiss();
                                                    Toast.makeText(SettingActivity.this,"Success Uploading",Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SettingActivity.this,"Something Going Wrong Thumbnail",Toast.LENGTH_LONG).show();
                                        mprogressbar.dismiss();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(SettingActivity.this,"Something Going Wrong",Toast.LENGTH_LONG).show();
                            mprogressbar.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
