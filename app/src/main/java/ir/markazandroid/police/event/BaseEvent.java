package ir.markazandroid.police.event;

import android.content.Intent;

/**
 * Coded by Ali on 4/27/2019.
 */
public final class BaseEvent {
    public static final String ACTION_EVENT="ir.markazandroid.police.event.BaseEvent.ACTION_EVENT";
    public static final String EVENT_TYPE_NAME="ir.markazandroid.police.event.BaseEvent.EVENT_TYPE_NAME";

    public static final String EVENT_TYPE_DEVICE_AUTHENTICATED="EVENT_TYPE_DEVICE_AUTHENTICATED";
    public static final String EVENT_TYPE_MIRROR_BLOCK = "EVENT_TYPE_MIRROR_BLOCK";
    public static final String EVENT_TYPE_MIRROR_UNBLOCK = "EVENT_TYPE_MIRROR_UNBLOCK";

    public static Intent getDeviceAuthenticatedIntent(){
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(EVENT_TYPE_NAME,EVENT_TYPE_DEVICE_AUTHENTICATED);
        return intent;
    }

    public static Intent getMirrorBlockIntent() {
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(EVENT_TYPE_NAME, EVENT_TYPE_MIRROR_BLOCK);
        return intent;
    }

    public static Intent getMirrorUnBlockIntent() {
        Intent intent = new Intent(ACTION_EVENT);
        intent.putExtra(EVENT_TYPE_NAME, EVENT_TYPE_MIRROR_UNBLOCK);
        return intent;
    }
}
