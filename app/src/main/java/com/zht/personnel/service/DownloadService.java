package com.zht.personnel.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;


import com.zht.personnel.R;
import com.zht.personnel.socket.DownloadUtil;
import com.zht.personnel.socket.MyLog;

import java.io.File;

/**
 * 自动下载更新apk服务
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class DownloadService extends Service {

    private String mDownloadUrl; //APK的下载路径
    private NotificationManager mNotifyMgr;
    private Notification mNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifyMgr = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            notifyMsg("温馨提醒", "文件下载失败", 0);
            stopSelf();
        }
        mDownloadUrl = intent.getStringExtra("apkUrl"); //获取下载APK的链接
        downloadFile(mDownloadUrl); //下载APK
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void notifyMsg(String title, String content, int progress) {

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = "channel_1";
            NotificationChannel channel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_HIGH);
            mNotifyMgr.createNotificationChannel(channel);
            builder = new Notification.Builder(this, id)
                    .setCategory(Notification.CATEGORY_EVENT)
                    .setSmallIcon(R.drawable.back)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true);
        } else {
            builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.back)
                    .setContentTitle(title)
                    .setContentText(content);
        }
        if (progress > 0 && progress <= 100) {
            //下载进行中
            builder.setProgress(100, progress, false);
        }

        if (progress > 100) {
            //下载完成
            builder.setContentIntent(getInstallIntent());
        }
        mNotifyMgr.notify(0, builder.build());
    }

    /**
     * 安装apk文件
     *
     * @return
     */
    private PendingIntent getInstallIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //重新构造Uri：content://
        Uri fileUri;
        File apkFile = new File(Environment.getExternalStorageDirectory(), "/tablet.apk");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(this, "com.zht.UPDATE_APP_FILE_PROVIDER", apkFile);
            //授予目录临时共享权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            fileUri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        //使用Intent传递Uri
        startActivity(intent);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    /**
     * 下载apk文件
     *
     * @param url
     */
    private void downloadFile(String url) {
        DownloadUtil.get().download(url, Environment.getExternalStorageDirectory().getAbsolutePath(), "/tablet.apk",
                new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        notifyMsg("温馨提醒", "文件下载已完成", 101);
                        stopSelf();
                    }

                    @Override
                    public void onDownloading(int progress) {
                        if (progress % 10 == 0) {
                            //避免频繁刷新View，这里设置每下载10%提醒更新一次进度
                            notifyMsg("温馨提醒", "文件正在下载..", progress);
                        }
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        MyLog.e("error", e.getMessage());
                        notifyMsg("温馨提醒", "文件下载失败", 0);
                        stopSelf();
                    }
                });
    }
}