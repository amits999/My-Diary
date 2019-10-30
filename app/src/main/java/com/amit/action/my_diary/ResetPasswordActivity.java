package com.amit.action.my_diary;

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
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailField;
    private FirebaseAuth mAuth;
    private Button resetButton;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailField=findViewById(R.id.reset_email_field);
        mAuth=FirebaseAuth.getInstance();
        resetButton=findViewById(R.id.reset_button);
        mProgress=new ProgressDialog(this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email=emailField.getText().toString().trim();

        if (email.isEmpty()){
            emailField.setError("Email id required!");
            emailField.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailField.setError("Not a valid email address!");
            emailField.requestFocus();
            return;
        }

        mProgress.setMessage("Sending request for Password Reset");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgress.dismiss();
                    Toast.makeText(ResetPasswordActivity.this,"Sent, Check your Email",Toast.LENGTH_LONG).show();

                    Intent intent=new Intent(ResetPasswordActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    mProgress.dismiss();
                    Toast.makeText(ResetPasswordActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
