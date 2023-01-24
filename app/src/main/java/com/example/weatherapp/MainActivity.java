package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityName, temparatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdit;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdaptor weatherRVAdaptor;
    private LocationManager locationManager;
    private int PERMISSION = -1;
    private String CityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.Home);
        loadingPB = findViewById(R.id.Loading);
        cityName = findViewById(R.id.CityName);
        temparatureTV= findViewById(R.id.Temperature);
        conditionTV = findViewById(R.id.Condition);
        weatherRV = findViewById(R.id.RvWeather);
        cityEdit = findViewById(R.id.IdCity);
        backIV = findViewById(R.id.Black);
        iconIV = findViewById(R.id.IVIcon);
        searchIV = findViewById(R.id.Search);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdaptor = new WeatherRVAdaptor(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdaptor);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED);
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        CityName = getCityName(location.getLatitude(),location.getLongitude());
        getWeatherInfo(CityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdit.getText().toString();
                    if(city.isEmpty()){
                        Toast.makeText(MainActivity.this,"Please Enter City Name !",Toast.LENGTH_SHORT).show();
                    }else{
                        cityName.setText(CityName);
                        getWeatherInfo(city);
                    }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this,"Permission Granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this,"Please give permissions !",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double latitude, double longitude){
        String cityName = "Not found";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,10);
            for(Address addr: addressList){
                if(addr!=null){
                    String city = addr.getLocality();
                    if(city!=null && city.equals("")){
                        cityName = city;
                    }else{
                        Log.d("TAG","CITY NOT FOUND");
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }return cityName;
    }

    private void getWeatherInfo(String cityName1){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=21c71cc823ff499e83f73947232401&q="+cityName1+"&days=1&aqi=no&alerts=no";
        cityName.setText(cityName1);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temparatureTV.setText(temperature+"C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");

                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1){
                        Picasso.get().load("https://unsplash.com/photos/iAk_yM7r8iE").into(backIV);
                    }else{
                        Picasso.get().load("https://unsplash.com/photos/ilfsT5p_qvA").into(backIV);
                    }
                    JSONObject forecastObj = response.getJSONObject("foecast");
                    JSONObject forcast0 = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forcast0.getJSONArray("hour");
                    for(int i=0;i< hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temp = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String windSpe = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time,temp,img,windSpe));
                    }
                    weatherRVAdaptor.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please Enter Valid City Name !",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}