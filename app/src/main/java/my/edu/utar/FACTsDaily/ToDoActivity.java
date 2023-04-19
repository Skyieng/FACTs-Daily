package my.edu.utar.FACTsDaily;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ToDoActivity extends AppCompatActivity {
    private EditText editText;
    private ImageButton add_button;
    private LinearLayout taskList;
    private DatabaseReference database_reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database_reference = FirebaseDatabase.getInstance().getReference("tasks");

        editText = findViewById(R.id.editText);
        add_button = findViewById(R.id.addButton);
        taskList = findViewById(R.id.taskList);

        database_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.removeAllViews();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    CheckBox checkBox = new androidx.appcompat.widget.AppCompatCheckBox(ToDoActivity.this) {
                        @Override
                        public boolean performClick() {
                            setChecked(!isChecked());
                            return super.performClick();
                        }
                    };
                    checkBox.setText(task.getText());
                    checkBox.setChecked(task.isCompleted());
                    checkBox.setOnCheckedChangeListener((compoundButton, checked) -> {
                        if (checked) {

                            Toast.makeText(ToDoActivity.this, "Task Completed", Toast.LENGTH_SHORT).show();
                            database_reference.child(task.getId()).removeValue();
                            taskList.removeView(compoundButton);

                        } else {
                            taskList.removeView(compoundButton);
                            taskList.addView(compoundButton, 0);
                        }
                    });

                    checkBox.setOnTouchListener(new View.OnTouchListener() {
                        float startX;
                        boolean isSwipe;

                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            switch (motionEvent.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    startX = motionEvent.getX();
                                    isSwipe = false;
                                    return true;
                                case MotionEvent.ACTION_MOVE:
                                    if (motionEvent.getX() < startX - 50) {
                                        isSwipe = true;
                                        checkBox.setBackgroundColor(Color.RED);
                                        Toast.makeText(ToDoActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();

                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                    if (isSwipe) {
                                        // user swiped left, remove the task
                                        database_reference.child(task.getId()).removeValue();
                                        taskList.removeView(checkBox);
                                    } else {
                                        // checkbox clicked, handle click event
                                        checkBox.setChecked(!checkBox.isChecked());
                                    }

                                    break;
                            }
                            return false;
                        }
                    });

                    taskList.addView(checkBox, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ToDoActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        add_button.setOnClickListener(view -> {
            String taskText = editText.getText().toString().trim();

            if (!taskText.isEmpty()) {
                addTask(taskText);
                editText.setText("");
            }
        });

    }

    private void addTask(String taskText) {
        String taskId = database_reference.push().getKey();
        Task task = new Task(taskText, false, taskId);
        database_reference.child(taskId).setValue(task);
    }
}