package com.amit.action.my_diary;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button loginButton;
    private SignInButton googleLogin;
    private FirebaseAuth mAuth;
    private ImageView   githubLogin;
    private ProgressDialog mProgress;
    private GoogleSignInClient mGoogleSignInClient;

    private final static int RC_SIGN_IN=5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();

        email=findViewById(R.id.login_email_field);
        password=findViewById(R.id.login_password_field);
        loginButton=findViewById(R.id.login_button);
        googleLogin=findViewById(R.id.google_signin_button);
        githubLogin=findViewById(R.id.github_signin_button);
        mProgress= new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyDetails();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }

    private void verifyDetails() {
        String emailId=email.getText().toString().trim();
        String pass=password.getText().toString().trim();

        if (emailId.isEmpty()){
            email.setError("Email id required!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailId).matches()){
            email.setError("Enter a valid email address!");
            email.requestFocus();
            return;
        }

        if (pass.isEmpty()){
            password.setError("Password should not be empty");
            password.requestFocus();
            return;
        }

        if (pass.length()<6){
            password.setError("Password length should be 6");
            password.requestFocus();
            return;
        }

        loginUser(emailId,pass);
    }

    private void loginUser(String emailId, String pass) {

        mProgress.setTitle("Logging In...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mAuth.signInWithEmailAndPassword(emailId,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mProgress.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent =new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    mProgress.dismiss();

                    if (task.getException() instanceof FirebaseAuthInvalidUserException){
                        Toast.makeText(getApplicationContext(),"User with email address does not exist!",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void toRegistratinActivity(View view) {
        Intent intent=new Intent(LoginActivity.this,RegistrationActivity.class);
        startActivity(intent);
        finish();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Google sign in result", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Google sign in failed!", Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Firebase google Auth", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            Log.d("Google Result Login", "signInWithCredential:success");
                            Intent intent =new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this, "Authentication Successful! "+user.getDisplayName(), Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google Result Login", "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
