package com.amit.action.my_diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddNotesActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private BottomAppBar bottomAppBar;
    private EditText titleField,notesField;
    private ProgressDialog mProgress;
    private FloatingActionButton fab;
    private DatabaseReference mRef,impRef;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    //private boolean textChanged=false;
    private String tit=null,not=null;
    private String saveCurrentDate=null,saveCurrentTime=null,time=null,key=null;
    private  boolean isImp=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        mProgress=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        mToolbar=findViewById(R.id.add_notes_bar);

        bottomAppBar=findViewById(R.id.bottomAppBar);
        bottomAppBar.replaceMenu(R.menu.add_down_menu);

        bottomAppBar.setTitle("Edited: 9 Nov");

        Intent intent =getIntent();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Note");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleField=findViewById(R.id.add_notes_title);
        notesField=findViewById(R.id.add_notes_note);
        fab=findViewById(R.id.fab);

        if(intent.getExtras().getString("uniqueKey").equals("from_main")) {

            getSupportActionBar().setTitle("Your Note");

            key=intent.getStringExtra("postKey");
            System.out.println(key);
            mRef= FirebaseDatabase.getInstance().getReference().child("notes").child(mUser.getUid()).child(key);

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        tit=dataSnapshot.child("title").getValue().toString();
                        titleField.setText(tit);
                        not=dataSnapshot.child("note").getValue().toString();
                        notesField.setText(not);
                    }else{
                        Toast.makeText(AddNotesActivity.this, "DataSnapshot does not exist", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        mRef= FirebaseDatabase.getInstance().getReference().child("notes");
        impRef=FirebaseDatabase.getInstance().getReference().child("star_marked").child(mUser.getUid()).child("important_notes");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verifyDetails();

            }
        });

        /*if (!titleField.hasFocus() && !notesField.hasFocus()){
            bottomAppBar.getMenu().getItem(R.id.undo).setIcon(R.drawable.undo_grey);
            bottomAppBar.getMenu().getItem(R.id.redo).setIcon(R.drawable.redo_grey);
        }*/

        final TextViewUndoRedo nTextViewUndoRedo = new TextViewUndoRedo(titleField);
        final TextViewUndoRedo mTextViewUndoRedo = new TextViewUndoRedo(notesField);

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==R.id.undo){
                    if (titleField.hasFocus()){

                        if (!nTextViewUndoRedo.getCanUndo()){
                            Toast.makeText(AddNotesActivity.this, "Cannot Undo!", Toast.LENGTH_SHORT).show();
                        }else{
                            nTextViewUndoRedo.undo();
                        }
                    }

                    if (notesField.hasFocus()){

                        if (!mTextViewUndoRedo.getCanUndo()){
                            Toast.makeText(AddNotesActivity.this, "Cannot Undo!", Toast.LENGTH_SHORT).show();
                        }else{
                            mTextViewUndoRedo.undo();
                        }
                    }

                }

                if (item.getItemId()==R.id.redo){
                    if (titleField.hasFocus()){

                        if (!nTextViewUndoRedo.getCanRedo()){
                            Toast.makeText(AddNotesActivity.this, "Cannot Redo!", Toast.LENGTH_SHORT).show();
                        }else{
                            nTextViewUndoRedo.redo();
                        }
                    }

                    if (notesField.hasFocus()){
                        if (!mTextViewUndoRedo.getCanRedo()){
                            Toast.makeText(AddNotesActivity.this, "Cannot Redo!", Toast.LENGTH_SHORT).show();
                        }else{
                            mTextViewUndoRedo.redo();
                        }
                    }
                }


                return false;
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

        System.out.println(title+" "+tit);

        if (title.equals(tit) && note.equals(not)){
            Toast.makeText(this, "No Changes found!", Toast.LENGTH_SHORT).show();

        }else{
            mProgress.setMessage("Submitting your notes safely :)");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            saveNote(title,note);
        }


    }

    private void saveNote(String title, String note) {

        long milis=System.currentTimeMillis();
        time=Long.toString(milis);

        Calendar calendarForDate= Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd:MMMM:yyyy");
        saveCurrentDate=currentDate.format(calendarForDate.getTime());

        Calendar calendarForTime= Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calendarForTime.getTime());

        HashMap map =new HashMap<>();
        map.put("title",title);
        map.put("note",note);
        map.put("date",saveCurrentDate);
        map.put("time",saveCurrentTime);

        if (key!=null){
            mRef.child(mUser.getUid()).child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mProgress.dismiss();
                        Toast.makeText(AddNotesActivity.this, "Note edited", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddNotesActivity.this,MainActivity.class));
                        finish();
                    }else{
                        mProgress.dismiss();
                        Toast.makeText(AddNotesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            mRef.child(mUser.getUid()).child(time).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mProgress.dismiss();
                        Toast.makeText(AddNotesActivity.this, "New note added!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddNotesActivity.this,MainActivity.class));
                        finish();
                    }else{
                        mProgress.dismiss();
                        Toast.makeText(AddNotesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Log.i("Imp value", isImp+"");
            if (isImp){
                impRef.child(time).setValue("yes").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.i("Imp mark check","work done");
                        }else{
                            Toast.makeText(AddNotesActivity.this, "Error Saving Changes! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.add_up_menu,menu);

        if (key!=null){
            impRef.child(key).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        menu.findItem(R.id.up_star).setIcon(R.drawable.ic_star_black_24dp);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.up_delete){

            AlertDialog.Builder builder=new AlertDialog.Builder(AddNotesActivity.this);
            builder.setTitle("Confirm Delete!");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (key!=null){
                        mRef.child(mUser.getUid()).child(key).removeValue();
                        impRef.child(key).removeValue();
                        onBackPressed();
                    }else{
                        onBackPressed();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }

        if (item.getItemId()==R.id.up_star){
            Drawable drawable=item.getIcon();

            if ( drawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_star_border_black_24dp).getConstantState())){

                isImp=true;
                if (key!=null){
                    impRef.child(key).setValue("yes").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.i("Imp mark check","work done");
                            }else{
                                Toast.makeText(AddNotesActivity.this, "Error Saving Changes! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                item.setIcon(R.drawable.ic_star_black_24dp);
                Toast.makeText(AddNotesActivity.this, "Note is marked as important.", Toast.LENGTH_SHORT).show();
                item.setTitle("Mark Important");

            }else if ( drawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_star_black_24dp).getConstantState())){

                isImp=false;
                if (key!=null){
                    impRef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Log.i("Imp unmark check","work done");
                            }else{
                                Toast.makeText(AddNotesActivity.this, "Error Saving Changes! "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                item.setIcon(R.drawable.ic_star_border_black_24dp);
                Toast.makeText(AddNotesActivity.this, "Note is unmarked from important.", Toast.LENGTH_SHORT).show();
                item.setTitle("Remove from Imp.");

            }
        }

        return super.onOptionsItemSelected(item);

    }

}
