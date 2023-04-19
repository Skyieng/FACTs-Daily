package my.edu.utar.FACTsDaily;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WeatherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    TextView clockTime, date, locationTv, weatherTv, reminderTv;
    ImageView weatherIconImage;
    ImageButton refreshButton, speakerButton;
    double latitude, longitude, temperature;
    String apiKey = "R3CjFd5pNg1wzz8u54OSPn4pP1h3wppy";

    String locationKey, weatherText, mukim, state, locationConcate, text, reminderStr;
    int weatherIcon;
    FusedLocationProviderClient fusedLocationClient;
    LocationManager locationManager;
    private TextToSpeech tts;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<String> quote = new ArrayList<String>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WeatherFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomepageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherFragment newInstance(String param1, String param2) {
        WeatherFragment fragment = new WeatherFragment();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clockTime = getView().findViewById(R.id.clockTextView);
        date = getView().findViewById(R.id.dateTextView);
        locationTv = getView().findViewById(R.id.locationTextView);
        weatherTv = getView().findViewById(R.id.weatherTextView);
        weatherIconImage = getView().findViewById(R.id.imageView);
        refreshButton = getView().findViewById(R.id.refreshButton);
        speakerButton = getView().findViewById(R.id.speakerButton);
        reminderTv = getView().findViewById(R.id.reminderTv);
        refreshButton.setVisibility(View.INVISIBLE);

        quote.add("Enjoy Your New Day!");
        quote.add("Good Luck and Have a Good One!");
        quote.add("Have a Awesome Day!");
        quote.add("Have a Great Day!");
        quote.add("Wishing you a fantastic day!");

        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        tts = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        speakerButton.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
        tts.setPitch(1.1f);
        tts.setSpeechRate(0.5f);

        Handler handler = new Handler();
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d,''yy ");
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a", Locale.getDefault());
                DateFormatSymbols symbols = new DateFormatSymbols();
                symbols.setAmPmStrings(new String[] {"AM", "PM"});
                sdf.setDateFormatSymbols(symbols);
                String dateString = dateFormat.format(currentDate);
                String currentTime = sdf.format(calendar.getTime());
                getActivity().runOnUiThread(() -> {
                    clockTime.setText(currentTime);
                    date.setText(dateString);
                });
                handler.postDelayed(this, 1000); // Update the clock every second
            }
        };

        handler.post(updateTimeRunnable);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            showAlertMessageLocationDisabled();
            refreshButton.setOnClickListener(v -> getLocation());
        }
    }

    private void showAlertMessageLocationDisabled() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Device location is turned off.\nDo you want to turn on Location?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            refreshButton.setVisibility(View.VISIBLE);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            refreshButton.setVisibility(View.VISIBLE);
            dialog.cancel();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        } else{
            {
                Toast.makeText(getActivity(), "Location is required to retrieve location and weather", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        updateUI(location);
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                    }

                }
            });
        }
    }

    private void updateUI(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        new Thread() {
            public void run() {
                //Get location key
                URL url;
                try {
                    url = new URL("http://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey="+apiKey+"&q="+latitude+","+longitude);
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod("GET");
                    hc.connect();

                    InputStream inputStream = hc.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();

                    bufferedReader.close();

                    JSONObject locationJsonObject = new JSONObject(response);
                    locationKey = locationJsonObject.getString("Key");
                    mukim = locationJsonObject.getString("LocalizedName");
                    state = locationJsonObject.getJSONObject("AdministrativeArea").getString("LocalizedName");
                    locationConcate = mukim + ", " + state;

                    url = new URL("http://dataservice.accuweather.com/currentconditions/v1/"+locationKey+"?apikey="+apiKey);
                    hc = (HttpURLConnection) url.openConnection();
                    hc.connect();
                    InputStream inputStream1 = hc.getInputStream();
                    BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1));
                    StringBuilder responseBuilder1 = new StringBuilder();
                    while ((line = bufferedReader1.readLine()) != null) {
                        responseBuilder1.append(line);
                    }
                    bufferedReader1.close();

                    JSONArray weatherJsonArray = new JSONArray(responseBuilder1.toString());
                    JSONObject weatherJsonObject = weatherJsonArray.getJSONObject(0);
                    weatherText = weatherJsonObject.getString("WeatherText");
                    weatherIcon = weatherJsonObject.getInt("WeatherIcon");
                    temperature = weatherJsonObject.getJSONObject("Temperature").getJSONObject("Metric").getDouble("Value");
                    String weatherTemperatureCelcius = weatherText+", "+temperature+" \u2103";
                    String iconName = String.format("%02d-s", weatherIcon);
                    String iconUrl = String.format("https://developer.accuweather.com/sites/default/files/%s.png", iconName);

                    url = new URL("http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/"+locationKey+"?apikey="+apiKey);
                    hc = (HttpURLConnection) url.openConnection();
                    hc.connect();
                    InputStream inputStream2 = hc.getInputStream();
                    BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                    StringBuilder responseBuilder2 = new StringBuilder();
                    while ((line = bufferedReader2.readLine()) != null) {
                        responseBuilder2.append(line);
                    }
                    bufferedReader2.close();

                    JSONArray forecastJsonArray = new JSONArray(responseBuilder2.toString());

                    Intent intent = new Intent(getActivity(), WeatherForecast.class);

                    getActivity().runOnUiThread(() -> {
                        refreshButton.setVisibility(View.INVISIBLE);
                        locationTv.setText(locationConcate);
                        weatherTv.setText(weatherTemperatureCelcius);
                        Picasso.get().load(iconUrl).into(weatherIconImage);

                        locationTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(intent);
                            }
                        });
                        weatherTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(intent);
                            }
                        });

                        for (int i = 0; i<12; i++){
                            JSONObject forecastObject = null;
                            try {
                                forecastObject = forecastJsonArray.getJSONObject(i);
                                int probability = forecastObject.getInt("PrecipitationProbability");
                                String weather = forecastObject.getString("IconPhrase");
                                if (probability >= 50 || weather.contains("Sunny")){
                                    reminderStr = "Recommended to carry an umbrella!";
                                    break;
                                }else{
                                    Random rand = new Random();
                                    reminderStr = quote.get(rand.nextInt(quote.size()));;
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        reminderTv.setText(reminderStr);

                        SimpleDateFormat sdf = new SimpleDateFormat("d 'of' MMMM");
                        String formattedDate = sdf.format(new Date());

                        text = "Today is " + formattedDate + "\n" +
                                "The current weather condition is " + weatherText+ "\n"+
                                "The temperature is " + temperature +"celcius" + "\n"+
                                reminderStr;
                    });
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                } catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}