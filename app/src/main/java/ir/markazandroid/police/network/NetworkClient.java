package ir.markazandroid.police.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.util.PreferencesManager;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Coded by Ali on 11/12/2017.
 */

public class NetworkClient {
    private OkHttpClient client;
    private List<Cookie> cookie;
    private PreferencesManager preferencesManager;

    public NetworkClient(PreferencesManager preferencesManager) {
        this.preferencesManager=preferencesManager;
        cookie = new ArrayList<>();
        populateCookies();
        client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                // for(int i=0;i<cookies.size();i++) Log.e("SAVED ",cookies.get(i).name()+"="+cookies.get(i).value());
                for (int i = 0; i < cookies.size(); i++) {
                    Cookie cook = cookies.get(i);
                    Log.e(cook.name(), cook.value());

                    preferencesManager.getPrivateSharedPreferences()
                            .edit().putString("COOKIE_" + cook.name(), cook.value()).apply();
                    for (int j = 0; j < cookie.size(); j++) {
                        Cookie cook2 = cookie.get(j);
                        if (cook2.name().compareTo(cook.name()) == 0) cookie.remove(cook2);
                    }
                }
                cookie.addAll(cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                //for(int i=0;i<cookie.size();i++) System.err.println("SENT "+cookie.get(i).name()+"="+cookie.get(i).value());
                return cookie;
            }
        })/*.addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        for (String h:chain.request().headers().names()){
                            Log.e("Head","name:"+h+"  value="+chain.request().header(h));
                        }
                        return chain.proceed(chain.request());
                    }
                }).addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        for (String h:chain.request().headers().names()){
                            Log.e("Head","name:"+h+"  value="+chain.request().header(h));
                        }
                        return chain.proceed(chain.request());
                    }
                })*/.retryOnConnectionFailure(true).build();
    }
    private void populateCookies() {
        Map map = preferencesManager.getPrivateSharedPreferences().getAll();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (((String) entry.getKey()).startsWith("COOKIE_")) {
                Cookie cookie = new Cookie.Builder().name(((String) entry.getKey()).substring(7))
                        .value((String) entry.getValue())
                        .domain(NetStatics.DOMAIN.substring(7).replace(":8080", ""))
                        .build();
                this.cookie.add(cookie);
            }
        }
    }
    public void deleteCookies(){
        SharedPreferences sharedPreferences = preferencesManager.getPrivateSharedPreferences();
        Map map = sharedPreferences.getAll();
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (((String) entry.getKey()).startsWith("COOKIE_")) {
                sharedPreferences.edit().remove(entry.getKey().toString()).apply();
            }
        }
        cookie.clear();
    }

    public OkHttpClient getClient() {
        return client;
    }


    public List<Cookie> getCookie() {
        return cookie;
    }
}
