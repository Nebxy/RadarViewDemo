package com.example.neb.radarviewdemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.overlay.PoiOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.neb.radarviewdemo.view.FloatView;
import com.example.neb.radarviewdemo.view.RadarView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener, SensorEventListener {
    EditText editQuery;
    @BindView(R.id.bt_search)
    Button btSearch;
    private FrameLayout poiContent;
    private ArrayList<FloatView> tvList = new ArrayList<>(); //存放悬浮View的集合
    private RelativeLayout activity_main;
    private MapView mapView;
    private AMap aMap;
    private SensorManager sensorManager;
    private Sensor magneticSensor;//传感器
    //private LatLng centerpoint = new LatLng(30.203588, 120.216596);// 温馨人家
    private LatLng centerpoint = new LatLng(30.230867, 120.189743);// 单位
    private LinearLayout.LayoutParams mParams;
    private Marker marker;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private LatLonPoint lp;
    private ArrayList<PoiItem> pois;
    //private LatLonPoint latLonPoint = new LatLonPoint(30.287459, 120.153576);
    private LatLonPoint latLonPoint = new LatLonPoint(30.230867, 120.189743);
    private LatLonPoint latLonPoint1;
    private RadarView radarView;
    private float x;
    private float y;
    private float fixedY;
    private float tempx;
    private TextView tempText;
    private ArrayList<Integer> angle = new ArrayList<>();
    private float z;
    private float fixedZ;
    private float fixedX;
    private int width;
    private int height;
    //private LatLonPoint latLonPoint = new LatLonPoint(31.238068, 121.501654);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //得到屏幕宽高
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        System.out.println(width + ":" + height);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magneticSensor = sensorManager
                .getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //注册传感器
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
        AMapOptions aMapOptions = new AMapOptions();
        aMapOptions.camera(new CameraPosition(centerpoint, 10f, 0, 0));
        //mapView = (MapView) findViewById(R.id.map);
        mapView = new MapView(this, aMapOptions);
        mapView.onCreate(savedInstanceState);
        init();
        showMarker(centerpoint);
        doSearchQuery("超市");
    }

    /**
     * 标注当前位置
     *
     * @param centerpoint
     */
    private void showMarker(LatLng centerpoint) {
        marker = aMap.addMarker(new MarkerOptions()
                .position(centerpoint)
                .title("温馨人家")
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .draggable(true));
        marker.showInfoWindow();// 设置默认显示一个infowinfow
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        activity_main = (RelativeLayout) findViewById(R.id.activity_main);
        poiContent = (FrameLayout) findViewById(R.id.poiContent);
        //将poicontent右移400像素以校准
        //poiContent.setTranslationX(8640);
        radarView = (RadarView) findViewById(R.id.radarView);
        editQuery = (EditText) findViewById(R.id.editQuery);
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
            mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            //activity_main.addView(mapView, mParams);
        }
    }


    /**
     * * 开始进行poi搜索
     */
    protected void doSearchQuery(String keyWord) {
        //aMap.clear();
        for (LinearLayout textView : tvList) {
            poiContent.removeView(textView);
        }
        tvList.clear();
        angle.clear();
        int currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "杭州市");
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 1000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();// 异步搜索
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        pois = poiResult.getPois();
        aMap.clear();// 清理之前的图标
        radarView.clearPOI();//清空poi
        PoiOverlay poiOverlay = new PoiOverlay(aMap, pois);
        poiOverlay.removeFromMap();
        poiOverlay.addToMap();
        poiOverlay.zoomToSpan();
        for (final PoiItem poi : pois) {
            latLonPoint1 = poi.getLatLonPoint();
            System.out.println("距离：   " + poi.getDirection() + poi.getDistance() + "==============");
            double x1 = (poi.getLatLonPoint().getLongitude());
            double x2 = (poi.getLatLonPoint().getLatitude());
            double y1 = (latLonPoint.getLongitude());
            double y2 = (latLonPoint.getLatitude());
            System.out.println("  poi点：" + x1 + "==" + x2 + "==\n 中心点：" + y1 + "==" + y2 + "");
            //System.out.println("店名 ==：" + poi.getTitle());
            RadarView.MyLatLng A = new RadarView.MyLatLng(x1, x2);
            RadarView.MyLatLng B = new RadarView.MyLatLng(y1, y2);
            System.out.println("角度：==" + RadarView.getAngle(A, B));
            angle.add((int) RadarView.getAngle(A, B));
            radarView.addPoint(poi, latLonPoint);
            //System.out.println("poi坐标：" + poi.getLatLonPoint().getLongitude() + ":" + poi.getLatLonPoint().getLatitude() + "====");
            System.out.println("==============================================================================");
            //初始化悬浮view位置 8640为屏幕像素的八倍
            //根据view
            int left = (int) ((((float) (360 - RadarView.getAngle(A, B)) / (float) 360)) * -width * 8);
            System.out.println("?????????" + left);
            /*FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT); //设置悬浮view的初始位置*/
            FloatView ll = new FloatView(this,left,0);
            TextView tv = new TextView(this);
            tv.setText("ssssss");


            //ll.setLayoutParams(llparams);
            //ll.addView(tv);
            //填充店名和距离
            ll.setName(poi.getTitle()).setDistance(poi.getDistance());
/*            TextView tempText = new TextView(this);
            tempText.setText(poi.getTitle() + "\n" + poi.getDistance() + "米");
            tempText.setLayoutParams(params);
            tempText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, poi.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });*/
            tvList.add(ll);
            poiContent.addView(ll);
        }

        RadarView.MyLatLng A = new RadarView.MyLatLng(latLonPoint.getLongitude(), latLonPoint.getLatitude());
        //radarView.addPoint(A, A, pois.get(0));
        showMarker(centerpoint);

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        System.out.println(poiItem.getAdName() + "=====");
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        radarView.unregisterListenter();
        mapView.onDestroy();
    }

    public void searchPOI(View view) {
        String keyWord = editQuery.getText().toString();
        if (TextUtils.isEmpty(keyWord)) {
            Toast.makeText(MainActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
        } else {
            doSearchQuery(keyWord);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        z = event.values[2];
        y = event.values[1];
        x = event.values[0] + z;
        //修正y
        fixedY = Math.abs(y / 90) * height - height / 2;
        //修正Z
        fixedZ = (180 - (z + 90)) + 270;

        //改变所有悬浮view的位置
        if (tvList.size() > 0) {
            for (int i = 0; i < tvList.size(); i++) {
                //做修正拼接处理,解决X指向360和0度的交界处引起的显示异常，
                if (x < angle.get(i)) {
                    tempx = x + 360;
                } else {
                    tempx = x;
                }
                //float fixedX = (tempx / (360 - 22.5f)) * 1080 - 540;
                //tvList.get(i).setTranslationX((1080 - fixedX * 8) + 4320);
                fixedX = (width - ((tempx / (360 - 22.5f)) * width - width / 2) * 8) + width * 8;
                float fixedXX = (((90f - (Math.abs(z))) / 90f)) * fixedX;
                tvList.get(i).setTranslationX(fixedX);
                tvList.get(i).setTranslationY(fixedY);
                //tvList.get(i).setRotation(fixedZ);
                //poiContent.setRotation(360 - fixedZ);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}