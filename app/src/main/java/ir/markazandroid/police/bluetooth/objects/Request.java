package ir.markazandroid.police.bluetooth.objects;

import java.io.Serializable;

/**
 * Coded by Ali on 6/11/2019.
 */
public class Request extends TransferObject implements Serializable {
    private String body;
    public static final String TYPE_REQUEST = "request";

    public Request() {
        setType(TYPE_REQUEST);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
