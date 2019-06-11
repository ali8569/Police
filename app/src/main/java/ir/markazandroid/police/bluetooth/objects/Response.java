package ir.markazandroid.police.bluetooth.objects;

import java.io.Serializable;

/**
 * Coded by Ali on 6/11/2019.
 */
public class Response extends TransferObject implements Serializable {
    public static final int STATUS_OK = 1;
    public static final int STATUS_FAIL = -1;
    public static final String TYPE_RESPONSE = "response";


    public Response() {
        setType(TYPE_RESPONSE);
    }

    private int status;
    private String body;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
