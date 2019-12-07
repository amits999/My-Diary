package com.amit.action.my_diary;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class SelectedBooks extends AppCompatActivity {
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private ProgressDialog mprogress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_books);

        mToolbar=findViewById(R.id.selected_books_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar ab=getSupportActionBar();
        ab.setTitle("Selected Books");
        ab.setDisplayHomeAsUpEnabled(true);

    }
}
