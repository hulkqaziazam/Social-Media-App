package com.cornez.muhammadharis.social_app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {
    private DatabaseReference UserDetailReference;

    private EditText Username_Text,FullNAME_Text,DOB_Text;
    private EditText Country_Text,Gender_Text,RelationShip_Text,Status_text;
    private CircleImageView image_Profile;

    private FirebaseAuth userAuth;
    private StorageReference UserProfileReference;
    private String currentUID="ABCD",userIDPOST ="ABCD",postImageURIpath;

    private String date,time;
    private ProgressDialog progressDialog;

    private Button EditPost_ID,Delete_Post_Id;

    private int mYear,mMonth,mDay;

    private String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};

    private static final int Gallery_request =1;
    private String downloadUrl=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

            //Firebase Intialization
        userAuth = FirebaseAuth.getInstance();
        currentUID = userAuth.getCurrentUser().getUid();
        UserProfileReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        progressDialog = new ProgressDialog(this);

        Username_Text = findViewById(R.id.Settings_username_id);
        FullNAME_Text = findViewById(R.id.Settings_Fullname_id);
        Country_Text  = findViewById(R.id.Setting_Country_ID);
        Gender_Text  = findViewById(R.id.Settings_Gender_id);
        RelationShip_Text  = findViewById(R.id.Settings_Relationship_id);
        image_Profile = findViewById(R.id.Settings_profile);
        DOB_Text = findViewById(R.id.Settings_DOB);
        Status_text = findViewById(R.id.Settings_status);

        UserDetailReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
                //Get Currrent Details
        UserDetailReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage"))
                        Picasso.get().load(dataSnapshot.child("profileimage").getValue().toString()).placeholder(R.drawable.profile).into(image_Profile);

                    Country_Text.setText(dataSnapshot.child("country").getValue().toString());
                    DOB_Text.setText(dataSnapshot.child("dob").getValue().toString());
                    Gender_Text.setText(dataSnapshot.child("gender").getValue().toString());
                    RelationShip_Text.setText(dataSnapshot.child("relationship").getValue().toString());
                    Username_Text.setText(dataSnapshot.child("username").getValue().toString());
                    FullNAME_Text.setText(dataSnapshot.child("fullname").getValue().toString());
                    Status_text.setText(dataSnapshot.child("status").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Click on Image
        image_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(Settings.this,image_Profile);
                popup.getMenuInflater().inflate(R.menu.popup_menu,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals("Camera")){
                            Toast.makeText(Settings.this, "Camera Coding Not Working...", Toast.LENGTH_SHORT).show();
                        }
                        else if(menuItem.getTitle().equals("Gallery")){
                            Intent gallery = new Intent();
                            gallery.setAction(Intent.ACTION_GET_CONTENT);
                            gallery.setType("image/*");
                            startActivityForResult(gallery,Gallery_request);
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    //For getting image from the Camera and Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode){
                case Gallery_request:
                    Uri selectedImage = data.getData();

                    progressDialog.setTitle("Profile Picture");
                    progressDialog.setMessage("Please wait, while we uploading your profile picture...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);

                    final StorageReference filePath = UserProfileReference.child(currentUID + ".jpg");

                    filePath.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Settings.this, "Profile Picture stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                                //Get Profile Address from Ifrebase
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUrl = uri.toString();
                                        //Upload Profile Firebase URI to Realtime Database
                                        UserDetailReference.child("profileimage").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(Settings.this, "Profile Picture stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                        } else {
                                                            Toast.makeText(Settings.this, "Error Occured: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                    //set Image to Circle Image
                    Picasso.get().load(selectedImage).placeholder(R.drawable.profile).into(image_Profile);
                    break;
            }
        }
    }
    public void DateChoose(View view){
             // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = 2000;
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        DOB_Text.setText(dayOfMonth + "-" + months[(monthOfYear)] + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void Update_BUTTON_CLICK (View view){
        progressDialog.setTitle("Update Information");
        progressDialog.setMessage("Please Wait, While we are updating Info...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);

        String Username  = Username_Text.getText().toString();
        String Fullname  = FullNAME_Text.getText().toString();
        String Country   = Country_Text.getText().toString();
        String Gender    = Gender_Text.getText().toString();
        String Realtionship  = RelationShip_Text.getText().toString();
        String DOB = DOB_Text.getText ().toString ();
        String status = Status_text.getText().toString();


        HashMap user_POST = new HashMap();
        //Post Related Data
        user_POST.put("username", Username);
        user_POST.put("fullname", Fullname);
        user_POST.put("country", Country);
        user_POST.put("gender", Gender);
        user_POST.put("relationship", Realtionship);
        user_POST.put("dob", DOB);
        user_POST.put("status", status);

        UserDetailReference.updateChildren(user_POST).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(Settings.this, "Information updated successfully.", Toast.LENGTH_SHORT).show();
                    Intent Home = new Intent(Settings.this,MainActivity.class);
                    startActivity(Home);
                } else {
                    Toast.makeText(Settings.this, "Error Occured" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }
}
