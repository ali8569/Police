package ir.markazandroid.police.event;

import android.content.Intent;

/**
 * Coded by Ali on 4/27/2019.
 */
public final class BaseEvent {
    public static final String ACTION_EVENT="ir.markazandroid.police.event.BaseEvent.ACTION_EVENT";
    public static final String EVENT_TYPE_NAME="ir.markazandroid.police.event.BaseEvent.EVENT_TYPE_NAME";

    public static final String EVENT_TYPE_DEVICE_AUTHENTICATED="EVENT_TYPE_DEVICE_AUTHENTICATED";
    public static final String EVENT_TYPE_3PARTY_APPLICATION_ADDED = "EVENT_TYPE_3PARTY_APPLICATION_ADDED";
    public static final String EVENT_TYPE_3PARTY_APPLICATION_REMOVED = "EVENT_TYPE_3PARTY_APPLICATION_REMOVED";

    public static final String PARAMETER_APP_ID = "PARAMETER_APP_ID";


    public static Intent getDeviceAuthenticatedIntent(){
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(EVENT_TYPE_NAME,EVENT_TYPE_DEVICE_AUTHENTICATED);
        return intent;
    }

    public static Intent get3PartyApplicationAddedIntent(String appId) {
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(EVENT_TYPE_NAME, EVENT_TYPE_3PARTY_APPLICATION_ADDED);
        intent.putExtra(PARAMETER_APP_ID, appId);
        return intent;
    }

    public static Intent get3PartyApplicationRemovedIntent(String appId) {
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(EVENT_TYPE_NAME, EVENT_TYPE_3PARTY_APPLICATION_REMOVED);
        intent.putExtra(PARAMETER_APP_ID, appId);
        return intent;
    }
}
