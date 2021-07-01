package com.zht.personnel.socket;

public class UrlConfig {

    /**
     * 需要访问服务器两个接口
     * 第一个接口： 查找需要盘库的数据量， 如果访问失败或者返回错误就使用默认的差值
     * 第二个接口： 查找盘库结果，是否有未查找到的档案和未知的档案
     * 第三个接口： 更新APP
     * 第四个接口： 检查是否需要更新
     */
    public static final String URL_GETHOUSENAME = "http://192.168.1.3:8980/dangan/app/getWarehouseName";

    public static final String URL_UPDATE = "http://192.168.1.3:8980/dangan/app/download";

    public static final String URL_CHERCK_UPDATE = "http://192.168.1.3:8980/dangan/app/checkVersion";


}
