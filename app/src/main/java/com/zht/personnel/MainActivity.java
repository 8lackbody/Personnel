package com.zht.personnel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zht.personnel.adapter.EPCTag;
import com.zht.personnel.adapter.HomeRecycleAdapter;
import com.zht.personnel.http.net.RestClient;
import com.zht.personnel.socket.MyLog;
import com.zht.personnel.socket.SocketClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private HomeRecycleAdapter homeAdapter;//声明适配器
    private Context context;
    private List<EPCTag> list;
    private Set<EPCTag> tableData;
    private TextView homeTitle;
    private TextView warehouseName;
    private TextView number;
    private Timer timer;
    MediaPlayer mediaPlayer;
    private GifImageView gif;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    AdminDialog adminDialog;
    Button confirm;

    Thread thread;

    //先定义
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    //然后通过一个函数来申请
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ImageView setting;

    private boolean mIsExit;

    @SuppressLint("HandlerLeak")
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
                    gif.setBackgroundResource(R.drawable.server_stop);
                    break;
                case 4:
                    homeTitle.setText(preferences.getString("mechanism_name", getString(R.string.home_title)));
                    warehouseName.setText(preferences.getString("warehouse_name", getString(R.string.warehouse_name)));
                    thread.start();
                    break;
                case 5:
                    gif.setBackgroundResource(R.drawable.reader_stop);
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
        verifyStoragePermissions(this);
        init();
        //请求获得对应仓库信息
        sendRequestWithOkHttp();

        thread = new Thread(new SocketClient() {
            @Override
            protected void onProgress(List<EPCTag> getData) {
                try {
                    if (tableData.size() == 0) {
                        startConfirmTable();
                    }
                    tableData.addAll(getData);
                    if (tableData.size() > list.size()) {
                        stopTimer();
                        startConfirmTable();
                        handler.sendEmptyMessage(1);
                    }
                } catch (Exception e) {
                    MyLog.v("socketClient error", e.getMessage());
                }
            }

            @Override
            protected void disconnection() {
                handler.sendEmptyMessage(3);
            }

            @Override
            protected void readerHeartBeatStop() {
                handler.sendEmptyMessage(5);
            }

            @Override
            protected void readerHeartBeatStart() {
                handler.sendEmptyMessage(2);
            }
        });
    }

    /**
     * 初始化view和属性
     */
    public void init() {
        //声明RecyclerView
        RecyclerView recyclerView = findViewById(R.id.home_recycler_view);
        preferences = getSharedPreferences("setting", 0);
        editor = preferences.edit();
        number = findViewById(R.id.bottom_count2);
        homeTitle = findViewById(R.id.home_title);
        homeTitle.setText(preferences.getString("mechanism_name", getString(R.string.home_title)));
        warehouseName = findViewById(R.id.warehouse_name);
        warehouseName.setText(preferences.getString("warehouse_name", getString(R.string.warehouse_name)));
        confirm = findViewById(R.id.button);
        gif = findViewById(R.id.gif);
        setting = findViewById(R.id.setting_image);
        list = new ArrayList<>();
        tableData = new HashSet<>();

        //重写列表生成时的音乐播放
        homeAdapter = new HomeRecycleAdapter(context, list) {
            @Override
            protected void startMp3() {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.test);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
            }
        };

        GridLayoutManager manager = new GridLayoutManager(context, 1);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(homeAdapter);
        timer = new Timer();

        adminDialog = new AdminDialog(this, R.style.dialog, this);
        confirm.setOnClickListener(this);
        setting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //确认按钮点击事件
        if (v == confirm) {
            stopTimer();
            setConfirm();
        }

        if (v == setting) {
            // 跳转页面去设置页面
            adminDialog.show();
        }

        if (v.getId() == R.id.btn_save_pop) {
            String adminPw = adminDialog.adminPw.getText().toString().trim();
            System.out.println(adminPw);
            if ("jisheng".equals(adminPw)) {
                adminDialog.dismiss();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                System.out.println("密码错误");
            }
        }
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
            if (haveGray) {
                stopTimer();
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
        //排序 先根据标签排序，在把警报的上移
        Collections.sort(list, (t1, t2) -> t1.getEpc().compareTo(t2.getEpc()));
        Collections.sort(list, (t1, t2) -> t2.getAlert().compareTo(t1.getAlert()));

        homeAdapter.setList(list);
        homeAdapter.notifyDataSetChanged();
        number.setText(list.size() + "");
    }

    /**
     * 开始清空表的任务
     */
    public void startCleanTable() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tableData.clear();
                list.clear();
                handler.sendEmptyMessage(1);
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                    mediaPlayer.release();
                }
                stopTimer();
            }
        }, 60000);
    }

    /**
     * 开始确认表的任务
     */
    public void startConfirmTable() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setConfirm();
            }
        }, 60000);
    }

    /**
     * 停止确认表的任务
     */
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
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
        String url = "http://" + preferences.getString("ip", "192.168.1.100") + ":8980/dangan/app/getWarehouseName";
        RestClient.builder()
                .url(url)
                .raw(preferences.getString("local", "192.168.1.100"))
                .loader(this)
                .success(response -> {
                    JSONObject resultVo = JSONObject.parseObject(response);
                    int code = resultVo.getInteger("code");
                    if (code == 0) {
                        JSONObject data = JSON.parseObject(resultVo.getString("data"));
                        editor.putString("local", data.getString("readerIp"));
                        editor.putString("warehouseId", data.getString("warehouseId"));
                        editor.putString("warehouse_name", data.getString("warehouseName"));
                        editor.putString("mechanism_name", data.getString("mechanismName"));
                        editor.commit();
                        handler.sendEmptyMessage(4);
                    } else if (code == 400) {
                        Toast.makeText(MainActivity.this, resultVo.getString("data"), Toast.LENGTH_SHORT).show();
                        handler.sendEmptyMessage(3);
                    } else if (code == 500) {
                        Toast.makeText(MainActivity.this, resultVo.getString("data"), Toast.LENGTH_SHORT).show();
                        handler.sendEmptyMessage(3);
                    }

                })
                .failure(() -> {
                    Toast.makeText(MainActivity.this, "发送失败,检查网络！", Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessage(3);
                })
                .error((code, msg) -> {
                    Toast.makeText(MainActivity.this, "服务器错误！" + msg, Toast.LENGTH_SHORT).show();
                })
                .build()
                .post();
    }

}
