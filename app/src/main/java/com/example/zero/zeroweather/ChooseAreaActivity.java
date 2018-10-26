package com.example.zero.zeroweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.zero.zeroweather.model.gson.Area;
import com.example.zero.zeroweather.model.db.City;
import com.example.zero.zeroweather.model.db.County;
import com.example.zero.zeroweather.model.db.Province;
import com.example.zero.zeroweather.model.gson.CountyWeather;
import com.example.zero.zeroweather.util.AreaUtil;
import com.example.zero.zeroweather.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

//完全没进行出错判断处理，比如没有网络，待进行  而且得选择完城市后才进入天气界面
public class ChooseAreaActivity extends AppCompatActivity {

    private List<String> dataList = new ArrayList<>();;
    private ListView listView;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private ArrayAdapter<String> adapter;
    //状态，012表示当前处于省市县界面
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);

         /*       //如果还没省市县数据，则记得读取
        AreaUtil areaUtil = new AreaUtil();
        areaUtil.requestArea();*/

         //初始化状态为省
        status = 0;

        listView = findViewById(R.id.areaListView);

        adapter = new ArrayAdapter<>(ChooseAreaActivity.this,
                android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (status){
                    case 0:
                        Province province = provinceList.get(i);
                        //更改适配器中的内容为城市
                        readCity(province.getProvinceCode());
                        status++;
                        break;
                    case 1:
                        City city = cityList.get(i);
                        readCounty(city.getCityCode());
                        status++;
                        break;
                    case 2:
                        County county = countyList.get(i);
                        Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                        intent.putExtra("WeatherID",county.getWeatherId());
                        startActivity(intent);
//                        requestWeather(county.getWeatherId());
                        status = 0;
                        finish();
                        break;
                    default:
                        break;
                }

            }
        });
        readProvince();
    }


    /** 进入选择地区界面时要先显示各省 */
    private void readProvince() {
        provinceList = DataSupport.findAll(Province.class);
        dataList.clear();
        for(Province province:provinceList){
            dataList.add(province.getName());
        }
        adapter.notifyDataSetChanged();

    }

    /** 读取城市列表 ,此时点击返回会直接回到天气界面，待修改*/
    private void readCity(int provinceCode) {
        cityList = DataSupport.where("provinceCode = ?", "" + provinceCode)
                .find(City.class);
        //直接修改dataList，保证只有一个adapter,只有一个listView
        dataList.clear();
        for(City city:cityList){
            dataList.add(city.getName());
        }
        adapter.notifyDataSetChanged();
    }

    /** 读取县 */
    private void readCounty(int cityCode) {
        countyList = DataSupport.where("cityCode = ?","" + cityCode).find(County.class);
        dataList.clear();
        for (County county:countyList){
            dataList.add(county.getName());
        }
        adapter.notifyDataSetChanged();
    }

}
