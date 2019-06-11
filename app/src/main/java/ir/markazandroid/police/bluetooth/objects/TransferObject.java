package ir.markazandroid.police.bluetooth.objects;

import java.io.Serializable;

/**
 * Coded by Ali on 6/11/2019.
 */
public abstract class TransferObject implements Serializable {
    private String type;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
