package ir.markazandroid.police.network;

import ir.markazandroid.police.object.LoginCredentials;
import ir.markazandroid.police.object.Phone;
import ir.markazandroid.police.object.Status;


/**
 * Coded by Ali on 03/11/2017.
 */

public interface NetworkManager {


    void register(LoginCredentials credentials, OnResultLoaded.ActionListener<Phone> actionListener);

    void login(String uuid, OnResultLoaded.ActionListener<Phone> actionListener);

    void sendName(String name, OnResultLoaded.ActionListener<Phone> actionListener);

    void getStatus(long lastTime,OnResultLoaded<Status> result);


}
