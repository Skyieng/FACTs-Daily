package my.edu.utar.FACTsDaily;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {
    //--(con13)
    private ImageButton viewDiaryBtn;
    //--(con15)
    private Date saveDate;

    private String conTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        //--(ab1)
        ImageButton backbtn = findViewById(R.id.exit_btn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //--(con15)
        TextView title = findViewById(R.id.diary_title_cal_text);
        conTitle = title.getText().toString();
            //--(con13)
            viewDiaryBtn = findViewById(R.id.view_diary);
            viewDiaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(conTitle.equals("No Title")) {
                        Intent intToDiary = new Intent(CalendarActivity.this, NewDiary.class);
                        intToDiary.putExtra("DATE", saveDate);
                        startActivity(intToDiary);
                    }else{
                        Intent intToDiary = new Intent(CalendarActivity.this, DiaryReview.class);
                        //--(con15)
                        intToDiary.putExtra("DATE", saveDate);
                        startActivity(intToDiary);
                    }
                }
            });

        //--(con14)
        CalendarView cv = findViewById(R.id.calendar_view);
        cv.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NotNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth-1);
                saveDate = cal.getTime();
                Log.d("CalendarActivity", saveDate.toString());
                long selectedDateInMillis = cal.getTimeInMillis();


                String diaryTitle = "No Title";
                String diaryDetail = "No Content";
                TextView title = findViewById(R.id.diary_title_cal_text);
                TextView detail = findViewById(R.id.diary_content_cal_text);
                title.setText(diaryTitle);
                detail.setText(diaryDetail);
                conTitle = "No Title";

                FirebaseDatabase.getInstance().getReference("diaries")
                        .orderByChild("date/time")
                        .startAt(selectedDateInMillis)
                        .endAt(selectedDateInMillis + (24 * 60 * 60 * 1000) - 1)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NotNull DataSnapshot snapshot) {
                                Log.d("CalendarActivity", "Retrieved " + snapshot.getChildrenCount() + " diaries");
                                for(DataSnapshot diarySnapshot : snapshot.getChildren()){
                                    Diary diary = diarySnapshot.getValue(Diary.class);
                                    if(diary != null) {
                                        Log.d("CalendarActivity", "Retrieve diary with title: " + diary.getTitle());
                                        String diaryTitle = diary.getTitle();
                                        String diaryDetail = diary.getDetail();
                                        TextView title = findViewById(R.id.diary_title_cal_text);
                                        TextView detail = findViewById(R.id.diary_content_cal_text);
                                        title.setText(diaryTitle);
                                        conTitle = diaryTitle;
                                        detail.setText(diaryDetail);

                                        //--(con15)
                                        saveDate = diary.getDate();
                                    } else {
                                        Log.e("CalendarActivity", "Diary object is null");
                                    }
                                }
                            }


                            @Override
                            public void onCancelled(@NotNull DatabaseError error) {
                                Log.e("CalendarActivity", "Error retrieving diaries: " + error.getMessage());
                            }
                        });

            }
        });

    }
}