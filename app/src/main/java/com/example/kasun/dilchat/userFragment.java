package com.example.kasun.dilchat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class userFragment extends Fragment {
    //Recycle List
    private RecyclerView mFriendList;
    //Database Reference
    private DatabaseReference dbRef;
    private DatabaseReference dbRef1;
    //Firebase Auth
    private FirebaseAuth fbAuth;
    //Current User ID
    private String mcurrent_user_id;
    //Image
    private String image;
    private View mView;
    public userFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context ctx;

        mView = inflater.inflate(R.layout.fragment_user, container, false);
        //Recycle List
        mFriendList = (RecyclerView) mView.findViewById(R.id.user_list);
        //Firebase Author
        fbAuth = FirebaseAuth.getInstance();
        //Current User ID
        mcurrent_user_id = fbAuth.getCurrentUser().getUid();
        //Database Reference
        dbRef = FirebaseDatabase.getInstance().getReference().child("Friend").child(mcurrent_user_id);
        dbRef1 = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendList.setHasFixedSize(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        dbRef.keepSynced(true);dbRef1.keepSynced(true);
        return mView;

    }

    @Override
    public void onStart() {
        super.onStart();
        //Adapter
        FirebaseRecyclerAdapter<Friends,UsersViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, UsersViewHolder>(
                        //Getter Setter class
                        Friends.class,
                        //layout
                        R.layout.users_single_layout,
                        //Static Class
                        UsersViewHolder.class,
                        //Database Reference
                        dbRef
                ){
                    @Override
                    protected void populateViewHolder(final UsersViewHolder viewHolder, final Friends model, int position) {
                        //setDate Method
                        viewHolder.setDate(model.getDate());
                        //If Click User
                        final String list_user_id = getRef(position).getKey();
                        dbRef1.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                                viewHolder.setUserImage(userThumb,getContext());
                                if(dataSnapshot.hasChild("online") ){
                                    String userOnline =  dataSnapshot.child("online").getValue().toString();
                                    viewHolder.setUserOnline(userOnline);
                                }
                                final String userName = dataSnapshot.child("name").getValue().toString();
                                viewHolder.setName(userName);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                        //If Click On Friend
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0) {
                                            Intent profile = new Intent(getContext(), ProfileActivity.class);
                                            profile.putExtra("user_id", list_user_id);
                                            startActivity(profile);
                                        }
                                        if (i == 1) {
                                            Intent profile1 = new Intent(getContext(), chatActivity.class);
                                            profile1.putExtra("user_id", list_user_id);
                                            //profile1.putExtra("username", userName);
                                            startActivity(profile1);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                        //end
                    }
                };
        mFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView userStatusview = (TextView)mView.findViewById(R.id.usi_status);
            userStatusview.setText(date);
        }

        public void setName(String name){
            TextView username = (TextView)mView.findViewById(R.id.usi_disname);
            username.setText(name);
        }

        public void setUserOnline(String userOnline) {
            ImageView useronline = (ImageView)mView.findViewById(R.id.user_online);
            if(userOnline == "true"){
                useronline.setVisibility(View.VISIBLE);
            }else{
                useronline.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserImage(String userThumb, Context context) {
            ImageView img = (ImageView) mView.findViewById(R.id.user_imageid);
            Picasso.with(context).load(userThumb).placeholder(R.drawable.de).into(img);

        }
    }
}
