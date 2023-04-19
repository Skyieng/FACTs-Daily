package my.edu.utar.FACTsDaily;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {

    private ImageButton exit_btn;
    private ImageButton edit_profile_button;
    private ImageView profileImageView;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("profile_info/name");
        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                TextView nameTextView = findViewById(R.id.profile_name);
                nameTextView.setText(String.format("Name: %s", name));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        exit_btn = findViewById(R.id.exit_to_task);

        exit_btn.setOnClickListener(view -> {
//            Intent intent = new Intent(Profile.this, ToDoActivity.class);
//            startActivity(intent);
            finish();
        });

        edit_profile_button = findViewById(R.id.edit_profile_btn);

        edit_profile_button.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, ProfileEdit.class);
            startActivity(intent);
        });

        profileImageView = findViewById(R.id.profileImage);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        storageReference.child("profile_image.jpg").getDownloadUrl()
                .addOnSuccessListener(uri -> Picasso.get()
                        .load(uri)
                        .into(profileImageView)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
    }

    // Call this method to log out the user
    private void logOut() {
        saveLoggedInStatus(false);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveLoggedInStatus(boolean isLoggedIn) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
}
