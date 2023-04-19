package my.edu.utar.FACTsDaily;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FoodDetailActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todo-dae97-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        TextView ingredient_text = findViewById(R.id.ingredient_text);
        TextView methods = findViewById(R.id.cooking_step_text);
        TextView textView = findViewById(R.id.foodName);
        ImageButton backBtn = findViewById(R.id.exit_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String foodName = this.getIntent().getStringExtra("foodname");
        textView.setText(foodName);

        String ingredient = this.getIntent().getStringExtra("ingredients");
        ingredient_text.setText(ingredient);

        String method = this.getIntent().getStringExtra("method");
        methods.setText(method);

    }

}

