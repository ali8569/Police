package ir.markazandroid.police.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.UUID;

import ir.markazandroid.police.PoliceApplication;
import ir.markazandroid.police.bluetooth.objects.Request;
import ir.markazandroid.police.bluetooth.objects.Response;
import ir.markazandroid.police.bluetooth.objects.TransferObject;
import ir.markazandroid.police.util.Console;

/**
 * Coded by Ali on 6/11/2019.
 */
public class BluetoothServer extends Thread {

    public static final String B_UUID = "47eb9faf-bb80-4ddd-8875-22f3de331a70";
    public static final String ADDRESS = "CC:4B:73:26:C9:73";

    private BluetoothServerSocket serverSocket;
    private BluetoothSocket bluetoothSocket;
    private BluetoothAdapter bluetoothAdapter;
    private ObjectOutputStream oOutputStream;
    private Console console;

    private boolean isStopped;

    public BluetoothServer(PoliceApplication policeApplication) {
        console = policeApplication.getConsole();
        Log.e("Bluetooth", "Created");
    }

    private void init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.setName("UniTech");
        bluetoothAdapter.enable();
        Log.e("Bluetooth", "Init");
    }

    private BluetoothSocket listenForNewClient() throws IOException {
        closeSockets();
        serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("ir.markazandroid.unitechcontroller", UUID.fromString(B_UUID));
        try {
            return serverSocket.accept();
        } finally {
            Log.e("Bluetooth", "Connected");
            serverSocket.close();
        }
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {

                init();
                bluetoothSocket = listenForNewClient();
                oOutputStream = getOOutput(bluetoothSocket);
                ObjectInputStream inputStream = getOInput(bluetoothSocket);
                while (!isStopped) {
                    try {
                        onObjectReceived((TransferObject) inputStream.readObject());
                    } catch (ClassNotFoundException | OptionalDataException e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                closeSockets();
            }
        }
    }

    private ObjectOutputStream getOOutput(BluetoothSocket bluetoothSocket) throws IOException {
        return new ObjectOutputStream(bluetoothSocket.getOutputStream());
    }

    private ObjectInputStream getOInput(BluetoothSocket bluetoothSocket) throws IOException {
        return new ObjectInputStream(bluetoothSocket.getInputStream());
    }

    private void onObjectReceived(TransferObject object) {
        switch (object.getType()) {
            case Request.TYPE_REQUEST:
                handleRequest((Request) object);
                break;
        }
    }

    private void handleRequest(Request request) {
        Log.e("bluetooth Request", request.getBody());
        console.executeAsync(request.getBody(), (resultCode, output) -> {
            Response response = new Response();
            response.setStatus(resultCode);
            response.setBody(output);
            writeToClient(response);
        });

    }

    private synchronized boolean writeToClient(TransferObject object) {
        if (oOutputStream != null && bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                oOutputStream.writeObject(object);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    public void close() {
        isStopped = true;
        closeSockets();
    }

    private void closeSockets() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (bluetoothSocket != null)
                bluetoothSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
