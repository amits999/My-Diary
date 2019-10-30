package com.amit.action.my_diary;

import android.accounts.NetworkErrorException;
import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameField,emailField,passField,conPassField;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private ProgressDialog mProgress;
    private FirebaseUser currUser;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth=FirebaseAuth.getInstance();
        mProgress=new ProgressDialog(this);

        nameField=findViewById(R.id.register_name_field);
        passField=findViewById(R.id.register_password_field);
        conPassField=findViewById(R.id.register_confirm_password_field);
        emailField=findViewById(R.id.register_email_field);
        signUpButton=findViewById(R.id.register_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyDetails();
            }
        });
    }

    private void verifyDetails() {
        String name=nameField.getText().toString().trim();
        String pass=passField.getText().toString().trim();
        String conPass=conPassField.getText().toString().trim();
        String email=emailField.getText().toString().trim();

        if (email.isEmpty()){
            emailField.setError("Email id required!");
            emailField.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailField.setError("Enter a valid email address");
            emailField.requestFocus();
            return;
        }

        if (name.isEmpty() || name.length() < 4){
            nameField.setError("Enter a valid full name!");
            nameField.requestFocus();
            return;
        }

        if (pass.isEmpty() || pass.length() < 6){
            passField.setError("Enter a valid Password min six character!");
            passField.requestFocus();
            return;
        }

        if(!pass.equals(conPass)){
            conPassField.setError("Passwords did not match!");
            conPassField.requestFocus();
            return;
        }

        registerUser(email,pass,name);
    }

    private void registerUser(String email, String pass, final String name) {
        mProgress.setTitle("Creating your Account");
        mProgress.setMessage("Please wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    Toast.makeText(RegistrationActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    currUser=mAuth.getCurrentUser();
                    uid=currUser.getUid();

                    UserProfileChangeRequest profileChangeRequest= new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    currUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(RegistrationActivity.this, "User Profile Updated", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    mProgress.dismiss();
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(RegistrationActivity.this, "You are already registered!",
                                Toast.LENGTH_SHORT).show();

                    }
                    else if (task.getException() instanceof NetworkErrorException){
                        Toast.makeText(RegistrationActivity.this, "Please check your internet connection!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {

                        Toast.makeText(RegistrationActivity.this, task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    public void toLoginActivity(View view) {
        Intent intent=new Intent(RegistrationActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
