package com.zht.personnel;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zht.personnel.adapter.EPCTag;
import com.zht.personnel.adapter.HomeRecycleAdapter;
import com.zht.personnel.socket.MyLog;
import com.zht.personnel.socket.SocketClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;//声明RecyclerView
    private HomeRecycleAdapter homeAdapter;//声明适配器
    private Context context;
    private List<EPCTag> list;
    private Set<EPCTag> tableData;
    private TextView homeTitle;
    private TextView warehouseName;
    private SocketClient socketClient;
    private TextView number;
    private Button confirm;
    private Timer timer;
    private GifImageView gif;

    //判断是否需要开始清空表任务
    private boolean flag = false;

    private boolean mIsExit;

    TimerTask confirmTable = new TimerTask() {
        @Override
        public void run() {
            setConfirm();
        }
    };

    TimerTask cleanTable = new TimerTask() {
        @Override
        public void run() {
            tableData.clear();
            list.clear();
            handler.sendEmptyMessage(1);
            stopCleanTable();
            stopConfirmTable();
        }
    };


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    refreshTable();
                    break;
                case 2:
                    gif.setBackgroundResource(R.drawable.running);
                    break;
                case 3:
                    gif.setBackgroundResource(R.drawable.stop);
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        init();

        socketClient = new SocketClient() {
            @Override
            protected void onProgress(List<EPCTag> getData) {
                try {
                    if (tableData.size() == 0) {
                        startConfirmTable();
                    }
                    tableData.addAll(getData);
                    if (tableData.size() > list.size()) {
                        stopConfirmTable();
                        startConfirmTable();
                        if (flag == true) {
                            stopCleanTable();
                        }
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    MyLog.v("main error", e.getMessage());
                }
            }

            @Override
            protected void disconnection() {
                handler.sendEmptyMessage(3);
            }

            @Override
            protected void connectionSuccess() {
                handler.sendEmptyMessage(2);
            }

            @Override
            protected void readerHeartBeatStop() {
                handler.sendEmptyMessage(3);
            }
        };
        new Thread(socketClient).start();
    }

    /**
     * 初始化view和属性
     */
    public void init() {
        recyclerView = findViewById(R.id.home_recycler_view);
        number = findViewById(R.id.bottom_count2);
        homeTitle = findViewById(R.id.home_title);
        warehouseName = findViewById(R.id.warehouse_name);
        confirm = findViewById(R.id.button);
        gif = findViewById(R.id.gif);
        list = new ArrayList<>();
        tableData = new HashSet<>();
        homeAdapter = new HomeRecycleAdapter(context, list);
        GridLayoutManager manager = new GridLayoutManager(context, 1);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
        timer = new Timer();

        confirm.setOnClickListener(view -> {
            stopConfirmTable();
            setConfirm();
        });
    }

    /**
     * 点击确认按钮出发的事件
     * 把当前列表里的标签变成确认
     */
    public void setConfirm() {
        if (tableData.size() != 0) {
            MyLog.v("run", new Date() + "确认按钮出发的事件");
            //是否有未确认的标签被确认
            boolean haveGray = false;
            for (EPCTag epcTag : tableData) {
                if ("未确认".equals(epcTag.getStatus())) {
                    haveGray = true;
                }
                epcTag.setStatus("已确认");
            }
            if (haveGray == true) {
                stopCleanTable();
                startCleanTable();
                handler.sendEmptyMessage(1);
            }
        }
    }

    /**
     * 根据list中的数组刷新表格和显示的数字
     */
    public void refreshTable() {
        list = new ArrayList<>(tableData);
        homeAdapter.setList(list);
        homeAdapter.notifyDataSetChanged();
        number.setText(list.size() + "");
    }

    /**
     * 开始清空表的任务
     */
    public void startCleanTable() {
        MyLog.v("run", new Date() + "开始清空表的任务");
        flag = true;
        timer.schedule(cleanTable, 60000);
    }

    /**
     * 停止清空表的任务
     */
    public void stopCleanTable() {
        if (cleanTable != null) {
            MyLog.v("run", new Date() + "停止清空表的任务");
            cleanTable.cancel();
            timer.purge();
            flag = false;
        }
    }

    /**
     * 开始确认表的任务
     */
    public void startConfirmTable() {
        MyLog.v("run", new Date() + "开始确认表的任务");
        timer.schedule(confirmTable, 60000);
    }

    /**
     * 停止确认表的任务
     */
    public void stopConfirmTable() {
        if (confirmTable != null) {
            MyLog.v("run", new Date() + "停止确认表的任务");
            confirmTable.cancel();
            timer.purge();
        }
    }

    /**
     * 双击返回键退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsExit) {
                System.exit(0);
                this.finish();
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mIsExit = true;
                new Handler().postDelayed(() -> mIsExit = false, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 发送请求 得到仓库名
     * 请求成功，跳转页面，运行socket
     */
    private void sendRequestWithOkHttp() {
        new Thread(() -> {
            String url = "http://192.168.1.4:8980/dangan/app/getWarehouseName";
            MediaType type = MediaType.parse("application/json;charset=utf-8");
            RequestBody RequestBody2 = RequestBody.create(type, "192.168.1.100");
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        // 指定访问的服务器地址
                        .url(url).post(RequestBody2)
                        .build();
                Response response = client.newCall(request).execute();
                JSONObject resultVo = JSON.parseObject(response.body().string());
                JSONObject data = JSON.parseObject(resultVo.getString("data"));
                if (response.code() == 200) {
                    //TODO 执行请求成功的操作
                } else {
                    Looper.prepare();
                    Toast.makeText(this, "服务器处理出错！", Toast.LENGTH_LONG).show();
                    MyLog.v("查询仓库名错误", "");
                    Looper.loop();
                }
            } catch (Exception e) {
                Looper.prepare();
                Toast.makeText(this, "连接超时，请检查服务器IP", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();
    }
}
