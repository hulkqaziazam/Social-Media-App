package com.cornez.muhammadharis.social_app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView nav_View;
    private DrawerLayout drawerLayout;

    private RecyclerView AllusersPOST;
    private FirebaseRecyclerAdapter firebaseRecyleradapter;

    private ActionBarDrawerToggle actionBarToggle;
    private Toolbar Nav_Toolbar;

    private CircleImageView Navigation_ProfileImage;
    private TextView Navigation_ProfileName, Navigation_Email;
    private ImageButton NewPost_BTN;

    private FirebaseAuth userAuth;
    private DatabaseReference UsersReference, PostsReference, likeReference;

    private Boolean isLike = false;
    String currentUID;
    private Boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Intialization
        userAuth = FirebaseAuth.getInstance();
        currentUID = userAuth.getCurrentUser().getUid();
        PostsReference = FirebaseDatabase.getInstance().getReference().child("Users Posts");
        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        likeReference = FirebaseDatabase.getInstance().getReference().child("Like");

        //Set up a toolbar
        SettingUpCustomToolbaar();


        // Show All users Post through Firebase recycler Adapter
        AllusersPOST = (RecyclerView) findViewById(R.id.MainActivity_all_UserPost_List);
        AllusersPOST.setHasFixedSize(true);
        //Linearlaoutmanager to show Latest posts on top
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        AllusersPOST.setLayoutManager(linearLayoutManager);


        //Show Header in Navigation View
        nav_View = (NavigationView) findViewById(R.id.MainActivity_nav_View);
        View header = nav_View.inflateHeaderView(R.layout.nav_header);


        //Navigation Menu Option Clicker
        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserSelectionOpetion(item);
                return false;
            }
        });

        //set Porifilfe image Email and Full name in Navigation Pane
        Navigation_ProfileImage = header.findViewById(R.id.navigation_Profile_pic);
        Navigation_ProfileName = header.findViewById(R.id.navigation_Profile_name);
        Navigation_Email = header.findViewById(R.id.navigation_Email);
        Navigation_Email.setText(userAuth.getCurrentUser().getEmail());


        //Get Image and Name from the Firebase of profile
        UsersReference.child(currentUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullname = "";
                    if (dataSnapshot.hasChild("fullname")) {
                        fullname = dataSnapshot.child("fullname").getValue().toString();
                        Navigation_ProfileName.setText(fullname);
                    } else
                        Toast.makeText(MainActivity.this, "Fullname not Exist", Toast.LENGTH_SHORT).show();
                    if (dataSnapshot.hasChild("profileimage")) {
                        String profile_Pic = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(profile_Pic).placeholder(R.drawable.profile).into(Navigation_ProfileImage);
                    } else
                        Toast.makeText(MainActivity.this, fullname + " not uploaded Profile Picture", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //For New Post
        NewPost_BTN = (ImageButton) findViewById(R.id.MainActivity_newPost_btn_ID);
        NewPost_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUsertoNewPostActivity();
            }
        });

        //Show Post ON Home Screen
        HomeUserPostShow();
    }

    //Show Post ON Home Screen
    private void HomeUserPostShow() {
        //get Details of the post
        FirebaseRecyclerOptions<post_Details> options =
                new FirebaseRecyclerOptions.Builder<post_Details>()
                        .setQuery(PostsReference, post_Details.class)
                        .build();


        firebaseRecyleradapter = new FirebaseRecyclerAdapter<post_Details, postViewHolder>(options) {
            @Override
            public postViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.allpost_layout, parent, false);
                return new postViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(postViewHolder holder, int position, post_Details model) {
                final String keyPost = getRef(position).getKey();

                holder.setDate1(model.getDate());
                holder.setTime1(model.getTime());
                holder.setDescription1(model.getDescription());
                holder.setFullname1(model.getFullname());
                holder.setProfileimage1(getApplicationContext(), model.getProfileimage());
                holder.setupdate(model.getupdate());
                holder.setPostimage1(getApplicationContext(), model.getPostimage());

                holder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickOnpost = new Intent(MainActivity.this, ClickOnPost.class);
                        clickOnpost.putExtra("postkey", keyPost);
                        startActivity(clickOnpost);
                    }
                });

                holder.like.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isLike = true;

                        likeReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.child(keyPost).hasChild(currentUID)) {
                                        dataSnapshot.child(keyPost).child(currentUID);
                                        isLike = true;
                                    } else {

                                    }//                                        dataSnapshot.child (keyPost).child (currentUID).
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };

        AllusersPOST.setAdapter(firebaseRecyleradapter);
    }


    public static class postViewHolder extends RecyclerView.ViewHolder {
        View mview;
        ImageButton like;

        public postViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;

            like = mview.findViewById(R.id.Allpost_Like);
        }

        public void setDate1(String date) {
            TextView Date_Text = mview.findViewById(R.id.ALLPOST_Date_ID);
            Date_Text.setText(date);
        }

        public void setDescription1(String description) {
            TextView Description_Text = mview.findViewById(R.id.ALLPOST_Description_ID);
            Description_Text.setText(description);
        }

        public void setFullname1(String fullname) {
            TextView fullname_Text = mview.findViewById(R.id.ALLPOST_Fullname_ID);
            fullname_Text.setText(fullname);
        }

        public void setPostimage1(Context ctx, String postimage) {
            ImageView Postimage_Text = mview.findViewById(R.id.ALLPOST_postImage_ID);
            if (postimage.equals("2"))
                Picasso.get().load(R.drawable.profile).into(Postimage_Text);
            else
                Picasso.get().load(postimage).into(Postimage_Text);
        }

        public void setProfileimage1(Context ctx1, String profileimage) {
            CircleImageView Profileimage_Text = mview.findViewById(R.id.ALLPOST_postProfileImage_ID);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(Profileimage_Text);
        }

        public void setTime1(String time) {
            TextView Time_Text = mview.findViewById(R.id.ALLPOST_time_ID);
            Time_Text.setText(time);
        }

        public void setupdate(String update) {
            TextView Update_text = mview.findViewById(R.id.ALLPOST_simpletext_ID);
            Update_text.setText(update);
        }

    }


    //Setting up a Toolbar
    private void SettingUpCustomToolbaar() {
        //Set Bar on the top and set name
        Nav_Toolbar = (Toolbar) findViewById(R.id.MainActivity_Custom_Toolbar);
        setSupportActionBar(Nav_Toolbar);
        getSupportActionBar().setTitle("Home");
        Nav_Toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        //Setting up a Drawer Or naviagation Toolbar
        drawerLayout = (DrawerLayout) findViewById(R.id.drwawer_layout);
        //Set icon for Nav Drawer
        actionBarToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, Nav_Toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarToggle);
        actionBarToggle.syncState();
    }

    //Whenever this Activity Start
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = userAuth.getCurrentUser();

        if (currentUser == null) {
            SendUsertoLoginActivity();
        } else {
            //If user is Authticated then check User data exist in Database
            final String current_UserID = userAuth.getCurrentUser().getUid();

            UsersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(current_UserID)) {
                        SendUsertoSetupActivity();
                    } else {
                        firebaseRecyleradapter.startListening();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Error Occured:" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyleradapter.startListening();
    }

    //Select the Opetion from the Naviagation Menu
    private void UserSelectionOpetion(MenuItem item) {
        switch (item.getItemId()) {
            //when click on Add new post
            case R.id.nav_addpost:
                SendUsertoNewPostActivity();
                break;
            //when click on Home
            case R.id.nav_home:
                Intent homepage = new Intent(MainActivity.this, MainActivity.class);
                startActivity(homepage);
                break;
            //when click on User Profile
            case R.id.nav_profile:
                SendUsertoUserProfileActivity();
                break;
            //when click on User profile
            case R.id.nav_friends:
                Toast.makeText(MainActivity.this, "Friends", Toast.LENGTH_SHORT).show();
                break;
            //when click on Messages
            case R.id.nav_messages:
                Toast.makeText(MainActivity.this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            //when click on Seettings
            case R.id.nav_Setting:
                SendUsertoSettingActivity();
                break;
            //when click on logout
            case R.id.nav_logout:
                userAuth.signOut();
                SendUsertoLoginActivity();
                break;
        }
    }

    private void SendUsertoNewPostActivity() {
        Intent newpost = new Intent(MainActivity.this, NewPost.class);
        startActivity(newpost);
    }

    private void SendUsertoUserProfileActivity() {
        Intent userprofile = new Intent(MainActivity.this, UserProfile.class);
        startActivity(userprofile);
    }

    private void SendUsertoSettingActivity() {
        Intent settings = new Intent(MainActivity.this, Settings.class);
        startActivity(settings);
    }

    private void SendUsertoSetupActivity() {
        Intent Setup = new Intent(MainActivity.this, setup_Account.class);
        //Its not sign in you when you press back button
        Setup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Setup);
        finish();
    }

    private void SendUsertoLoginActivity() {
        Intent SignIN = new Intent(MainActivity.this, Sign_in.class);
        //Its not sign in you when you press back button
        SignIN.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(SignIN);
        finish();
    }

    //When click on menu Item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}
