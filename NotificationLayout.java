package com.example.soo.hw4_2015726017;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class NotificationLayout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_layout);
    }

    @Override
    protected void onDestroy() {
        Log.i("noti","onDestroy");
        super.onDestroy();
    }
}
