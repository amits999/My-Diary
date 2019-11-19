package com.amit.action.my_diary;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button loginButton;
    private SignInButton googleLogin;
    private FirebaseAuth mAuth;
    private ImageView   githubLogin;
    private ProgressDialog mProgress;
    private GoogleSignInClient mGoogleSignInClient;
    private OAuthProvider.Builder provider;

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
        mProgress.setTitle("Logging In...");
        mProgress.setCanceledOnTouchOutside(false);

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

        provider = OAuthProvider.newBuilder("github.com");


        githubLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginGithub();
            }
        });


    }

    private void loginGithub() {
        mProgress.show();

        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    mProgress.dismiss();

                                    Toast.makeText(LoginActivity.this, "Github Authentication Successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent =new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure.
                                    mProgress.dismiss();
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
        } else {
            // There's no pending result so you need to start the sign-in flow.
            // See below.

            mAuth.startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    mProgress.dismiss();
                                    // User is signed in.
                                    // IdP data available in
                                    // authResult.getAdditionalUserInfo().getProfile().
                                    // The OAuth access token can also be retrieved:
                                    // authResult.getCredential().getAccessToken().
                                    Toast.makeText(LoginActivity.this, "Github Authentication Successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent =new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgress.dismiss();
                                    // Handle failure.
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            });

        }

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

        mProgress.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            Log.d("Google Result Login", "signInWithCredential:success");
                            Intent intent =new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this, "Authentication Successful! "+user.getDisplayName(), Toast.LENGTH_SHORT).show();

                        } else {
                            mProgress.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w("Google Result Login", "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void resetPassword(View view) {
        Intent intent=new Intent(getApplicationContext(),ResetPasswordActivity.class);
        startActivity(intent);
    }
}