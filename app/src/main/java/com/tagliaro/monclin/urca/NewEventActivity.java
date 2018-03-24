package com.tagliaro.monclin.urca;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class NewEventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), AlarmSetter.class);
        intent.setAction("com.tagliaro.monclin.urca.SET_NOTIFY");
        sendBroadcast(intent);
    }
}
