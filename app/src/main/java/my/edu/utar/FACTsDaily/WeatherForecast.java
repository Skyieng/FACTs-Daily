package my.edu.utar.FACTsDaily;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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
import java.util.ArrayList;
import java.util.List;

public class WeatherForecast extends AppCompatActivity {
    TextView currentCondition, currentLocation, currentTemperature;
    ImageButton refreshButton, backBtn;
    final List<TextView> tvTimeList = new ArrayList<>();
    final List<TextView> tvTempList = new ArrayList<>();
    final List<ImageView> ivList = new ArrayList<>();
    ImageView weatherIconImage;
    String apiKey = "R3CjFd5pNg1wzz8u54OSPn4pP1h3wppy";
    String locationKey, weatherText, mukim, state, country;
    double latitude, longitude, temperature;
    int weatherIcon, probability;
    FusedLocationProviderClient fusedLocationClient;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherforecast);
        currentCondition = findViewById(R.id.weatherTextView);
        currentLocation = findViewById(R.id.textView2);
        currentTemperature = findViewById(R.id.textView3);
        weatherIconImage = findViewById(R.id.imageView);
        refreshButton = findViewById(R.id.imageButton6);
        refreshButton.setVisibility(View.INVISIBLE);

        for (int i = 5; i < 17; i++) {
            String tvTimeID = "textView" + i;
            String tvTempID = "textView" + i + "temp";
            String ivID = "imageView" + i;
            int resTimeID = getResources().getIdentifier(tvTimeID, "id", getPackageName());
            int resTempID = getResources().getIdentifier(tvTempID, "id", getPackageName());
            int resIvID = getResources().getIdentifier(ivID, "id", getPackageName());
            TextView tvTime = findViewById(resTimeID);
            TextView tvTemp = findViewById(resTempID);
            ImageView iv = findViewById(resIvID);
            tvTimeList.add(tvTime);
            tvTempList.add(tvTemp);
            ivList.add(iv);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            showAlertMessageLocationDisabled();
            refreshButton.setOnClickListener(v -> getLocation());
        }

        backBtn = findViewById(R.id.exit_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showAlertMessageLocationDisabled() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Device location is turned off.\nDo you want to turn on Location?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            refreshButton.setVisibility(View.VISIBLE);
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
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
                Toast.makeText(WeatherForecast.this, "Location is Required: Please enable location from Settings", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getLocation(){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null){
                        updateUI(location);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},10);
            }
    }

    private void updateUI(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        new Thread(){
            public void run(){
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

                    JSONObject locationJsonObject = new JSONObject(response);
                    locationKey = locationJsonObject.getString("Key");
                    mukim = locationJsonObject.getString("LocalizedName");
                    state = locationJsonObject.getJSONObject("AdministrativeArea").getString("LocalizedName");
                    country = locationJsonObject.getJSONObject("Country").getString("ID");
                    String location = mukim+", "+state+", "+country;

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
                    String temperatureCelcius = temperature +" \u2103";
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

                    runOnUiThread(() -> {
                        refreshButton.setVisibility(View.INVISIBLE);
                        currentCondition.setText(weatherText);
                        currentLocation.setText(location);
                        currentTemperature.setText(temperatureCelcius);
                        Picasso.get().load(iconUrl).into(weatherIconImage);

                        for (int i = 5; i<17; i++) {
                            try {
                                JSONObject forecastObject = forecastJsonArray.getJSONObject(i-5);

                                String dateTime = forecastObject.getString("DateTime");
                                String extractDateTime = dateTime.substring(dateTime.indexOf("T")+1, dateTime.indexOf(":"));
                                if (Integer.parseInt(extractDateTime) > 12){
                                    extractDateTime = Integer.parseInt(extractDateTime) - 12 + " pm";
                                }
                                else if (extractDateTime.equals("00")){
                                    extractDateTime = "12 am";
                                }
                                else{
                                    extractDateTime = extractDateTime + " am";
                                }

                                if (extractDateTime.startsWith("0"))
                                    extractDateTime = extractDateTime.substring(1);

                                tvTimeList.get(i-5).setText(extractDateTime);

                                String iconName1 = String.format("%02d-s", forecastObject.getInt("WeatherIcon"));
                                String iconUrl1 = String.format("https://developer.accuweather.com/sites/default/files/%s.png", iconName1);
                                Picasso.get().load(iconUrl1).into(ivList.get(i-5));

                                temperature = Double.parseDouble(Double.toString(forecastObject.getJSONObject("Temperature").getDouble("Value")));
                                temperature = Math.round((temperature - 32) * 5/9 *10)/10.0;

                                probability = forecastObject.getInt("PrecipitationProbability");
                                String TemperaturePrecipitation = temperature +" \u2103"+ "    " + " \u2614"+"  "+probability+"%";
                                tvTempList.get(i-5).setText(TemperaturePrecipitation);

                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }
}