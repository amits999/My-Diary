package com.amit.action.my_diary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;

public class AddNotesActivity extends AppCompatActivity {
    private Toolbar mToolbar,bottomToolbar;
    private EditText titleField,notesField;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);

        mToolbar=findViewById(R.id.add_notes_bar);
        bottomToolbar=findViewById(R.id.bottomAppBar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_down_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }
}
