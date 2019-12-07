package com.amit.action.my_diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Toolbar mToolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    private FloatingActionButton fab;
    private FirebaseUser curUser;
    private CircleImageView headerImage;
    private TextView headerUserName;

    boolean doubleTap=false;

    private RecyclerView mRecyclerView;
    private DatabaseReference mRef;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        curUser=mAuth.getCurrentUser();
        mProgress=new ProgressDialog(this);

        mProgress.setTitle("Getting your Books!");
        mProgress.setMessage("Please Wait ...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mToolbar=findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Diary");

        drawerLayout=findViewById(R.id.main_drawer_layout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView=findViewById(R.id.main_navigation_view);
        fab=findViewById(R.id.main_floatingActionButton);

        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);

        headerImage=navView.findViewById(R.id.header_image);
        headerUserName=navView.findViewById(R.id.header_username);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                UserMenuSelector(menuItem);
                return false;
            }
        });

        headerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,AddNotesActivity.class);
                intent.putExtra("uniqueKey","from_create");
                startActivity(intent);
            }
        });

        mRecyclerView=findViewById(R.id.main_recyclerView);
        mRecyclerView.setHasFixedSize(true);

        gridLayoutManager=new GridLayoutManager(this,2);
        //gridLayoutManager.setReverseLayout(true);
        //gridLayoutManager.setStackFromEnd(true);

        linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);

    }

    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.nav_my_home:
                drawerLayout.closeDrawer(Gravity.LEFT);
                Toast.makeText(getApplicationContext(), "You are on HOME!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:

                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Dou you really want to logout!");
                builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGoogleSignInClient.signOut();
                        mAuth.signOut();
                        sentUserToLoginActivity();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog=builder.create();
                alertDialog.show();

                break;

            case R.id.nav_about_developer:
                Intent intent=new Intent(MainActivity.this,AboutUsActivity.class);
                startActivity(intent);
                break;

            case R.id.nav_selected_books:
                Intent selectIntent=new Intent(MainActivity.this,SelectedBooks.class);
                startActivity(selectIntent);
                break;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (curUser==null){
            Intent intent=new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        mRef= FirebaseDatabase.getInstance().getReference().child("notes").child(curUser.getUid());

        if (!isNetworkAvailable()){
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle("Info");
            alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog ad=alertDialog.create();
            ad.show();
        }

        if (curUser.getPhotoUrl()!=null){
            Picasso.get().load(curUser.getPhotoUrl()).into(headerImage);
        }
        headerUserName.setText(curUser.getDisplayName());

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(mRef,Model.class)
                .build();



        FirebaseRecyclerAdapter<Model, NotesViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Model, NotesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull NotesViewHolder holder, int position, @NonNull Model model) {
                final String postKey=getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setNote(model.getNote());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(MainActivity.this,AddNotesActivity.class);
                        intent.putExtra("postKey",postKey);
                        intent.putExtra("uniqueKey","from_main");
                        startActivity(intent);
                    }
                });

                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent=new Intent(MainActivity.this,ReaderActivity.class);
                        startActivity(intent);
                        return false;
                    }
                });
            }

            @NonNull
            @Override
            public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mProgress.dismiss();

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                NotesViewHolder viewHolder=new NotesViewHolder(view);
                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public NotesViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setTitle(String t){
            TextView title= mView.findViewById(R.id.note_title_text);
            title.setText(t);
        }

        public void setNote(String n){
            TextView note= mView.findViewById(R.id.notes_text);
            note.setText(n);
        }

    }

    @Override
    public void onBackPressed() {
        if (doubleTap){
            super.onBackPressed();
        }
        else {
            Toast.makeText(getApplicationContext(), "Double tap back to exit the app!", Toast.LENGTH_SHORT).show();
            doubleTap=true;
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleTap=false;
                }
            },500); //half second
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        if (item.getItemId()==R.id.main_menu_layout){
            Drawable drawable=item.getIcon();


            if ( drawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_grid_on_black_24dp).getConstantState())){
                mRecyclerView.setLayoutManager(gridLayoutManager);
                item.setIcon(R.drawable.ic_linear_black_24dp);

            }else if ( drawable.getConstantState().equals(getResources().getDrawable(R.drawable.ic_linear_black_24dp).getConstantState())){
                mRecyclerView.setLayoutManager(linearLayoutManager);
                item.setIcon(R.drawable.ic_grid_on_black_24dp);

            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void sentUserToLoginActivity() {

        Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
