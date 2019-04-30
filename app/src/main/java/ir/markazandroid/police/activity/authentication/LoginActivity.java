package ir.markazandroid.police.activity.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ir.markazandroid.police.R;
import ir.markazandroid.police.activity.BaseActivity;
import ir.markazandroid.police.network.OnResultLoaded;
import ir.markazandroid.police.object.ErrorObject;
import ir.markazandroid.police.object.LoginCredentials;
import ir.markazandroid.police.object.Phone;
import ir.markazandroid.police.signal.Signal;

/**
 * Coded by Ali on 06/02/2018.
 */

public class LoginActivity extends BaseActivity {

    private EditText username,password;
    private TextInputLayout usernameLayout,passwordLayout;
    private Button login;
    private String uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username=findViewById(R.id.username);
        password = findViewById(R.id.password);
        usernameLayout=findViewById(R.id.username_layout);
        passwordLayout=findViewById(R.id.password_layout);
        login=findViewById(R.id.login);

        username.setText("admin");
        password.setText("12345");


        uuid = getSharedPreferences("pref",MODE_PRIVATE).getString("uuid",null);
        if (uuid==null) {
            login();
        }
        else {
           // buttonHandler.click();
            doLogin();
        }

    }



    private void login() {
        LoginCredentials user = new LoginCredentials();
        user.setUsername(username.getText().toString());
        user.setPassword(password.getText().toString());
        String id =((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        user.setDeviceId(id);
        //user.setToken(FirebaseInstanceId.getInstance().getToken());

        getNetworkManager().register(user, new OnResultLoaded.ActionListener<Phone>() {
            @Override
            public void onSuccess(final Phone successResult) {
                runOnUiThread(() -> {
                    uuid=successResult.getUuid();
                    getSharedPreferences("pref",MODE_PRIVATE).edit().putString("uuid",successResult.getUuid()).apply();
                    doLogin();
                });
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(() -> handleServerError(error));
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->{
                    Toast.makeText(LoginActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                    login();
                });
            }
        });
    }

    private void doLogin() {
       // buttonHandler.click();
        getNetworkManager().login(uuid, new OnResultLoaded.ActionListener<Phone>() {
            @Override
            public void onSuccess(final Phone successResult) {
                runOnUiThread(() -> {
                    if (successResult.getStatus()==Phone.STATUS_NO_LOGIN)
                        proceedToEnterNameActivity();
                    else
                        proceedToMainActivity(successResult);
                });
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(() -> handleServerError(error));
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                        doLogin();
                    }
                });
            }
        });
    }

    private void proceedToEnterNameActivity() {
        Intent intent = new Intent(this,EnterNameActivity.class);
        startActivity(intent);
        finish();
    }
    private void proceedToMainActivity(Phone phone) {
        Signal signal = new Signal("Login",Signal.SIGNAL_LOGIN,phone);
        getSignalManager().sendMainSignal(signal);
        finish();
       // Intent intent = new Intent(this,MainActivity.class);
        //startActivity(intent);
        //finish();
    }

    private void handleServerError(ErrorObject error) {
        usernameLayout.setError(error.getMessage());
    }

    private boolean valid() {
        boolean valid =true;
        String name = username.getText().toString();
        String pass = password.getText().toString();
        passwordLayout.setErrorEnabled(false);
        usernameLayout.setErrorEnabled(false);

        if (name.isEmpty()){
            usernameLayout.setError("نام کاربری نمی تواند خالی باشد");
            valid=false;
        }

        if (pass.isEmpty()){
            passwordLayout.setError("رمز عبور نمی تواند خالی باشد");
            valid=false;
        }
        return valid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //buttonHandler.dispose();
    }


}
