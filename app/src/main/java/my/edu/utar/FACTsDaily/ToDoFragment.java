package my.edu.utar.FACTsDaily;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ToDoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ToDoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText editText;
    private ImageButton add_button;
    private LinearLayout taskList;
    private DatabaseReference database_reference;
    private ImageButton profile_button;

    public ToDoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ToDoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ToDoFragment newInstance(String param1, String param2) {
        ToDoFragment fragment = new ToDoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_do, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database_reference = FirebaseDatabase.getInstance().getReference("tasks");

        editText = getView().findViewById(R.id.editText);
        add_button = getView().findViewById(R.id.addButton);
        taskList = getView().findViewById(R.id.taskList);

        database_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.removeAllViews();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    CheckBox checkBox = new androidx.appcompat.widget.AppCompatCheckBox(getActivity()) {
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

                            Toast.makeText(getActivity(), "Task Completed", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getActivity(), "Task Deleted", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskText = editText.getText().toString().trim();

                if (!taskText.isEmpty()) {
                    ToDoFragment.this.addTask(taskText);
                    editText.setText("");
                }
            }
        });
    }

    private void addTask(String taskText) {
        String taskId = database_reference.push().getKey();
        Task task = new Task(taskText, false, taskId);
        database_reference.child(taskId).setValue(task);
    }
}