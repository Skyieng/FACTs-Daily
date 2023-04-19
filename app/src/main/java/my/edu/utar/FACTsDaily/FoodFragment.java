package my.edu.utar.FACTsDaily;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FoodFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FoodFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://todo-dae97-default-rtdb.firebaseio.com/");
    ArrayList<String> nameAL = new ArrayList<String>();
    ArrayList<String> foodAL = new ArrayList<String>();
    ArrayList<Integer> ratingAL = new ArrayList<Integer>();
    ArrayList<String> ingredientArr = new ArrayList<String>();
    ArrayList<String> methodArr = new ArrayList<String>();
    int counter = 0;

    public FoodFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FoodFragment newInstance(String param1, String param2) {
        FoodFragment fragment = new FoodFragment();
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
        return inflater.inflate(R.layout.fragment_food, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textView = getView().findViewById(R.id.foodName);
        ImageView food_refresh = getView().findViewById(R.id.refresh);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),FoodDetailActivity.class);
                i.putExtra("foodname",nameAL.get(counter));
                i.putExtra("ingredients",ingredientArr.get(counter));
                i.putExtra("method", methodArr.get(counter));
                startActivity(i);

            }
        });

        TextView foodname = getView().findViewById(R.id.foodName);
        TextView rating = getView().findViewById(R.id.rating);
        ImageView foodimage = getView().findViewById(R.id.foodImage);

        food_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter ++;
                if (counter == foodAL.size()){
                    counter = 0;
                }
                foodname.setText(nameAL.get(counter));
                rating.setText(ratingAL.get(counter)+" ");
                Picasso.get().load(foodAL.get(counter)).into(foodimage);
            }
        });

        FirebaseDatabase.getInstance().getReference("Food").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i < snapshot.getChildrenCount() + 1; i++) {
                    String image = snapshot.child("Food" + i).child("image").getValue(String.class);
                    String name = snapshot.child("Food" + i).child("name").getValue(String.class);
                    Integer rate = snapshot.child("Food" + i).child("rate").getValue(Integer.class);
                    String ingredient = snapshot.child("Food" + i).child("ingredients")
                            .getValue(String.class).replace("\\n","\n");
                    String method = snapshot.child("Food" + i).child("method")
                            .getValue(String.class).replace("\\n","\n");
                    nameAL.add(name);
                    foodAL.add(image);
                    ratingAL.add(rate);
                    ingredientArr.add(ingredient);
                    methodArr.add(method);
                }

                foodname.setText(nameAL.get(0));
                rating.setText(ratingAL.get(0)+" ");
                Picasso.get().load(foodAL.get(0)).into(foodimage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error Loading Image",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}