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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class setup_Account extends AppCompatActivity {
    private EditText Username_Text,FullNAME_Text,DOB_Text;
    private Spinner Country_Text,Gender_Text,RelationShip_Text;
    private CircleImageView image_Profile;
    private static final int Gallery_request =1;

    private FirebaseAuth userAuth;
    private DatabaseReference UserReference;
    private StorageReference  UserProfileReference;
    private String currentUID;
    private ProgressDialog progressDialog;

    private String downloadUrl;

    private int mYear,mMonth,mDay;

    private String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup__account);

        userAuth    = FirebaseAuth.getInstance();
        currentUID  = userAuth.getCurrentUser().getUid();
        UserReference        = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);
        UserProfileReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        progressDialog = new ProgressDialog(this);

        Username_Text = findViewById(R.id.setup_username_id);
        FullNAME_Text = findViewById(R.id.setup_Fullname_id);
        Country_Text  = findViewById(R.id.setup_Country_id);
        Gender_Text  = findViewById(R.id.setup_Gender_id);
        RelationShip_Text  = findViewById(R.id.setup_Relationship_id);
        image_Profile = findViewById(R.id.setup_profile);
        DOB_Text = findViewById(R.id.setup_DOB);

            //Click on Image
        image_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(setup_Account.this,image_Profile);
                popup.getMenuInflater().inflate(R.menu.popup_menu,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals("Camera")){
                            Toast.makeText(setup_Account.this, "Camera Coding Not Working...", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(setup_Account.this, "Profile Picture stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                                    //Get Profile Address from Ifrebase
                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUrl = uri.toString();
                                            //Upload Profile Firebase URI to Realtime Database
                                        UserReference.child("profileimage").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(setup_Account.this, "Profile Picture stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                        } else {
                                                            Toast.makeText(setup_Account.this, "Error Occured: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    public void SAVE_BUTTON_CLICK(View view){
        String Username  = Username_Text.getText().toString();
        String Fullname = FullNAME_Text.getText().toString();
        String Country  = Country_Text.getSelectedItem ().toString();
        String Gender  = Gender_Text.getSelectedItem ().toString();
        String Realtionship  = RelationShip_Text.getSelectedItem ().toString();
        String DOB = DOB_Text.getText ().toString ();

        if (Username.isEmpty() && Fullname.isEmpty() && Country.isEmpty())
            Toast.makeText(this, "Please Filled Every Field...", Toast.LENGTH_SHORT).show();
        else if (Username.isEmpty())
            Toast.makeText(this, "Please type your Username...", Toast.LENGTH_SHORT).show();
        else if (Fullname.isEmpty())
            Toast.makeText(this, "Please type your Full Name...", Toast.LENGTH_SHORT).show();
        else if (Country.isEmpty())
            Toast.makeText(this, "Please type your Country...", Toast.LENGTH_SHORT).show();
        else if (Gender.isEmpty())
            Toast.makeText(this, "Please type your Country...", Toast.LENGTH_SHORT).show();
        else if (Realtionship.isEmpty())
            Toast.makeText(this, "Please type your Country...", Toast.LENGTH_SHORT).show();
        else if( Username.length() <= 3)
            Toast.makeText(this,"Username must be greater than 3 letters...",Toast.LENGTH_SHORT).show();
        else if(Fullname.length()  <= 5)
            Toast.makeText(this,"Full Name must be greater than 5 letters...",Toast.LENGTH_SHORT).show();
        else {
            progressDialog.setTitle("Saving Your Information");
            progressDialog.setMessage("Please Wait, While we are Saving Information...");
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            HashMap userData = new HashMap();
            userData.put("username",Username);
            userData.put("fullname",Fullname);
            userData.put("country",Country);
            userData.put("dob",DOB);
            userData.put("gender",Gender);
            userData.put("relationship",Realtionship);
            userData.put("status","Hey There i am using Social App");

                //Now Save User Information
            UserReference.updateChildren(userData).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(setup_Account.this, "Your Account is Created Succesfully...", Toast.LENGTH_LONG).show();
                        Intent Home = new Intent(setup_Account.this,MainActivity.class);
                        Home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Home);
                        finish();
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(setup_Account.this, "Error Occured: "+ task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void chooseDate(View view){
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
}
