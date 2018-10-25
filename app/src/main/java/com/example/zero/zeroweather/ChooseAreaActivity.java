package com.example.zero.zeroweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.zero.zeroweather.model.gson.Area;
import com.example.zero.zeroweather.model.db.City;
import com.example.zero.zeroweather.model.db.County;
import com.example.zero.zeroweather.model.db.Province;
import com.example.zero.zeroweather.model.gson.CountyWeather;
import com.example.zero.zeroweather.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class ChooseAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
        ListView listView = findViewById(R.id.areaListView);
        //如果还没省市县数据，则记得读取
//         requestArea();
    }
    /** 向服务器请求地区选择信息 */
    private void requestArea() {
        String address = "http://guolin.tech/api/china/";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            /** string不能调用2次，因为源码中调用了close()，这里应该clone出来，手动关闭 */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // request the entire body.
                Buffer buffer = source.buffer();
                // clone buffer before reading from it
                String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));
//                String responseData = response.body().string();
                response.close();
//                Log.d("deep", "onResponse: " + responseBodyString);
                parseProvince(responseBodyString);
            }
        });
    }

    /** 解析省级Json数据，并直接保存到数据库*/
    private void parseProvince(String jsonData) {
        Gson gson = new Gson();
        //解析数组要借助TypeToken将期望解析成的数据类型传入到fromJson中
        List<Area> areaList = gson.fromJson(jsonData,new TypeToken<List<Area>>(){}.getType());
        for(Area area:areaList){
            Province province = new Province();
            province.setProvinceCode(area.getId());
            province.setName(area.getName());
            province.save();
            //请求市级Json数据
            requestCity(area.getId());
        }
    }

    /** 请求市级Json数据 */
    private void requestCity(final int provinceId) {
        String address = "http://guolin.tech/api/china/" + provinceId;
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                String responseData = response.body().string();
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // request the entire body.
                Buffer buffer = source.buffer();
                // clone buffer before reading from it
                String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));
//                String responseData = response.body().string();
                response.close();
                parseCity(responseBodyString,provinceId);
            }
        });
    }

    /** 解析市级数据 */
    private void parseCity(String cityData,int provinceId) {
        Gson gson = new Gson();
        List<Area> areaList = gson.fromJson(cityData,new TypeToken<List<Area>>(){}.getType());
        for(Area area:areaList){
            City city = new City();
            city.setProvinceCode(provinceId);
            city.setName(area.getName());
            city.setCityCode(area.getId());
            city.save();
            //请求县级Json数据
            requestCounty(area.getId(),provinceId);

        }
    }

    /** 请求县级数据 */
    private void requestCounty(final int cityId, int provinceId) {
        String address = "http://guolin.tech/api/china/" + provinceId + "/" + cityId;
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                String countyData = response.body().string();
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // request the entire body.
                Buffer buffer = source.buffer();
                // clone buffer before reading from it
                String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));
//                String responseData = response.body().string();
                response.close();
                parseCounty(responseBodyString,cityId);
            }
        });
    }

    /** 解析县级数据 */
    private void parseCounty(String conutyData,int cityCode) {
        Gson gson = new Gson();
        List<CountyWeather> countyWeatherList = gson.fromJson(conutyData,new TypeToken<List<CountyWeather>>(){}.getType());
        for(CountyWeather countyWeather:countyWeatherList){
            County county = new County();
            county.setCityCode(cityCode);
            county.setCountyCode(countyWeather.getId());
            county.setName(countyWeather.getName());
            county.setWeatherId(countyWeather.getWeatherId());
            county.save();
        }
    }
}
