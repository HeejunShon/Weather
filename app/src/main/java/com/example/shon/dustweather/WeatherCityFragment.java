package com.example.shon.dustweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//도시이름으로 날씨 검색
public class WeatherCityFragment  extends Fragment {
    public static String BaseUrl = "http://api.openweathermap.org/"; //오픈웨더
    public static String AppId = "480a142cc42b0d4362295872f3109f66"; //앱키

    private TextView curTemp, weather, location, date, minTemp, maxTemp;
    private TextView wind, sunrise, maxtemp, humidity, sunset, mintemp;
    private ImageView weatherImg;

    //도시이름
    String cityStr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current_weather, container, false);

        curTemp = view.findViewById(R.id.Curtemp);
        weather = view.findViewById(R.id.weather_text);
        location = view.findViewById(R.id.loc_text);
        date = view.findViewById(R.id.date_text);
        minTemp = view.findViewById(R.id.min_text);
        maxTemp = view.findViewById(R.id.max_text);
        weatherImg = view.findViewById(R.id.weather_image);

        wind = view.findViewById(R.id.wind_text);
        sunrise = view.findViewById(R.id.sunRise_text);
        maxtemp = view.findViewById(R.id.maxTemp_text);
        humidity = view.findViewById(R.id.humidity_text);
        sunset = view.findViewById(R.id.sunSet_text);
        mintemp = view.findViewById(R.id.minTemp_text);

        //도시이름 받기
        Bundle bundle = this.getArguments();
        if(bundle != null){
            cityStr = bundle.getString("city", "Seoul");
            getCityWeather();
        }
        return view;
    }

    //도시 이름으로 날씨설정
    private void getCityWeather(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherCity city = retrofit.create(WeatherCity.class);
        Call<WeatherResponse> call = city.getCurrentWeatherData(cityStr, AppId);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if(response.code() == 200){
                    WeatherResponse weatherResponse = response.body();

                    //피카소로 openweathermap에서 현재 날씨 이미지 받아오기
                    String weatherStr = weatherResponse.weather.get(0).icon;
                    String imgUrl = "https://openweathermap.org/img/w/"+weatherStr+".png";
                    Picasso.get().load(imgUrl).into(weatherImg);

                    //현재기온
                    int cur = (int)(weatherResponse.main.temp - 273.15);
                    curTemp.setText(String.valueOf(cur)+ "℃");

                    //날씨
                    weather.setText(weatherResponse.weather.get(0).description);

                    //도시, 국가
                    String city = weatherResponse.name;
                    String country = weatherResponse.sys.country;
                    location.setText(city + ", " +country);

                    String dateStr = DayConverter.getDate((long)weatherResponse.dt);
                    date.setText(dateStr+"요일");

                    //최저기온, 최고기온
                    int min = (int)(weatherResponse.main.temp_min - 273.15);
                    int max = (int)(weatherResponse.main.temp_max- 273.15);
                    minTemp.setText(String.valueOf(min) + "℃");
                    maxTemp.setText(String.valueOf(max) + "℃");


                    //6아이콘 뷰
                    wind.setText("바람: " + weatherResponse.wind.speed+" m/s");
                    sunrise.setText("일출: " + DayConverter.getTime(weatherResponse.sys.sunrise));
                    maxtemp.setText("최고기온: " + String.valueOf(max) + "℃");
                    humidity.setText("습도: " + weatherResponse.main.humidity+"%");
                    sunset.setText("일몰: " + DayConverter.getTime(weatherResponse.sys.sunset));
                    mintemp.setText("최저기온: " + min +"℃");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {

            }
        });
    }


}

