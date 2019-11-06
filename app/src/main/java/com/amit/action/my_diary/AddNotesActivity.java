package com.amit.action.my_diary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class AddNotesActivity extends AppCompatActivity {
    private Toolbar mToolbar,bottomToolbar;
    private EditText titleField,notesField;
    private ProgressDialog mProgress;
    private FloatingActionButton fab;
    private DatabaseReference mref;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        mProgress=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        uid=mUser.getUid();

        mToolbar=findViewById(R.id.add_notes_bar);
        //bottomToolbar=findViewById(R.id.bottomAppBar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Note");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyDetails();
            }
        });
    }

    private void verifyDetails() {
        String title=titleField.getText().toString().trim();
        String note=notesField.getText().toString().trim();

        if (title.isEmpty() || title.length() <4){
            titleField.setError("Title length should be more than 4 characters!");
            titleField.requestFocus();
            return;
        }

        if (note.isEmpty() || note.length() <4){
            notesField.setError("Note length should be more than 4 characters!");
            notesField.requestFocus();
            return;
        }

        mProgress.setMessage("Submitting your notes safely :)");
        mProgress.setCanceledOnTouchOutside(false);
        //mProgress.show();


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.add_down_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }
}
