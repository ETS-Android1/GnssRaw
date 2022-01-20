package com.github.mattiadellepiane.gnssraw.listeners;


import android.util.Log;

import com.github.mattiadellepiane.gnssraw.R;
import com.github.mattiadellepiane.gnssraw.data.SharedData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerCommunication extends MeasurementListener {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ServerCommunication(SharedData data){
        super(data);
        data.setServerCommunication(this);
    }

    private String getDebugTag(){
        return data.getContext().getString(R.string.debug_tag);
    }

    @Override
    protected void initResources() {
        if(data.isServerEnabled()) {
            new Thread(() -> {
                try {
                    Log.v(getDebugTag(), "Server ip: " + data.getServerAddress());
                    socket = new Socket(data.getServerAddress(), data.getServerPort());
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    protected void releaseResources() {
        new Thread(()->{
            if(out != null)
                out.close();
            try {
                if(in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void write(String s){
        if(data.isListeningForMeasurements() && data.isServerEnabled()) {
            Log.v(getDebugTag(), "Invio messaggio al server");
            if(out != null) {
                new Thread(() -> {
                    out.println(s);
                }).start();
            }
            Log.v(getDebugTag(), "Messaggio inviato al server");
        }
    }

    public boolean isReachable() {
        Socket s = null;
        boolean reachable = false;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(data.getServerAddress(), data.getServerPort()),3000);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println("PING");
            String response = in.readLine();
            if(response != null && response.equalsIgnoreCase("OK")){
                reachable = true;
            }
            out.close();
        } catch (IOException e) {
            return false;
        }
        return reachable;
    }
}
