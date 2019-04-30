package ir.markazandroid.police.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.R;
import ir.markazandroid.police.activity.BaseActivity;
import ir.markazandroid.police.object.Phone;

public class ShowAuthenticationDetailsActivity extends BaseActivity {

    public static final String PHONE="ShowAuthenticationDetailsActivity.PHONE" ;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_authentication_details);

        Phone phone = (Phone) getIntent().getSerializableExtra(PHONE);

        textView= findViewById(R.id.username_layout);

        textView.setText("نام دستگاه: "+phone.getName()+"\r\n"+"رمز: "+phone.getPassword());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> ((PoliceApplication)getApplication()).getConsole().write("reboot"));
            }
        },10_000);

    }
}
