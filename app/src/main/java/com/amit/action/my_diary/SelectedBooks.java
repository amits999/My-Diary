package com.amit.action.my_diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SelectedBooks extends AppCompatActivity {
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef,mNoteRef;
    private ProgressDialog mprogress;
    private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_books);

        mAuth=FirebaseAuth.getInstance();
        mNoteRef= FirebaseDatabase.getInstance().getReference().child("notes").child(mAuth.getCurrentUser().getUid());
        mRef=FirebaseDatabase.getInstance().getReference().child("star_marked").child(mAuth.getCurrentUser().getUid()).child("important_notes");

        mprogress=new ProgressDialog(this);
        mToolbar=findViewById(R.id.selected_books_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar ab=getSupportActionBar();
        ab.setTitle("Selected Books");
        ab.setDisplayHomeAsUpEnabled(true);

        mprogress.setTitle("Loading...");
        mprogress.setMessage("Please wait while loading your books...");
        mprogress.show();

        mRecyclerView=findViewById(R.id.selected_books_recyclerView);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(mNoteRef,Model.class)
                .build();

        FirebaseRecyclerAdapter<Model, SelectedBooks.NotesViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Model, SelectedBooks.NotesViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull SelectedBooks.NotesViewHolder holder, int position, @NonNull Model model) {
                final String postKey=getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setNote(model.getNote());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(SelectedBooks.this,AddNotesActivity.class);
                        intent.putExtra("postKey",postKey);
                        intent.putExtra("uniqueKey","from_main");
                        startActivity(intent);
                    }
                });

                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent=new Intent(SelectedBooks.this,ReaderActivity.class);
                        startActivity(intent);
                        return false;
                    }
                });
            }

            @NonNull
            @Override
            public SelectedBooks.NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mprogress.dismiss();

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                SelectedBooks.NotesViewHolder viewHolder=new SelectedBooks.NotesViewHolder(view);
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
}
