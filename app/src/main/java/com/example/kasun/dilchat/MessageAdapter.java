package com.example.kasun.dilchat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by kasun on 3/24/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> messagesList;
    private FirebaseAuth fbAuth;
    private DatabaseReference mRoofRef;
    private final String[] nameUser = new String[1];
    private String current_user_id;
    public MessageAdapter(List<Messages> messagesList){
        this.messagesList = messagesList;
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        fbAuth = FirebaseAuth.getInstance();
        mRoofRef = FirebaseDatabase.getInstance().getReference();
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);


    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        current_user_id = fbAuth.getCurrentUser().getUid();
        Messages c = messagesList.get(position);
        String from_user = c.getFrom();
        String message_type = c.getType();
        mRoofRef = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mRoofRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                holder.name.setText(name);
                Picasso.with(holder.image.getContext()).load(image).placeholder(R.drawable.de).into(holder.image);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if(message_type.equals("text")){
            holder.messageText.setText(c.getMessage());
            holder.image.setVisibility(View.INVISIBLE);
        }else{
          holder.messageText.setVisibility(View.INVISIBLE);
          Picasso.with(holder.image.getContext()).load(c.getMessage()).placeholder(R.drawable.de).into(holder.image);
        }
        //holder.messageText.setText(c.getMessage());
        //holder.name.setText(current_user_id);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public TextView timeText;
        public TextView name;
        public ImageView image;

        public MessageViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            timeText = (TextView) view.findViewById(R.id.message_time_layout);
            name = (TextView) view.findViewById(R.id.message_disname_layout);
            image = (ImageView) view.findViewById(R.id.message_imae_layout);

        }
    }
}
