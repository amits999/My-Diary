package com.amit.action.my_diary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CompleteNoteActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private String postKey;
    private FirebaseUser mUser;
    private TextView mTitle,mNote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_note);

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        mToolbar=findViewById(R.id.complete_notes_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postKey= getIntent().getStringExtra("postKey");
        mRef=FirebaseDatabase.getInstance().getReference().child("notes").child(mUser.getUid()).child(postKey);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
