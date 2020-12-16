package com.zht.personnel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONObject;
import com.zht.personnel.adapter.EPCTag;
import com.zht.personnel.adapter.HomeRecycleAdapter;
import com.zht.personnel.socket.MyLog;
import com.zht.personnel.socket.SocketClient;
import pl.droidsonroids.gif.GifImageView;

import java.util.*;

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
    private Timer cleanTable;
    private Timer confirmTable;
    private EPCTag epcTag;
    private GifImageView gif;

    //判断是否需要开始清空表任务
    private boolean flag = false;

    private boolean mIsExit;

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
            protected void onProgress(String msg) {
                try {
                    epcTag = JSONObject.parseObject(msg, EPCTag.class);
                    if (tableData.size() == 0) {
                        startConfirmTable();
                    }
                    tableData.add(epcTag);
                    if (tableData.size() > list.size()) {
                        stopConfirmTable();
                        startConfirmTable();
                        if (flag == true) {
                            stopCleanTable();
                        }
                        handler.sendEmptyMessage(1);
                    }
                }catch (Exception e){
                    MyLog.v("main error",e.getMessage());
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
        };
        new Thread(socketClient).start();
    }

    /**
     * 初始化view和属性
     */
    public void init() {
        Intent intent = getIntent();
        recyclerView = findViewById(R.id.home_recycler_view);
        number = findViewById(R.id.bottom_count2);
        homeTitle = findViewById(R.id.home_title);
        homeTitle.setText(intent.getStringExtra("title"));
        warehouseName = findViewById(R.id.warehouse_name);
        warehouseName.setText(intent.getStringExtra("warehouse"));
        confirm = findViewById(R.id.button);
        gif = findViewById(R.id.gif);
        list = new ArrayList<>();
        tableData = new HashSet<>();
        homeAdapter = new HomeRecycleAdapter(context, list);
        GridLayoutManager manager = new GridLayoutManager(context, 1);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);

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
            MyLog.v("run",new Date() + "确认按钮出发的事件");
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
        MyLog.v("run",new Date() + "开始清空表的任务");
        flag = true;
        cleanTable = new Timer();
        cleanTable.schedule(new TimerTask() {
            @Override
            public void run() {
                tableData.clear();
                list.clear();
                handler.sendEmptyMessage(1);
                stopCleanTable();
                stopConfirmTable();
            }
        }, 60000);
    }

    /**
     * 停止清空表的任务
     */
    public void stopCleanTable() {
        if (cleanTable != null) {
            MyLog.v("run",new Date() + "停止清空表的任务");
            cleanTable.cancel();
            cleanTable.purge();
            flag = false;
        }
    }

    /**
     * 开始确认表的任务
     */
    public void startConfirmTable() {
        MyLog.v("run",new Date() + "开始确认表的任务");
        confirmTable = new Timer();
        confirmTable.schedule(new TimerTask() {
            @Override
            public void run() {
                setConfirm();
            }
        }, 60000);
    }

    /**
     * 停止确认表的任务
     */
    public void stopConfirmTable() {
        if (confirmTable != null) {
            MyLog.v("run",new Date() + "停止确认表的任务");
            confirmTable.cancel();
            confirmTable.purge();
        }
    }

    /**
     * 双击返回键退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsExit) {
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
}
