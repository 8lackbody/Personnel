package com.zht.personnel;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class AdminDialog extends Dialog {

    /**
     * 上下文对象 *
     */
    Activity context;

    public EditText adminPw;

    private View.OnClickListener mClickListener;

    public AdminDialog(Activity context) {
        super(context);
        this.context = context;
    }

    public AdminDialog(Activity context, int theme, View.OnClickListener clickListener) {
        super(context, theme);
        this.context = context;
        this.mClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 指定布局
        this.setContentView(R.layout.admin_password_dialog);

        adminPw = (EditText) findViewById(R.id.admin_password);
        adminPw.setHint("输入密码");
        Window dialogWindow = this.getWindow();

        WindowManager m = context.getWindowManager();
        // 获取屏幕宽、高用
        Display d = m.getDefaultDisplay();
        // 获取对话框当前的参数值
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        // 宽度设置为屏幕的0.8
        p.width = (int) (d.getWidth() * 0.8);
        dialogWindow.setAttributes(p);

        // 根据id在布局中找到控件对象
        Button btn_save = findViewById(R.id.btn_save_pop);

        // 为按钮绑定点击事件监听器
        btn_save.setOnClickListener(mClickListener);

        this.setCancelable(true);
    }

}