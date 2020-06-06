package com.cornez.muhammadharis.social_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClickOnPost extends AppCompatActivity {

    private DatabaseReference ClickPostReference;

    private TextView Date_text,Time_text,Description_text,Fullname_text;
    private ImageView PostImage_ID;
    private CircleImageView ProfileImage_id;

    private FirebaseAuth userAuth;
    private StorageReference userPost;
    private String currentUID="ABCD",userIDPOST ="ABCD",postImageURIpath;

    private String date,time;
    private ProgressDialog progressDialog;

    private Button EditPost_ID,Delete_Post_Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_on_post);

            //Firebase Intialization
        userAuth = FirebaseAuth.getInstance();
        currentUID = userAuth.getCurrentUser().getUid();
        userPost = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);


        Date_text = findViewById(R.id.ClickPOST_Date_ID);
        Time_text = findViewById(R.id.ClickPOST_time_ID);
        Description_text = findViewById(R.id.ClickPOST_Description_ID);
        PostImage_ID = findViewById(R.id.ClickPOST_postImage_ID);
        ProfileImage_id = findViewById(R.id.ClickPOST_postProfileImage_ID);
        Fullname_text = findViewById(R.id.ClickPOST_Fullname_ID);

        EditPost_ID = findViewById(R.id.ClickPost_EDitBtn_ID);
        Delete_Post_Id = findViewById(R.id.ClickPost_DelPostBtn_ID);
        EditPost_ID.setVisibility(View.GONE);
        Delete_Post_Id.setVisibility(View.GONE);

        String Postkey = getIntent().getExtras().get("postkey").toString();
        ClickPostReference = FirebaseDatabase.getInstance().getReference().child("Users Posts").child(Postkey);

       GetPostDetails();
    }

    private void GetPostDetails() {
        ClickPostReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userIDPOST = dataSnapshot.child("uid").getValue().toString();

                    date =dataSnapshot.child("date").getValue().toString();
                    time =dataSnapshot.child("time").getValue().toString();
                    Date_text.setText(date);
                    Time_text.setText(time);
                    Fullname_text.setText(dataSnapshot.child("fullname").getValue().toString());
                    Description_text.setText(dataSnapshot.child("description").getValue().toString());
                    ((TextView) findViewById(R.id.ClickPOST_simpletext_ID)).setText(dataSnapshot.child("update").getValue().toString());

                    if(dataSnapshot.hasChild ("postURIpath"))
                        postImageURIpath =dataSnapshot.child("postURIpath").getValue().toString();

                    if (dataSnapshot.hasChild("postimage"))
                        Picasso.get().load(dataSnapshot.child("postimage").getValue().toString()).into(PostImage_ID);
                    else
                        PostImage_ID.getLayoutParams().height=0;
                    if (dataSnapshot.hasChild("profileimage"))
                        Picasso.get().load(dataSnapshot.child("profileimage").getValue().toString()).placeholder(R.drawable.profile).into(ProfileImage_id);

                    if(currentUID.equals(userIDPOST)) {
                        EditPost_ID.setVisibility(View.VISIBLE);
                        Delete_Post_Id.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void EditYourPost(View view){

        final AlertDialog.Builder updateAlert = new AlertDialog.Builder(ClickOnPost.this);
        updateAlert.setTitle("Edit Post");

        final EditText Inputupdate = new EditText(ClickOnPost.this);
        Inputupdate.setText(Description_text.getText().toString());
        updateAlert.setView(Inputupdate);

        final String CurrentDate,CurrentTime;
        Calendar ForDate = Calendar.getInstance();
        SimpleDateFormat NowDate = new SimpleDateFormat("yyyy-MM-dd");
        CurrentDate = NowDate.format(ForDate.getTime());

        Calendar ForTime = Calendar.getInstance();
        SimpleDateFormat NowTime = new SimpleDateFormat("HH:mm:ss");
        CurrentTime = NowTime.format(ForTime.getTime());


        updateAlert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.setTitle("Editing");
                progressDialog.setMessage("Please Wait, While we are Editing Post...");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                ClickPostReference.child("time").setValue(CurrentTime);
                ClickPostReference.child("date").setValue(CurrentDate);
                ClickPostReference.child("description").setValue(Inputupdate.getText().toString());
                ClickPostReference.child("update").setValue("Edited at");

                progressDialog.dismiss();
                SendUserToHomeActivity();
                Toast.makeText(ClickOnPost.this, "Your Post has been Edited successfully.", Toast.LENGTH_SHORT).show();

            }
        });

        updateAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = updateAlert.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_purple);
    }
    public void DeleteYourPost(View view){
        final AlertDialog.Builder updateAlert = new AlertDialog.Builder(ClickOnPost.this);
        updateAlert.setTitle("Delete Post");

        updateAlert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    //Remove From Firebase Realtime
                ClickPostReference.removeValue();

                    //Remove From Firbase Storage
                String RandomNumber = date + time;
                userPost = userPost.child("Post Images").child(postImageURIpath + ".jpg");
                userPost.delete();

                SendUserToHomeActivity();
                Toast.makeText(ClickOnPost.this,"Your Post has been deleted",Toast.LENGTH_SHORT).show();
            }
        });

        updateAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = updateAlert.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_purple);
    }

    public void SendUserToHomeActivity(){
        Intent Home = new Intent(ClickOnPost.this,MainActivity.class);
        Home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Home);
        finish();
    }
}
