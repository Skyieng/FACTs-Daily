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
import android.provider.MediaStore;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewDiary extends AppCompatActivity{

    //--(con4)
    private final int UPIMG_REQ_CODE = 1000;
    //--(con5)
    public static final int MY_PERMISSION_REQUEST_CAMERA = 100;
    private static final int REQUEST_CODE = 22;
    private ImageView imgUp;
    private static Bitmap photo;
    //--(con6)
    private byte[] imageVar;
    private Uri imageUri;
    //--(con1)
    private int sYear = 0, sMonth = 0, sDay = 0;
    private Date sDate;
    //--(con8)
    private String emoji;
    private ImageButton happyBtn, modeBtn, sadBtn, editBtn;
    //--(con9) --(con20)
    private EditText diaryTitle;
    private EditText diaryDetail;
    private String diaryTitleStore;
    private String diaryDetailStore;
    //--(con10)
    private DatabaseReference diaryDatabase;

    //--(con12)
    private FirebaseStorage stDiary;
    private StorageReference diaryStRef;

    //--(con20)
    private ImageButton drEmoji;
    private ImageView upImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dairy);

        //--(con9)
        diaryTitle = findViewById(R.id.edit_diary_title);
        diaryDetail = findViewById(R.id.edit_diary_details);
        editBtn = findViewById(R.id.edit_emo_btn);
        imgUp = findViewById(R.id.m_upload_img_view);



        //--(con10)
        diaryDatabase = FirebaseDatabase.getInstance().getReference();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(NewDiary.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure want to update it?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSaveFunc();
                        Intent intToBack = new Intent(NewDiary.this, CalendarActivity.class);
                        Toast.makeText(NewDiary.this, "Diary saved successfully", Toast.LENGTH_SHORT).show();
                        startActivity(intToBack);
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });

        //--(con18)
        Date reDate = (Date) getIntent().getSerializableExtra("DATE");
        Calendar ca = Calendar.getInstance();
        ca.setTime(reDate);
        sDate = ca.getTime();
        Log.d("MainActivity", ca.getTime().toString());


        //--(con20)
        upImage = findViewById(R.id.m_upload_img_view);


        //--(con1)
        Button selDate = findViewById(R.id.date_pick);
        selDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = ca.get(Calendar.YEAR);
                int month = ca.get(Calendar.MONTH);
                int day = ca.get(Calendar.DAY_OF_MONTH);

                if(sYear != 0 && sMonth != 0 && sDay != 0) {
                    year = sYear;
                    month = sMonth;
                    day = sDay;
                }

                DatePickerDialog dpDialog = new DatePickerDialog(NewDiary.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                ca.set(Calendar.YEAR, selectedYear);
                                ca.set(Calendar.MONTH, selectedMonth);
                                ca.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

                                sYear = selectedYear;
                                sMonth = selectedMonth;
                                sDay = selectedDayOfMonth;

                                sDate = ca.getTime();
                                Log.d("MainActivity", sDate.toString());
                            }
                        }, year, month, day);
                dpDialog.getDatePicker().setMinDate(ca.getTimeInMillis());
                dpDialog.getDatePicker().setMaxDate(ca.getTimeInMillis());
                dpDialog.show();
            }
            //--(QST4):
            public void dateSelUser(DatePicker view, int year, int month, int day){
                //code
            }
        });


        //--(con4)
        Button uploadBtn = findViewById(R.id.upload_img_btn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent upIntent = new Intent(Intent.ACTION_PICK);
                upIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(upIntent, UPIMG_REQ_CODE);
            }
        });

        //--(con5)
        Button camBtn = findViewById(R.id.upload_cam_btn);
        camBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(NewDiary.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(NewDiary.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CODE);
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
    }


    //--(con6)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            photo = (Bitmap) data.getExtras().get("data");
            imgUp.setImageBitmap(photo);
            imgUp.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            imageVar = saveImgInVar(photo);
            imageUri = data.getData();
        }else if(requestCode == UPIMG_REQ_CODE && resultCode == RESULT_OK){
            imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                photo = BitmapFactory.decodeStream(inputStream);
                imgUp.setImageBitmap(photo);
                imgUp.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
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



    //--(ab2): Define the save function
    //--(QST3):
    private void onSaveFunc(){
        //--(con9)
        diaryTitleStore = diaryTitle.getText().toString();
        diaryDetailStore = diaryDetail.getText().toString();

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
                    Toast.makeText(NewDiary.this, "Image error uploading: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Diary diary = new Diary(diaryTitleStore, diaryDetailStore, sDate, emoji, uri.toString());
                            diaryDatabase.child("diaries").push().setValue(diary);
                            Log.d("MainActivity", "Save button clicked (image uploaded)" + sDate);
                        }
                    });
                }
            });
        }else{
            Diary diary = new Diary(diaryTitleStore, diaryDetailStore, sDate, emoji, null);
            diaryDatabase.child("diaries").push().setValue(diary);
            Log.d("MainActivity", "Save button clicked (no uploaded image)" + sDate);
        }
    }
}