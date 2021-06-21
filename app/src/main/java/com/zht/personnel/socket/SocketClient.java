package com.zht.personnel.socket;

import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.zht.personnel.ContextApplication;
import com.zht.personnel.adapter.EPCTag;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketClient implements Runnable {

    private static Socket socket;
    SharedPreferences preferences;
    String warehouseId;
    Timer timer;

    public SocketClient() {
        preferences = ContextApplication.getAppContext().getSharedPreferences("setting", 0);
    }

    @Override
    public void run() {
        warehouseId = preferences.getString("warehouseId", "1");

        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};
        options.reconnectionAttempts = 5;     // 重连尝试次数
        options.reconnectionDelay = 2000;     // 失败重连的时间间隔(ms)
        options.timeout = 20000;              // 连接超时时间(ms)
        options.forceNew = true;
        options.query = "warehouseId=" + warehouseId;
        try {
            socket = IO.socket("http://" + preferences.getString("ip", "172.29.12.118") + ":9999/", options);
        } catch (URISyntaxException e) {
            disconnection();
            Log.e("e",e.getMessage());
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            // 客户端一旦连接成功，开始发起登录请求
            Log.v("v", warehouseId + "连接成功");
            startGetDataFromServer();

        }).on("login", args -> {
            Log.v("v", "接受到服务器房间广播的登录消息：" + Arrays.toString(args));

        }).on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.v("v", "Socket.EVENT_CONNECT_ERROR");
            disconnection();
            stopGetDataForServer();
            socket.connect();

        }).on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
            Log.v("v", "Socket.EVENT_CONNECT_TIMEOUT");
            Log.v("v", Arrays.toString(args));
            stopGetDataForServer();
            disconnection();
            socket.connect();

        }).on(Socket.EVENT_PING, args -> {
            Log.v("v", "Socket.EVENT_PING");

        }).on(Socket.EVENT_PONG, args -> {
            Log.v("v", "Socket.EVENT_PONG");

        }).on(Socket.EVENT_MESSAGE, args -> {
            Log.v("v", "-----------接受到消息啦--------" + Arrays.toString(args));

        }).on(Socket.EVENT_DISCONNECT, args -> {
            Log.v("v", "客户端断开连接啦。。。");
            disconnection();
            stopGetDataForServer();
            socket.connect();

        }).on(Socket.EVENT_RECONNECT, objects -> {
            Log.v("v", "重连中。。。");

        });

        socket.connect();
    }

    /**
     * @param getData 获得的数据
     */
    protected void onProgress(List<EPCTag> getData) {

    }

    /**
     * 阅读器心跳停止
     */
    protected void readerHeartBeatStop() {

    }

    /**
     * 阅读器心跳开始
     */
    protected void readerHeartBeatStart() {

    }

    /**
     * 设备断线触发
     */
    protected void disconnection() {

    }

    /**
     *
     */
    public void startGetDataFromServer() {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    socket.emit(Socket.EVENT_MESSAGE
                            , warehouseId
                            , (Ack) objects -> {
                                Object[] clone = objects.clone();
                                JSONObject jsonObject = (JSONObject) clone[0];
                                try {
                                    boolean readerStatus = jsonObject.getBoolean("readerStatus");
                                    if (readerStatus) {
                                        List<EPCTag> getData = JSON.parseArray(jsonObject.getString("tags"), EPCTag.class);
                                        if (getData.size() > 0) {
                                            onProgress(getData);
                                        }
                                        readerHeartBeatStart();
                                    } else {
                                        readerHeartBeatStop();
                                        Log.e("e","reader stop");
                                    }
                                } catch (JSONException e) {
                                    Log.e("e",e.getMessage());
                                }
                            });
                } catch (Exception e) {
                    Log.e("e",e.getMessage());
                }
            }
        }, 2000, 1000);
    }

    /**
     *
     */
    public void stopGetDataForServer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

}