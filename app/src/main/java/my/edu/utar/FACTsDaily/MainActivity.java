package my.edu.utar.FACTsDaily;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private LinearLayout layoutIndicator;
    MyFragmentStateAdapter myFragmentStateAdapter;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton profile = findViewById(R.id.profileBtn);
        ImageButton calendar = findViewById(R.id.calenderBtn);
        ImageButton addDiary = findViewById(R.id.addDiaryBtn);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivity(intent);
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        addDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewDiary.class);
                Calendar ca = Calendar.getInstance();
                Date currentDate = ca.getTime();
                intent.putExtra("DATE", currentDate);
                startActivity(intent);
            }
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (!isLoggedIn) {
            // User is not logged in, redirect to Login Activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {

            viewPager2 = findViewById(R.id.viewPager2);
            layoutIndicator = findViewById(R.id.layoutIndicator);

            myFragmentStateAdapter = new MyFragmentStateAdapter(this);
            myFragmentStateAdapter.createFragment(1);
            viewPager2.setAdapter(myFragmentStateAdapter);
            viewPager2.setCurrentItem(1);

            setupIndicators();
            setCurrentIndicator(1);

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    setCurrentIndicator(position);
                }
            });
        }
    }

    private void setupIndicators(){
        ImageView[] indicators = new ImageView[myFragmentStateAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8,0,8,0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutIndicator.addView(indicators[i]);
        }
    }

    private void setCurrentIndicator(int index){
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i<childCount; i++){
            ImageView imageView = (ImageView) layoutIndicator.getChildAt(i);
            if (i == index){
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.indicator_active)
                );
            }else {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.indicator_inactive)
                );
            }
        }
    }

}
