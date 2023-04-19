package my.edu.utar.FACTsDaily;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todo-dae97-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText fullname = findViewById(R.id.fullname);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final EditText conpassword = findViewById(R.id.confirm_password);
        final TextView register_login = findViewById(R.id.register_login);
        final Button registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullnametxt = fullname.getText().toString();
                final String emailtxt = email.getText().toString();
                final String passwordtxt = password.getText().toString();
                final String conpasswordtxt = conpassword.getText().toString();

                if(fullnametxt.isEmpty() || emailtxt.isEmpty() || passwordtxt.isEmpty() || conpasswordtxt.isEmpty()){
                    Toast.makeText(RegisterActivity.this,"Please enter your fullname or email or password",Toast.LENGTH_SHORT).show();
                }

                else if (!passwordtxt.equals(conpasswordtxt)) {
                    Toast.makeText(RegisterActivity.this,"Password are not matching", Toast.LENGTH_SHORT).show();
                }

                else {
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if(snapshot.hasChild(fullnametxt)){
                                Toast.makeText(RegisterActivity.this,"Name has been registered",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                databaseReference.child("users").child(emailtxt).child("email").setValue(emailtxt);
                                databaseReference.child("users").child(emailtxt).child("password").setValue(passwordtxt);

                                Toast.makeText(RegisterActivity.this, "user registered", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });

                }
            }
        });

        register_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}