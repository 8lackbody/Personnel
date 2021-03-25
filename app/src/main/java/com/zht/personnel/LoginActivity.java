package com.zht.personnel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            Toast.makeText(this, "设置成功", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
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
