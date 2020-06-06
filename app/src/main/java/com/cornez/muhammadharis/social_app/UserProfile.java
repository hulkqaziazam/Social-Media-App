package com.cornez.muhammadharis.social_app;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.cornez.muhammadharis.social_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {
    private DatabaseReference UserDetailReference;

    private TextView Username_Text,FullNAME_Text,DOB_Text;
    private TextView Country_Text,Gender_Text,RelationShip_Text,Status_text;
    private CircleImageView image_Profile;

    private FirebaseAuth userAuth;
    private String currentUID;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_user_profile);

        //Firebase Intialization
        userAuth = FirebaseAuth.getInstance();
        currentUID = userAuth.getCurrentUser().getUid();
        UserDetailReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUID);

        Username_Text = findViewById(R.id.userProfile_username_id);
        FullNAME_Text = findViewById(R.id.userProfile_Fullname_id);
        Country_Text  = findViewById(R.id.userProfile_Country_ID);
        Gender_Text  = findViewById(R.id.userProfile_Gender_id);
        RelationShip_Text  = findViewById(R.id.userProfile_Relationship_id);
        image_Profile = findViewById(R.id.userProfile_profile);
        DOB_Text = findViewById(R.id.userProfile_DOB);
        Status_text = findViewById(R.id.userProfile_status);

        progressDialog = new ProgressDialog(this);

        progressDialog.setTitle("Getting User Information");
        progressDialog.setMessage("Please wait, while we Getting Your Information...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        UserDetailReference.addValueEventListener(new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage"))
                        Picasso.get().load(dataSnapshot.child("profileimage").getValue().toString()).placeholder(R.drawable.profile).into(image_Profile);

                    Country_Text.setText("Country: " +dataSnapshot.child("country").getValue().toString());
                    DOB_Text.setText("DOB: " +dataSnapshot.child("dob").getValue().toString());
                    Gender_Text.setText("Gender: " +dataSnapshot.child("gender").getValue().toString());
                    RelationShip_Text.setText("Relationship: " + dataSnapshot.child("relationship").getValue().toString());
                    Username_Text.setText("@" +dataSnapshot.child("username").getValue().toString());
                    FullNAME_Text.setText(dataSnapshot.child("fullname").getValue().toString());
                    Status_text.setText( dataSnapshot.child("status").getValue().toString());

                    progressDialog.dismiss ();
                }
                else
                    progressDialog.dismiss ();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss ();
            }
        });
    }
}
