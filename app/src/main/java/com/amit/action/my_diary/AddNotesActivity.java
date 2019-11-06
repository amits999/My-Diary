package com.amit.action.my_diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddNotesActivity extends AppCompatActivity {
    private Toolbar mToolbar,bottomToolbar;
    private EditText titleField,notesField;
    private ProgressDialog mProgress;
    private FloatingActionButton fab;
    private DatabaseReference mRef;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    long childCount=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        mProgress=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        mRef= FirebaseDatabase.getInstance().getReference().child("notes");

        mToolbar=findViewById(R.id.add_notes_bar);
        bottomToolbar=findViewById(R.id.bottomAppBar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Note");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleField=findViewById(R.id.add_notes_title);
        notesField=findViewById(R.id.add_notes_note);
        fab=findViewById(R.id.fab);


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
        mProgress.show();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mUser.getUid()).exists()){
                    childCount=dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        HashMap map =new HashMap<>();
        map.put("title",title);
        map.put("note",note);

        mRef.child(mUser.getUid()).child("note"+childCount +1).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgress.dismiss();
                    Toast.makeText(AddNotesActivity.this, "Note added safely!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddNotesActivity.this,MainActivity.class));
                    finish();
                }else{
                    mProgress.dismiss();
                    Toast.makeText(AddNotesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
