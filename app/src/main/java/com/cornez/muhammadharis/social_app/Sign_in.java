package com.cornez.muhammadharis.social_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cornez.muhammadharis.social_app.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Sign_in extends AppCompatActivity {
    private Button signInBTN;
    private EditText Email_Text,Password_Text;
    private TextView newAccountLink;

    private FirebaseAuth userAuth;
    private ProgressDialog progressDialog;

    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        userAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        signInBTN = findViewById(R.id.Login_id);
        Email_Text = findViewById(R.id.SingIn_Email_id);
        Password_Text = findViewById(R.id.SingIn_Password_id);
        newAccountLink = findViewById(R.id.newAccount_ID);


    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToHomeActivity();
        }
    }

    public void SignIN_Button_Click (View view) {
        String Email = Email_Text.getText().toString();
        String Password = Password_Text.getText().toString();

        if (Email.isEmpty() && Password.isEmpty())
            Toast.makeText(this, "Please Filled Every Field...", Toast.LENGTH_SHORT).show();
        else if (Email.isEmpty())
            Toast.makeText(this, "Please type your Email...", Toast.LENGTH_SHORT).show();
        else if (Password.isEmpty())
            Toast.makeText(this, "Please type your Password...", Toast.LENGTH_SHORT).show();
        else{
                if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                    progressDialog.setTitle("Logging In");
                    progressDialog.setMessage("Please Wait, While we are login your Account...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);

                    userAuth.signInWithEmailAndPassword(Email,Password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Toast.makeText(Sign_in.this, "You are Logged in Successfully", Toast.LENGTH_SHORT).show();
                                        SendUserToHomeActivity();
                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Toast.makeText(Sign_in.this, "Error Occured: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
                else
                    Toast.makeText(this, "Provide Email is Not valid...", Toast.LENGTH_SHORT).show();
        }
    }

        //Google Image Click
    public void GoogleSignIN(View view){
        Toast.makeText(this, "Google SignIn Not Configure...", Toast.LENGTH_SHORT).show();
    }

    //Google Image Click
    public void FacebookSignIN(View view){
        Toast.makeText(this, "Facebook SignIN = Not Configure...", Toast.LENGTH_SHORT).show();
    }


    public void SendUserToHomeActivity(){
        Intent Home = new Intent(Sign_in.this,MainActivity.class);
        Home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Home);
        finish();
    }

        //GOTO Create Account
    public void newAccount_TextLINK_Click(View view){
        Intent SignUP = new Intent(Sign_in.this,Sign_up.class);
        startActivity(SignUP);
    }
}
