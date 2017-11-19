package com.qx.wz.com.rtcmdemo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.qx.wz.sdk.rtcm.RtcmSnippet;
import com.qx.wz.sdk.rtcm.WzRtcmFactory;
import com.qx.wz.sdk.rtcm.WzRtcmListener;
import com.qx.wz.sdk.rtcm.WzRtcmManager;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    LocationManager mLocationManager;
    WzRtcmManager mRtcmManager;
    boolean mInitRtcm;
    TextView mShowTv;
    final int GETLOCATION = 1;
    final int GETRTCM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mShowTv = (TextView) findViewById(R.id.rtcm);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //填入从www.qxwz.com申请的appKey和appSecret
        String appKey = "";
        String appSecret = "";
        //得到rtcm对象
        mRtcmManager = WzRtcmFactory.getWzRtcmManager(this, appKey, appSecret, null);
        //开启定位，每秒吐出定位结果
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //停止定位
        mLocationManager.removeUpdates(mLocationListener);
        //关闭rtcm
        mRtcmManager.close();
        mRtcmManager = null;
        mInitRtcm = false;
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mInitRtcm) {
                //rtcm已经初始化过，直接发送位置给rtcm SDK，并通过WzRtcmListener得到差分流
                try {
                    mRtcmManager.sendGGA(location.getLatitude(), location.getLongitude());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mHandler.sendEmptyMessage(GETLOCATION);
                //rtcm初始化
                try {
                    mRtcmManager.requestRtcmUpdate(mRtcmListener, location.getLatitude(), location.getLongitude(), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mInitRtcm = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    WzRtcmListener mRtcmListener = new WzRtcmListener() {
        @Override
        public void onRtcmDatachanged(RtcmSnippet rtcmSnippet) {
            // 得到差分数据流
            mHandler.sendEmptyMessage(GETRTCM);
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message = "";
            switch (msg.what) {
                case GETLOCATION:
                    message = "得到GPS定位";
                    break;
                case GETRTCM:
                    message = "得到RTCM结果" + (new Date().toString());
                    break;
            }
            mShowTv.setText(message);
        }
    };
}
