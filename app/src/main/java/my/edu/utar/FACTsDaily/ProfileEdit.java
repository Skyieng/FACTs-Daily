package my.edu.utar.FACTsDaily;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileEdit extends AppCompatActivity {

    private ImageButton exit_btn;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button chooseImageButton;
    private ImageButton saveButton;
    private ImageView profileImageView;
    private Uri imageUri;
    private EditText nameEditText;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference database_reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        database_reference = FirebaseDatabase.getInstance().getReference("profile_info");

        exit_btn = findViewById(R.id.exit_to_profile);
        nameEditText = findViewById(R.id.profile_name_edit);

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ProfileEdit.this, Profile.class);
//                startActivity(intent);
                finish();
            }
        });

        chooseImageButton = findViewById(R.id.choose_image_button);
        saveButton = findViewById(R.id.profile_save);
        profileImageView = findViewById(R.id.profileEdit);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();

                String name = nameEditText.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(ProfileEdit.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                } else {
                    // Save name to database
                    database_reference.child("name").setValue(name);

                    Toast.makeText(ProfileEdit.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        profileImageView = findViewById(R.id.profileEdit);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        storageReference.child("profile_image.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .into(profileImageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child("profile_image." + getFileExtension(imageUri));

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Upload successful
                            Toast.makeText(ProfileEdit.this, "Upload Successful!! ", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Upload failed
                            Toast.makeText(ProfileEdit.this, "Upload Fail!! ", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}