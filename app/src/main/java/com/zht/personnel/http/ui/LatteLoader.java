package com.zht.personnel.http.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.wang.avi.AVLoadingIndicatorView;
import com.zht.personnel.R;
import com.zht.personnel.http.util.DimenUtil;

import java.util.ArrayList;

public class LatteLoader {
 
    private static final int LOADER_SIZE_SCALE = 8;
    private static final int LOADER_OFFSET_SCALE = 10;
    private static final ArrayList<Dialog> LOADERS = new ArrayList<>();
    private static final String DEFAULT_LOADER = LoaderStyle.BallClipRotatePulseIndicator.name();
 
    public static void showLoading(Context context, String type){
        final Dialog dialog = new Dialog(context, R.style.dialog);
        final AVLoadingIndicatorView avLoadingIndicatorView = LoaderCreator.create(type,context);
        dialog.setContentView(avLoadingIndicatorView);
        int deviceWidth = DimenUtil.getScreenWidth(context);
        int deviceHeight = DimenUtil.getScreenHeight(context);
        final Window dialogWindow = dialog.getWindow();
        if (dialogWindow!=null){
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = deviceWidth/LOADER_SIZE_SCALE;
            lp.height = deviceHeight/LOADER_SIZE_SCALE;
            lp.height = lp.height + deviceHeight/LOADER_OFFSET_SCALE;
            lp.gravity = Gravity.CENTER;
        }
        LOADERS.add(dialog);
        dialog.show();
    }
 
    public static void showLoading(Context context){
        showLoading(context,DEFAULT_LOADER);
    }
 
    public static void stopLoading(){
        for(Dialog dialog:LOADERS){
            if (dialog!=null){
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        }
    }
 
    public static void showLoading(Context context, Enum<LoaderStyle> type){
        showLoading(context,type.name());
    }
}