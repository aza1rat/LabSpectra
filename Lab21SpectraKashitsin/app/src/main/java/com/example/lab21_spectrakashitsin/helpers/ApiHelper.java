package com.example.lab21_spectrakashitsin.helpers;

import android.app.Activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiHelper {
    Activity ctx;
    public static String address = "http://labs-api.spbcoit.ru:80/lab/spectra/api";
    public ApiHelper(Activity ctx)
    {
        this.ctx = ctx;
    }
    public void onReady(String res)
    {

    }

    public void onFailed()
    {

    }
    String httpGet(String req,String body) throws IOException
    {
        URL url = new URL(req);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
        out.write(body.getBytes());
        out.flush();
        BufferedInputStream inp = new BufferedInputStream(connection.getInputStream());
        byte[] buf = new byte[512];
        String res = "";
        while (true)
        {
            int num = inp.read(buf);
            if (num < 0) break;
            res += new String(buf,0,num);
        }
        connection.disconnect();
        return res;
    }

    public class NetOp implements Runnable
    {
        public String req;
        public String body;

        @Override
        public void run() {
            try{
                final String res = httpGet(address+"/"+req,body);
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onReady(res);
                    }
                });
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                onFailed();
            }
        }
    }

    public void send(String req, String body)
    {
        NetOp netOp = new NetOp();
        netOp.body = body;
        netOp.req = req;
        Thread thread = new Thread(netOp);
        thread.start();
    }
}
