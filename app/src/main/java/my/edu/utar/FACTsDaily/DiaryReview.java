package my.edu.utar.FACTsDaily;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class DiaryReview extends AppCompatActivity {

    //--(con16)
    private TextView drTitle;
    private TextView drDetail;
    private ImageButton drEmoji;
    private ImageView upImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_review);

        //--(ab1)
        ImageButton backbtn = findViewById(R.id.exit_btn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //--(con16)
        drTitle = findViewById(R.id.diary_title_text);
        drDetail = findViewById(R.id.diary_content_text);
        drEmoji = findViewById(R.id.emo_btn);
        upImage = findViewById(R.id.upload_img_view);


        Date reDate = (Date) getIntent().getSerializableExtra("DATE");
        Calendar ca = Calendar.getInstance();
        ca.setTime(reDate);


        long selectedDateInMillis = ca.getTimeInMillis();

        FirebaseDatabase.getInstance().getReference("diaries")
                .orderByChild("date/time")
                .startAt(selectedDateInMillis)
                .endAt(selectedDateInMillis + (24 * 60 * 60 * 1000) - 1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot snapshot) {
                        for(DataSnapshot diarySnapshot : snapshot.getChildren()){
                            Diary diary = diarySnapshot.getValue(Diary.class);
                            String title = diary.getTitle();
                            String detail = diary.getDetail();
                            String emoji = diary.getEmoji();
                            String imageUri = diary.getImageUpload();

                            drTitle.setText(title);
                            drDetail.setText(detail);

                            if(emoji == null){
                                drEmoji.setImageDrawable(null);
                            }
                            else if(emoji.equals("happy")){
                                Drawable drawable = getResources().getDrawable(R.drawable.ic_happy_emo);
                                drEmoji.setImageDrawable(drawable);
                            }else if(emoji.equals("moderate")){
                                Drawable drawable = getResources().getDrawable(R.drawable.ic_moderate_emo);
                                drEmoji.setImageDrawable(drawable);
                            }else if(emoji.equals("sad")){
                                Drawable drawable = getResources().getDrawable(R.drawable.ic_sad_emo);
                                drEmoji.setImageDrawable(drawable);
                            }

                            //--(con17)
                            if (imageUri != null && !imageUri.isEmpty()) {
                                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                                StorageReference imgRef = storageRef.child(imageUri);
                                Log.d("DiaryReview", imageUri);
                                Picasso.get().load(imageUri).into(upImage);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("DiaryReview", "Error retrieving diaries: " + error.getMessage());
                    }
                });


        //--(con1)
        Button selDate = findViewById(R.id.date_pick);
        selDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //--(con16)
                int year = ca.get(Calendar.YEAR);
                int month = ca.get(Calendar.MONTH);
                int day = ca.get(Calendar.DAY_OF_MONTH);

                long selectedDateInMillis = ca.getTimeInMillis();

                DatePickerDialog dpDialog = new DatePickerDialog(DiaryReview.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // TODO: handle selected date
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

        //--(con19)
        ImageButton editBtn = findViewById(R.id.edit_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intToEdit = new Intent(DiaryReview.this, EditDiary.class);
                intToEdit.putExtra("DATE", reDate);
                startActivity(intToEdit);
            }
        });
    }
}