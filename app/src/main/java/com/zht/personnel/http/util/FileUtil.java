package com.zht.personnel.http.util;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
 
    private static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getPath();
    //格式化的模板
    private static final String TIME_FORMAT = "_yyyyMMdd_HHmmss";
 
    public static File writeToDisk(InputStream is, String dir, String name) {
        final File file = FileUtil.createFile(dir, name);
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            byte data[] = new byte[1024 * 4];
            int count;
            while ((count = bis.read(data)) != -1) {
                bos.write(data, 0, count);
            }
            bos.flush();
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
 
    public static File writeToDisk(InputStream is, String dir, String prefix, String extension) {
        final File file = FileUtil.createFileByTime(dir, prefix, extension);
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            byte data[] = new byte[1024 * 4];
            int count;
            while ((count = bis.read(data)) != -1) {
                bos.write(data, 0, count);
            }
            bos.flush();
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (bis != null) {
                    bis.close();
                }
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 
        return file;
    }
 
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File createFile(String sdcardDirName, String fileName) {
        return new File(createDir(sdcardDirName), fileName);
    }
 
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File createDir(String sdcardDirName) {
        //拼接成SD卡中完整的dir
        final String dir = SDCARD_DIR + "/" + sdcardDirName + "/";
        final File fileDir = new File(dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }
 
    private static File createFileByTime(String sdcardDirName, String timeFormatHeader, String extension) {
        final String fileName = getFileNameByTime(timeFormatHeader, extension);
        return createFile(sdcardDirName, fileName);
    }
 
    /**
     * @param timeFormatHeader 格式化的头(除去时间部分)
     * @param extension        后缀名
     * @return 返回时间格式化后的文件名
     */
    public static String getFileNameByTime(String timeFormatHeader, String extension) {
        return getTimeFormatName(timeFormatHeader) + "." + extension;
    }
 
    private static String getTimeFormatName(String timeFormatHeader) {
        final Date date = new Date(System.currentTimeMillis());
        //必须要加上单引号
        final SimpleDateFormat dateFormat = new SimpleDateFormat("'" + timeFormatHeader + "'" + TIME_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }
 
    //获取文件的后缀名
    public static String getExtension(String filePath) {
        String suffix = "";
        final File file = new File(filePath);
        final String name = file.getName();
        final int idx = name.lastIndexOf('.');
        if (idx > 0) {
            suffix = name.substring(idx + 1);
        }
        return suffix;
    }
}