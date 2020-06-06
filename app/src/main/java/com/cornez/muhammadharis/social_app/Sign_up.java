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
import android.widget.Toast;

import com.cornez.muhammadharis.social_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Sign_up extends AppCompatActivity {
    private EditText Email_Text,Password_Text,Confirm_Password_Text;
    private Button create_Account;
    private FirebaseAuth userAuth;

    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        userAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        Email_Text = findViewById(R.id.SignUp_Email_id);
        Password_Text = findViewById(R.id.SignUp_Password_id);
        Confirm_Password_Text = findViewById(R.id.SignUp_Cpassword_id);
        create_Account = findViewById(R.id.SignUP_BTN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = userAuth.getCurrentUser();

        if(currentUser != null){
            SendUserToHomeActivity();
        }
    }

    public void SendUserToHomeActivity(){
        Intent Home = new Intent(Sign_up.this,MainActivity.class);
        Home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Home);
        finish();
    }

    public void SIGNUP_BUTTON_CLICK(View view){
        String Email = Email_Text.getText().toString();
        String Password = Password_Text.getText().toString();
        String Confirm_Password = Confirm_Password_Text.getText().toString();

        if(Password.length() >= 6  && Email.length() >= 7){
            if (Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                if (Password.compareTo(Confirm_Password) == 0) {
                    progressDialog.setTitle("Create New Account");
                    progressDialog.setMessage("Please Wait, While we are Creating Account...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);

                    userAuth.createUserWithEmailAndPassword(Email,Password).
                           addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                               @Override
                               public void onComplete(@NonNull Task<AuthResult> task) {
                                   if(task.isSuccessful()){
                                       progressDialog.dismiss();
                                       Toast.makeText(Sign_up.this, "You are Authenticated Succesffuly...", Toast.LENGTH_LONG).show();
                                       Intent setupAccount = new Intent(Sign_up.this,setup_Account.class);
                                       setupAccount.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                       startActivity(setupAccount);
                                       finish();
                                   }
                                   else{
                                       progressDialog.dismiss();
                                       Toast.makeText(Sign_up.this, "Error Occured: "+ task.getException().getMessage().toString(), Toast.LENGTH_LONG).show();
                                   }
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(Sign_up.this, "Error Occured: "+ e.getMessage().toString(), Toast.LENGTH_LONG).show();
                                }
                           }
                    );
                } else
                    Toast.makeText(this, "Password & Confirm Password do not Match...", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "Provide Email is Not valid...", Toast.LENGTH_SHORT).show();
        }
        else{
            if(Email.isEmpty() && Password.isEmpty() && Confirm_Password.isEmpty())
                Toast.makeText(this,"Please Filled Every Field...",Toast.LENGTH_SHORT).show();
            else if(Email.isEmpty())
                Toast.makeText(this,"Please type your Email...",Toast.LENGTH_SHORT).show();
            else if(Password.isEmpty())
                Toast.makeText(this,"Please type your Password...",Toast.LENGTH_SHORT).show();
            else if(Confirm_Password.isEmpty())
                Toast.makeText(this,"Please Confirm Your Password...",Toast.LENGTH_SHORT).show();
            else if( Email.length() <= 6)
                Toast.makeText(this,"Email must be greater than 6 letters...",Toast.LENGTH_SHORT).show();
            else if(Password.length() <= 5)
                Toast.makeText(this,"Password must be greater than 5 letters...",Toast.LENGTH_SHORT).show();
        }
    }
}
