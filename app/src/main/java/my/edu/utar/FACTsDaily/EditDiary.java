package my.edu.utar.FACTsDaily;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditDiary extends AppCompatActivity {
    private EditText eTitle;
    private EditText eDetail;
    private ImageView eImage;
    private static Bitmap photo;
    private byte[] imageVar;
    private String imageUri;
    private Uri imageUril;
    private ImageButton happyBtn, modeBtn, sadBtn, editBtn;
    private String diaryTitleStore;
    private String diaryDetailStore;
    private FirebaseStorage stDiary;
    private StorageReference diaryStRef;
    private Date eDate;
    private String emoji;
    private DatabaseReference diaryDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_diary);

        //--(ab1)
        ImageButton backbtn = findViewById(R.id.exit_btn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //--(ab2)
        ImageButton saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditDiary.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure want to update it?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSaveFunc();
                        Intent intToBack = new Intent(EditDiary.this, CalendarActivity.class);
                        Toast.makeText(EditDiary.this, "Diary saved successfully", Toast.LENGTH_SHORT).show();
                        startActivity(intToBack);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });


        //--(con21)
        Date reDate = (Date) getIntent().getSerializableExtra("DATE");
        Calendar ca = Calendar.getInstance();
        ca.setTime(reDate);

        //--(con20)
        eTitle = findViewById(R.id.edit_title);
        eDetail = findViewById(R.id.edit_details);
        eImage = findViewById(R.id.edit_upload_img_view);
        editBtn = findViewById(R.id.edit_emo_btn);

        //--(con1)
        Button selDate = findViewById(R.id.date_pick);
        selDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //--(con16)
                int year = ca.get(Calendar.YEAR);
                int month = ca.get(Calendar.MONTH);
                int day = ca.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpDialog = new DatePickerDialog(EditDiary.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                eDate = ca.getTime();
                            }
                        }, year, month, day);
                dpDialog.getDatePicker().setMinDate(ca.getTimeInMillis());
                dpDialog.getDatePicker().setMaxDate(ca.getTimeInMillis());
                dpDialog.show();
            }
            //--(QST4):
            public void dateSelUser(DatePicker view, int year, int month, int day){
            }
        });

        //--(con8)
        int numHappyBtnClicked = 0;
        int numModeBtnClicked = 0;
        int numSadBtnClicked = 0;

        happyBtn = findViewById(R.id.happy_emo_btn);
        modeBtn = findViewById(R.id.moderate_emo_btn);
        sadBtn = findViewById(R.id.sad_emo_btn);

        if(numHappyBtnClicked == 0 && numModeBtnClicked == 0 && numSadBtnClicked == 0){
            happyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emoji = "happy";
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_happy_emo);
                    editBtn.setImageDrawable(drawable);
                }
            });

            modeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emoji = "moderate";
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_moderate_emo);
                    editBtn.setImageDrawable(drawable);
                }
            });

            sadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emoji = "sad";
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_sad_emo);
                    editBtn.setImageDrawable(drawable);
                }
            });
        }




        long selectedDateInMillis = ca.getTimeInMillis();
        FirebaseDatabase.getInstance().getReference("diaries")
                .orderByChild("date/time")
                .startAt(selectedDateInMillis)
                .endAt(selectedDateInMillis + (24 * 60 * 60 * 1000) - 1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for(DataSnapshot diarySnap : snapshot.getChildren()){
                            Diary diary = diarySnap.getValue(Diary.class);
                            String title = diary.getTitle();
                            String detail = diary.getDetail();
                            imageUri = diary.getImageUpload();

                            eTitle.setText(title);
                            eDetail.setText(detail);
                            eDate = diary.getDate();
                            emoji = diary.getEmoji();

                            if(emoji == null){
                                editBtn.setImageDrawable(null);
                            }
                            else if(emoji.equals("happy")){
                                Drawable drawable = getResources().getDrawable(R.drawable.ic_happy_emo);
                                editBtn.setImageDrawable(drawable);
                            }else if(emoji.equals("moderate")){
                                Drawable drawable = getResources().getDrawable(R.drawable.ic_moderate_emo);
                                editBtn.setImageDrawable(drawable);
                            }else if(emoji.equals("sad")){
                                Drawable drawable = getResources().getDrawable(R.drawable.ic_sad_emo);
                                editBtn.setImageDrawable(drawable);
                            }

                            if (imageUri != null && !imageUri.isEmpty()) {
                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                StorageReference imgRef = storageRef.child(imageUri);
                                Log.d("DiaryReview", imageUri);
                                Picasso.get().load(imageUri).into(eImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

        //--(con4)
        Button uploadBtn = findViewById(R.id.upload_img_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent upIntent = new Intent(Intent.ACTION_PICK);
                upIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(upIntent, 1000);
            }
        });

        //--(con5)
        Button camBtn = findViewById(R.id.upload_cam_btn);
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(EditDiary.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(EditDiary.this, new String[]{Manifest.permission.CAMERA}, 100);
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 22);
            }
        });
    }
    //--(con6)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 22 && resultCode == RESULT_OK){
            photo = (Bitmap) data.getExtras().get("data");
            eImage.setImageBitmap(photo);
            eImage.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            imageVar = saveImgInVar(photo);
            imageUril = data.getData();
        }else if(requestCode == 1000 && resultCode == RESULT_OK){
            imageUril = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUril);
                photo = BitmapFactory.decodeStream(inputStream);
                eImage.setImageBitmap(photo);
                eImage.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                imageVar = saveImgInVar(photo);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else
        {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //--(con10)
    private byte[] saveImgInVar(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    private void onSaveFunc(){
        //--(con9)
        diaryTitleStore = eTitle.getText().toString();
        diaryDetailStore = eDetail.getText().toString();

        //--(con12)
        if(photo != null) {
            stDiary = FirebaseStorage.getInstance();
            diaryStRef = stDiary.getReference();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "IMG_" + timestamp + ".jpg";
            StorageReference imageRef = diaryStRef.child("images/" + filename);
            byte[] imageData = saveImgInVar(photo);
            UploadTask upTask = imageRef.putBytes(imageData);

            upTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NotNull Exception e) {
                    Toast.makeText(EditDiary.this, "Image error uploading: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Diary diary = new Diary(diaryTitleStore, diaryDetailStore, eDate, emoji, uri.toString());
                            diaryDatabase = FirebaseDatabase.getInstance().getReference();
                            diaryDatabase.child("diaries").push().setValue(diary);
                            Log.d("MainActivity", "Save button clicked (image uploaded)");
                        }
                    });
                }
            });
        }else{
            Diary diary = new Diary(diaryTitleStore, diaryDetailStore, eDate, emoji, imageUri);
            diaryDatabase = FirebaseDatabase.getInstance().getReference();
            diaryDatabase.child("diaries").push().setValue(diary);
            Log.d("MainActivity", "Save button clicked (no uploaded image)" + eDate);
        }

    }

}