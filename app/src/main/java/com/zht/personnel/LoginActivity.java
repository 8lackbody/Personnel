package com.zht.personnel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zht.personnel.socket.MyLog;
import okhttp3.*;

public class LoginActivity extends AppCompatActivity {

    private EditText androidIp;
    private EditText serverIp;
    private Button button;
    private String check = "^((25[0-5]|2[0-4]\\d|[1]{1}\\d{1}\\d{1}|[1-9]{1}\\d{1}|\\d{1})($|(?!\\.$)\\.)){4}$";
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        settings = getSharedPreferences("setting", 0);
        editor = settings.edit();
        init();

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
        serverIp.setKeyListener(listener);

        button.setOnClickListener(this::onClick);
    }

    /**
     * 发送请求 得到仓库名
     * 请求成功，跳转页面，运行socket
     */
    private void sendRequestWithOkHttp() {
        new Thread(() -> {
            String url = "http://" + settings.getString("ip", "192.168.1.4")
                    + ":8980/dangan/app/getWarehouseName";
            MediaType type = MediaType.parse("application/json;charset=utf-8");
            RequestBody RequestBody2 = RequestBody.create(type, androidIp.getText().toString());
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
                    Intent intent;
                    intent = new Intent().setClass(LoginActivity.this, MainActivity.class);
                    intent.putExtra("title", data.getString("mechanismName"));
                    intent.putExtra("warehouse", data.getString("warehouseName"));
                    startActivity(intent);
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

    /**
     * 检查首页中的三个输入框的数据是否符合格式
     *
     * @return 是 true 否 false
     */
    public boolean checkInputData() {
        String androidIpString = androidIp.getText().toString();
        String serverIpString = serverIp.getText().toString();
        if (androidIpString.equals("") || serverIpString.equals("")) {
            Toast.makeText(this, "不能为空", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!(serverIpString.matches(check) && androidIpString.matches(check))) {
            Toast.makeText(this, "输入IP格式有误", Toast.LENGTH_LONG).show();
            return false;
        }
        editor.putString("ip", serverIpString);
        editor.putString("local", androidIpString);
        editor.commit();

        return true;
    }

    private void onClick(View view) {
        if (checkInputData()) {
            sendRequestWithOkHttp();
        }
    }

    public void init() {
        androidIp = findViewById(R.id.editText);
        serverIp = findViewById(R.id.editText2);

        serverIp.setText(settings.getString("ip", "192.168.1.4"));
        androidIp.setText(settings.getString("local", "192.168.1.100"));

        button = findViewById(R.id.button);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
