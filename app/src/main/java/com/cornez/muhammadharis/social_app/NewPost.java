package com.cornez.muhammadharis.social_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NewPost extends AppCompatActivity {
    private Toolbar Nav_Toolbar;

    private ImageButton postImage;
    private Uri selectedImage;

    private EditText statusText,picstatusText;
    private static final int Gallery_request =1;
    private String Status_Description,download_selectURI;

    private FirebaseAuth userAuth;
    private DatabaseReference UserReference, PostReference;
    private StorageReference postImageReference;
    private String currentUID,CurrentTime,CurrentDate,RandomNumber;

    private ProgressDialog progressDialog;
    private ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        SettingUpCustomToolbaar();

        userAuth    = FirebaseAuth.getInstance();
        currentUID  = userAuth.getCurrentUser().getUid();
        UserReference  = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        PostReference  = FirebaseDatabase.getInstance().getReference().child("Users Posts");

        postImageReference= FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        pbar = findViewById(R.id.NEWPOST_prograssbar);

        postImage = findViewById(R.id.NewPOST_imagePost_ID);
        statusText = findViewById(R.id.NewPOST_statusPOST_ID);
        picstatusText = findViewById(R.id.NewPOST_Pic_statusPOST_ID);

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,Gallery_request);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        pbar.setVisibility(View.VISIBLE);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode){
                case Gallery_request:
                     selectedImage = data.getData();
                     Picasso.get().load(selectedImage).placeholder(R.drawable.profile).into(postImage);
                     pbar.setVisibility(View.GONE);
                     break;
            }
            pbar.setVisibility(View.GONE);
        }
        else
            pbar.setVisibility(View.GONE);
    }

    public void Status_Post_BTN_CLICK(View view){
        Status_Description = statusText.getText().toString();
        if(Status_Description.isEmpty())
            Toast.makeText(this, "Please write something in your status...", Toast.LENGTH_SHORT).show();
        else {
            progressDialog.setTitle("Posting");
            progressDialog.setMessage("Please Wait, While we are updating Post...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            Calendar ForDate = Calendar.getInstance();
            SimpleDateFormat NowDate = new SimpleDateFormat("yyyy-MM-dd");
            CurrentDate = NowDate.format(ForDate.getTime());

            Calendar ForTime = Calendar.getInstance();
            SimpleDateFormat NowTime = new SimpleDateFormat("HH:mm:ss");
            CurrentTime = NowTime.format(ForTime.getTime());

            RandomNumber = CurrentDate + CurrentTime;

            //upload status only
            UserReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        String full_Name = dataSnapshot.child("fullname").getValue().toString();
                        String profile_Image_Link;
                        if (dataSnapshot.hasChild("profileimage"))
                            profile_Image_Link = dataSnapshot.child("profileimage").getValue().toString();
                        else
                            profile_Image_Link = "https://firebasestorage.googleapis.com/v0/b/socialaap-29a1c.appspot.com/o/profile.png?alt=media&token=c1c0907a-9173-483e-9f4b-be3746b37bf1";

                        HashMap user_POST = new HashMap();
                            //Post Related Data
                        user_POST.put("uid", currentUID);
                        user_POST.put("time", CurrentTime);
                        user_POST.put("date", CurrentDate);
                        user_POST.put("fullname", full_Name);
                        user_POST.put("description", Status_Description);
                        user_POST.put("profileimage", profile_Image_Link);
                        user_POST.put("postimage", "no");
                        user_POST.put("update", "Posted At");

                        //Now uplolad the post details to Firebase Database with the storage link of the post
                        PostReference.child(RandomNumber +currentUID ).updateChildren(user_POST)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(NewPost.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                            SendUsertoHomeActivity();
                                        } else {
                                            Toast.makeText(NewPost.this, "Error Occured" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    public void Post_BTN_CLICK(View view){
        Status_Description = picstatusText.getText().toString();

        if(selectedImage == null)
            Toast.makeText(this, "Please select post Picture...", Toast.LENGTH_SHORT).show();
        else if(Status_Description.isEmpty())
            Toast.makeText(this, "Please say something about your Picture...", Toast.LENGTH_SHORT).show();
        else {
            progressDialog.setTitle("Posting");
            progressDialog.setMessage("Please Wait, While we are updating Post...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);


            Calendar ForDate = Calendar.getInstance();
            SimpleDateFormat NowDate = new SimpleDateFormat("yyyy-MM-dd");
            CurrentDate = NowDate.format(ForDate.getTime());

            Calendar ForTime = Calendar.getInstance();
            SimpleDateFormat NowTime = new SimpleDateFormat("HH:mm:ss");
            CurrentTime = NowTime.format(ForTime.getTime());

            RandomNumber = CurrentDate + CurrentTime;

            final StorageReference userPost = postImageReference.child("Post Images").child(selectedImage.getLastPathSegment() + RandomNumber + ".jpg");
                //Upload Picture Status
            userPost.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewPost.this, "Picture uploaded successfully to Storage...", Toast.LENGTH_SHORT).show();

                        //Get Profile Address from firebase storage
                        userPost.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                download_selectURI = uri.toString();    //storage link of post

                                //get current user fullname and profile image link
                                UserReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String full_Name = dataSnapshot.child("fullname").getValue().toString();
                                            String profile_Image_Link;
                                            if (dataSnapshot.hasChild("profileimage"))
                                                profile_Image_Link = dataSnapshot.child("profileimage").getValue().toString();
                                            else
                                                profile_Image_Link = "https://firebasestorage.googleapis.com/v0/b/socialaap-29a1c.appspot.com/o/profile.png?alt=media&token=c1c0907a-9173-483e-9f4b-be3746b37bf1";
                                            HashMap user_POST = new HashMap();
                                                //Post Related Data
                                            user_POST.put("uid", currentUID);
                                            user_POST.put("time", CurrentTime);
                                            user_POST.put("date", CurrentDate);
                                            user_POST.put("fullname", full_Name);
                                            user_POST.put("description", Status_Description);
                                            user_POST.put("profileimage", profile_Image_Link);
                                            user_POST.put("postimage", download_selectURI);
                                            user_POST.put("postURIpath", selectedImage.getLastPathSegment()+CurrentDate+CurrentTime);
                                            user_POST.put("update", "Posted At");

                                                 //Now uplolad the post details to Firebase Database with the storage link of the post
                                            PostReference.child(  RandomNumber + currentUID).updateChildren(user_POST)
                                                    .addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(NewPost.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
                                                                SendUsertoHomeActivity();
                                                            } else {
                                                                Toast.makeText(NewPost.this, "Error Occured" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(NewPost.this, "Error occured: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }



        //Toolbar
    private void SettingUpCustomToolbaar() {
        //Set Bar on the top and set name
        Nav_Toolbar = (Toolbar) findViewById(R.id.NewPOST_Custom_Toolbar);
        setSupportActionBar(Nav_Toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Post");
        Nav_Toolbar.setTitleTextColor(getResources().getColor(R.color.white));
    }


        //Get to home activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            SendUsertoHomeActivity();
        return super.onOptionsItemSelected(item);
    }

    private void SendUsertoHomeActivity() {
        Intent Home = new Intent(NewPost.this,MainActivity.class);
        startActivity(Home);
    }
}
