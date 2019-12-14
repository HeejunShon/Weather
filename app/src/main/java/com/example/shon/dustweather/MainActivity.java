package com.example.shon.dustweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static String BaseUrl = "http://api.openweathermap.org/"; //오픈웨더
    public static String AppId = "480a142cc42b0d4362295872f3109f66"; //앱키
    public static double Latitude = 36.8504;
    public static double Longitude = 127.1501;

    private String provider; //위치 제공자
    double latitude; //위도
    double longitude; //경도

    // 사용자 위치 수신기
    private LocationManager locationManager;

    Toolbar myToolbar;
    WeatherFragment weatherFragment;
    WeatherCityFragment weatherCityFragment;
    Bundle bundle;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //툴바 추가
        myToolbar = (Toolbar)findViewById(R.id.mytoolbar);
        myToolbar.setTitle("Weather");
        setSupportActionBar(myToolbar);

        //위치 수신
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setLocation(getMyLocation());

        //위도, 경도 전달
        weatherFragment = new WeatherFragment();
        bundle = new Bundle();
        bundle.putDouble("lat", latitude);
        bundle.putDouble("lon", longitude);
        weatherFragment.setArguments(bundle);

        //프래그먼트 연결
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.weather_frame, weatherFragment);
        fragmentTransaction.commit();

    }


    //툴바
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("도시(영어)를 검색하세요..");

        //검색한 도시이름 WeatherCityFragment로 전달
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                weatherCityFragment = new WeatherCityFragment();

                bundle.clear();
                bundle.putString("city",query);
                weatherCityFragment.setArguments(bundle);

                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.weather_frame, weatherCityFragment);
                fragmentTransaction.commit();

                Toast.makeText(MainActivity.this, query, Toast.LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //GPS
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            provider = location.getProvider();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void setLocation(Location location){
        if(location != null)
        {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            provider = location.getProvider();
        }
    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);

            // 직접 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                latitude = currentLocation.getLatitude();
                longitude = currentLocation.getLongitude();
                Log.d("Main", "longtitude=" + longitude + ", latitude=" + latitude);
            }
        }
        return currentLocation;
    }


    /**
     * GPS 권한 응답에 따른 처리
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    boolean canReadLocation = false;
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        if (requestCode == 1001) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                Location userLocation = getMyLocation();
                if( userLocation != null ) {
                    double latitude = userLocation.getLatitude();
                    double longitude = userLocation.getLongitude();
                }
                canReadLocation = true;
            }
            else {
                // Permission was denied or request was cancelled
                canReadLocation = false;
            }
        }
    }


    //retrofit2
    //현재날씨
    void getCurrentData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeatherData(String.valueOf(latitude), String.valueOf(longitude), AppId);
    //절대온도 - 273.15 = 섭씨
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.code() == 200) {
                    WeatherResponse weatherResponse = response.body();
                    assert weatherResponse != null;

                    //테스트 출력용 스트링
                    String stringTest = "Country: " +
                            weatherResponse.sys.country +
                            "\n" +
                            "Temperature: " +
                            weatherResponse.main.temp +
                            "\n" +
                            "Temperature(Min): " +
                            weatherResponse.main.temp_min +
                            "\n" +
                            "Temperature(Max): " +
                            weatherResponse.main.temp_max +
                            "\n" +
                            "Humidity: " +
                            weatherResponse.main.humidity +
                            "\n" +
                            "Pressure: " +
                            weatherResponse.main.pressure;

                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
            }
        });
    }



}
