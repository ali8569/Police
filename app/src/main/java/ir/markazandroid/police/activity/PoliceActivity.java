package ir.markazandroid.police.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import ir.markazandroid.police.hardware.PortReader;

public class PoliceActivity extends AppCompatActivity {

    private Button start,stop;
    private Intent starterIntent;
    PortReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PortReader reader = new PortReader(this);
        //reader.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //reader.close();
    }
}
