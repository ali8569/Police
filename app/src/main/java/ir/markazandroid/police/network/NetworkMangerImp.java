package ir.markazandroid.police.network;

import android.content.Context;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ir.markazandroid.police.BuildConfig;
import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.hardware.PortReader;
import ir.markazandroid.police.hardware.SensorMeter;
import ir.markazandroid.police.network.JSONParser.Parser;
import ir.markazandroid.police.network.formdata.FormDataParser;
import ir.markazandroid.police.object.ErrorObject;
import ir.markazandroid.police.object.LoginCredentials;
import ir.markazandroid.police.object.Phone;
import ir.markazandroid.police.object.Stats;
import ir.markazandroid.police.object.Status;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Coded by Ali on 03/11/2017.
 */

public class NetworkMangerImp implements NetworkManager {


    private Context context;
    private Parser parser;
    private OkHttpClient client;
    private String tag;

    private NetworkMangerImp(Context context,String tag) {
        this.tag=tag;
        this.context=context;
        parser=((PoliceApplication)context.getApplicationContext()).getParser();
        client=((PoliceApplication)context.getApplicationContext()).getNetworkClient().getClient();
    }

    @Override
    public void register(LoginCredentials user, final OnResultLoaded.ActionListener<Phone> actionListener) {
        Request request = new Request.Builder()
                .url(NetStatics.REGISTRATION_REGISTER)
                .post(FormDataParser.objectToFormBody(user))
                .build();

        client.newCall(request).enqueue(new CBack(context,tag) {
            @Override
            public boolean isSuccessfull(int code) {
                return true;
            }

            @Override
            public boolean isSuccessfull(int code, JSONObject response) {
                if (code==200) actionListener.onSuccess(parser.get(Phone.class,response));
                else actionListener.onError(parser.get(ErrorObject.class,response));
                return false;
            }

            @Override
            public void fail(IOException e) {
                actionListener.failed(e);
            }
        });
    }

    @Override
    public void login(String uuid, final OnResultLoaded.ActionListener<Phone> actionListener) {
        Request request = new Request.Builder()
                .url(NetStatics.REGISTRATION_LOGIN)
                .post(new FormBody.Builder().add("uuid",uuid).build())
                .build();

        client.newCall(request).enqueue(new CBack(context,tag) {
            @Override
            public boolean isSuccessfull(int code) {
                return true;
            }

            @Override
            public boolean isSuccessfull(int code, JSONObject response) {
                if (code==200) actionListener.onSuccess(parser.get(Phone.class,response));
                else actionListener.onError(parser.get(ErrorObject.class,response));
                return false;
            }

            @Override
            public void fail(IOException e) {
                actionListener.failed(e);
            }
        });
    }

    @Override
    public void sendName(String name, final OnResultLoaded.ActionListener<Phone> actionListener) {
        Request request = new Request.Builder()
                .url(NetStatics.PHONE_FIRSTLOGIN)
                .post(new FormBody.Builder().add("name",name).build())
                .build();

        client.newCall(request).enqueue(new CBack(context,tag) {
            @Override
            public boolean isSuccessfull(int code) {
                return true;
            }

            @Override
            public boolean isSuccessfull(int code, JSONObject response) {
                if (code==200) actionListener.onSuccess(parser.get(Phone.class,response));
                else actionListener.onError(parser.get(ErrorObject.class,response));
                return false;
            }

            @Override
            public void fail(IOException e) {
                actionListener.failed(e);
            }
        });
    }

    @Override
    public void getStatus(long lastTime,OnResultLoaded<Status> result) {
        Stats statsObj = new Stats();
        statsObj.setLastTime(lastTime);
        statsObj.setArduinoStats(PortReader.lastData);
        statsObj.setVersionCode(BuildConfig.VERSION_CODE);
        statsObj.setVersionName(BuildConfig.VERSION_NAME);
        statsObj.setTimestamp(System.currentTimeMillis());
        Location location = getLocation();
        if (location!=null){
            statsObj.setLat(location.getLatitude());
            statsObj.setLon(location.getLongitude());
        }

        PortReader.save(statsObj.toString());


        Request request = new Request.Builder()
                .url(NetStatics.STATUS)
                .post(FormDataParser.objectToFormBody(statsObj))
                .build();

        client.newCall(request).enqueue(new CBack(context,tag) {
            @Override
            public void result(JSONObject response) throws JSONException {
                result.loaded(parser.get(Status.class,response));
            }

            @Override
            public void fail(IOException e) {
                result.failed(e);
            }
        });
    }


    private SensorMeter getSensorMeter(){
        return ((PoliceApplication)context.getApplicationContext()).getSensorMeter();
    }



    public static class NetworkManagerBuilder {
        private String tag;
        private Context context;

        public NetworkManagerBuilder() {
        }

        public NetworkManagerBuilder from(Context context) {
            this.context = context;
            return this;
        }

        public NetworkManagerBuilder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public NetworkManager build() {
            return new NetworkMangerImp(context, tag);
        }
    }

    private Location getLocation(){
        return ((PoliceApplication)context.getApplicationContext()).getLocationMgr().getLatestLocation();
    }
}
