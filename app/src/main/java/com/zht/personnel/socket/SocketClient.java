package com.zht.personnel.socket;

import android.content.SharedPreferences;
import com.zht.personnel.ContextApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketClient implements Runnable {

    private Socket socket;
    private boolean isConnect = false;

    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;

    SharedPreferences preferences;


    public SocketClient() {
        preferences = ContextApplication.getAppContext().getSharedPreferences("setting", 0);
    }

    private void connect() {
        try {
            InetAddress addr = InetAddress.getByName(preferences.getString("ip", "192.168.1.1"));
            socket = new Socket();
            socket.connect(new InetSocketAddress(addr, 9999), 5000);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            //连接socket 连接成功后发送userName
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            String userName = "zht";
            out.println(userName);
            out.flush();
            isConnect = true;
            MyLog.v("socket", userName + "连接成功");
            connectionSuccess();
        } catch (Exception e) {
            MyLog.v("socket error", e.getMessage());
        }
    }


    @Override
    public void run() {

        while (true) {
            //判断是否连接成功
            if (isConnect) {
                try {
                    String msg = bufferedReader.readLine();
                    onProgress(msg);
                } catch (Exception e) {
                    disconnection();
                    MyLog.v("socket", "设备socket断开" + e.getMessage());
                    isConnect = false;
                }
            } else {
                MyLog.v("socket", "设备socket重连中");
                connect();
            }
        }
    }

    /**
     * @param msg
     */
    protected void onProgress(String msg) {

    }

    /**
     * 设备断线触发
     */
    protected void disconnection() {

    }

    /**
     * 设备断线触发
     */
    protected void connectionSuccess() {

    }
}
