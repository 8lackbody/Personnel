package com.zht.personnel.socket;

import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zht.personnel.ContextApplication;
import com.zht.personnel.adapter.EPCTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketClient implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private static Socket socket;
    SharedPreferences preferences;
    String warehouseId;
    Timer timer;
    TimerTask heartBeat = new TimerTask() {
        @Override
        public void run() {
            try {
                socket.emit(Socket.EVENT_MESSAGE
                        , preferences.getString("warehouseId", "1")
                        , (Ack) objects ->{
                            JSONObject jsonObject = JSON.parseObject(Arrays.toString(objects));
                            boolean readerStatus = jsonObject.getBoolean("readerStatus");
                            List<EPCTag> getData = JSON.parseArray(jsonObject.getString("tags"), EPCTag.class);
                            if (readerStatus) {
                                if (getData.size() > 0) {
                                    onProgress(getData);
                                }
                                readerHeartBeatStart();
                            } else {
                                readerHeartBeatStop();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

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
        options.timeout = 10000;              // 连接超时时间(ms)
        options.forceNew = true;
        options.query = "warehouseId=" + warehouseId;
        try {
            socket = IO.socket("http://" + preferences.getString("ip", "192.168.1.2") + ":9999/", options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // 客户端一旦连接成功，开始发起登录请求
                logger.info(warehouseId + "连接成功");
                startGetDataForServer();
            }
        }).on("login", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("接受到服务器房间广播的登录消息：" + Arrays.toString(args));
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("Socket.EVENT_CONNECT_ERROR");
                disconnection();
                socket.connect();
            }
        }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("Socket.EVENT_CONNECT_TIMEOUT");
                stopGetDataForServer();
                socket.connect();
            }
        }).on(Socket.EVENT_PING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("Socket.EVENT_PING");
            }
        }).on(Socket.EVENT_PONG, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("Socket.EVENT_PONG");
            }
        }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                logger.info("-----------接受到消息啦--------" + Arrays.toString(args));
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                logger.info("客户端断开连接啦。。。");
                disconnection();
                stopGetDataForServer();
                socket.connect();
            }
        }).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                logger.info("重连中。。。");
            }
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
    public void startGetDataForServer() {
        if(timer == null){
            timer = new Timer();
        }
        timer.schedule(heartBeat, 5000, 1000);
    }

    /**
     *
     */
    public void stopGetDataForServer(){
        if(timer != null){
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

}