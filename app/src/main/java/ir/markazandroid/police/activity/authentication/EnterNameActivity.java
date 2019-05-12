package ir.markazandroid.police.activity.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.Toast;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.R;
import ir.markazandroid.police.activity.BaseActivity;
import ir.markazandroid.police.network.OnResultLoaded;
import ir.markazandroid.police.object.ErrorObject;
import ir.markazandroid.police.object.Phone;
import ir.markazandroid.police.signal.Signal;

public class EnterNameActivity extends BaseActivity {

    private EditText username;
    private TextInputLayout usernameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);

        username=findViewById(R.id.username);
        usernameLayout=findViewById(R.id.username_layout);
        String name;
        try {
            Context myContext = createPackageContext("ir.markazandroid.uinitializr",
                    Context.CONTEXT_IGNORE_SECURITY);
            name= myContext.getSharedPreferences("ir.markazandroid.uinitializr.SETTINGS",MODE_WORLD_READABLE).
                    getString("ir.markazandroid.uinitializr.EnterNameActivity.name",null);

            ((PoliceApplication) getApplication()).getConsole().write("pm uninstall ir.markazandroid.uinitializr");
        } catch (Exception e) {
            e.printStackTrace();
            name="TBInit8569";
        }

        username.setText(name);


        submit();
    }

    private boolean valid() {
        boolean valid =true;
        String name = username.getText().toString();
        usernameLayout.setErrorEnabled(false);

        if (name.isEmpty()){
            usernameLayout.setError("نام کاربری نمی تواند خالی باشد");
            valid=false;
        }
        return valid;
    }


    private void submit() {

        getNetworkManager().sendName(username.getText().toString(), new OnResultLoaded.ActionListener<Phone>() {
            @Override
            public void onSuccess(final Phone successResult) {
                runOnUiThread(() -> proceedToMainActivity(successResult));
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(() -> {
                    handleServerError(error);
                    username.setText("TBInit8569");
                    submit();
                });


            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(EnterNameActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                    submit();
                });
            }
        });
    }

    private void proceedToMainActivity(Phone phone) {
        Intent intent = new Intent(this,ShowAuthenticationDetailsActivity.class);
        intent.putExtra(ShowAuthenticationDetailsActivity.PHONE,phone);
        startActivity(intent);
        Signal signal = new Signal("Login",Signal.SIGNAL_LOGIN,phone);
        getSignalManager().sendMainSignal(signal);
        finish();
    }

    private void handleServerError(ErrorObject error) {
        usernameLayout.setError(error.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
