package com.amit.action.my_diary;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Toolbar myChildToolbar = (Toolbar) findViewById(R.id.about_page_bar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setElevation(3f);

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }
    public void contact_us(View view) {
        View aboutPage = new AboutPage(this)
                .isRTL(false)
                //.addGroup("Developer Profile")
                .setImage(R.drawable.diary)
                .setDescription(" ")
                //.addItem(new Element().setTitle("Version 1.0"))
                .addGroup("Connect with us")
                .addEmail("awesomeamit50@gmail.com")
                //.addWebsite("http://medyo.github.io/")
                .addFacebook("amitsharma000")
                .addTwitter("awesomeamit98")
                .addYoutube("UCZT0aW4u2_TOi4NX15iG8bA")
                //.addPlayStore("com.ideashower.readitlater.pro")
                .addInstagram("i__amit")
                .addGitHub("amits999")
                .create();

        setContentView(aboutPage);
    }
}
