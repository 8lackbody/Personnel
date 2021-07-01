package com.zht.personnel;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.zht.personnel.http.net.RestClient;
import com.zht.personnel.http.net.callback.IError;
import com.zht.personnel.http.net.callback.IFailure;
import com.zht.personnel.http.net.callback.ISuccess;
import com.zht.personnel.service.DownloadService;
import com.zht.personnel.socket.HostResponse;
import com.zht.personnel.socket.MyLog;
import com.zht.personnel.socket.UrlConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText androidIp;
    private Button button;
    private Button update;
    private TextView version;
    private String check = "^((25[0-5]|2[0-4]\\d|[1]{1}\\d{1}\\d{1}|[1-9]{1}\\d{1}|\\d{1})($|(?!\\.$)\\.)){4}$";
    SharedPreferences settings;
    SharedPreferences.Editor editor;


    Handler mHandler = new Handler() {

        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dialogBox();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        settings = getSharedPreferences("setting", 0);
        editor = settings.edit();
        init();
    }

    /**
     * 检查首页中的三个输入框的数据是否符合格式
     *
     * @return 是 true 否 false
     */
    public boolean checkInputData() {
        String androidIpString = androidIp.getText().toString();
        if (androidIpString.equals("")) {
            Toast.makeText(this, "不能为空", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!(androidIpString.matches(check))) {
            Toast.makeText(this, "输入IP格式有误", Toast.LENGTH_LONG).show();
            return false;
        }
        editor.putString("local", androidIpString);
        editor.commit();

        return true;
    }

    private void onClick(View view) {
        if (view == button) {
            if (checkInputData()) {
                Toast.makeText(this, "设置成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
        if (view == update) {

        }
    }

    public void init() {
        androidIp = findViewById(R.id.editText);

        androidIp.setText(settings.getString("local", "192.168.1.100"));

        button = findViewById(R.id.button);

        update = findViewById(R.id.update);

        version = findViewById(R.id.version);

        version.setText("当前版本:" + ContextApplication.VERSION);

        KeyListener listener = new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
                return chars;
            }

            @Override
            public int getInputType() {
                return 1;
            }
        };

        //给输入框添加字符限制
        androidIp.setKeyListener(listener);

        button.setOnClickListener(this::onClick);
        update.setOnClickListener(this::onClick);
    }


    public void cherkUpdate() {
        RestClient.builder()
                .url(UrlConfig.URL_CHERCK_UPDATE)
                .params("version", ContextApplication.VERSION)
                .params("deviceType", 0)
                .loader(this)
                .success(new ISuccess() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            HostResponse hostResponse = JSONObject.parseObject(response, HostResponse.class);
                            if (hostResponse.getCode() == 0) {
                                boolean isNeedUpdate = (boolean) hostResponse.getData();
                                if (isNeedUpdate) {
                                    mHandler.sendEmptyMessage(1);
                                } else {
                                    Toast.makeText(LoginActivity.this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                MyLog.v("检查版本更新错误", hostResponse.getMsg());
                            }
                        } catch (Exception e) {
                            MyLog.v("检查版本更新错误", e.getMessage());
                        }
                    }
                })
                .failure(new IFailure() {
                    @Override
                    public void onFailure() {
                        Toast.makeText(LoginActivity.this, "服务器错误！", Toast.LENGTH_SHORT).show();
                    }
                })
                .error(new IError() {
                    @Override
                    public void onError(int code, String msg) {
                        Toast.makeText(LoginActivity.this, "服务器错误！" + code, Toast.LENGTH_SHORT).show();
                    }
                })
                .build()
                .post();
    }

    public void dialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(LoginActivity.this, DownloadService.class);
                intent.putExtra("apkUrl", UrlConfig.URL_UPDATE);
                startService(intent);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setMessage("发现新版本，是否确认更新？");
        builder.setTitle("提示");
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
