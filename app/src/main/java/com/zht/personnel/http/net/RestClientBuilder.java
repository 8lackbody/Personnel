package com.zht.personnel.http.net;

import android.content.Context;

import com.zht.personnel.http.net.callback.IError;
import com.zht.personnel.http.net.callback.IFailure;
import com.zht.personnel.http.net.callback.IRequest;
import com.zht.personnel.http.net.callback.ISuccess;
import com.zht.personnel.http.ui.LoaderStyle;

import java.io.File;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RestClientBuilder {
    private String mUrl = null;
    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();
    private IRequest mIRequest = null;
    private ISuccess mISuccess = null;
    private IFailure mIFailure = null;
    private IError mIError = null;
    private RequestBody mBody = null;
    private Context mContext = null;
    private LoaderStyle mLoaderStyle = null;
    private File mFile = null;
    private String download_dir;
    private String extension;
    private String name;
 
    /**
     * 只允许同包创建类实例
     */
    RestClientBuilder(){}
 
    public final RestClientBuilder url(String url){
        this.mUrl = url;
        return this;
    }
 
    public final RestClientBuilder params(WeakHashMap<String, Object> params){
        PARAMS.putAll(params);
        return this;
    }
 
    public final RestClientBuilder params(String key, Object value){
        PARAMS.put(key, value);
        return this;
    }
 
    public final RestClientBuilder file(File file){
        this.mFile = file;
        return this;
    }
 
    public final RestClientBuilder file(String file){
        this.mFile = new File(file);
        return this;
    }
 
    public final RestClientBuilder raw(String raw){
        this.mBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"),raw);
        return this;
    }
 
    public final RestClientBuilder success(ISuccess iSuccess){
        this.mISuccess = iSuccess;
        return this;
    }
 
    public final RestClientBuilder failure(IFailure iFailure){
        this.mIFailure = iFailure;
        return this;
    }
 
    public final RestClientBuilder onRequest(IRequest iRequest){
        this.mIRequest = iRequest;
        return this;
    }
 
    public final RestClientBuilder error(IError iError){
        this.mIError = iError;
        return this;
    }
 
    public final RestClientBuilder downloadDir(String download_dir){
        this.download_dir = download_dir;
        return this;
    }
 
    public final RestClientBuilder extension(String extension){
        this.extension = extension;
        return this;
    }
 
    public final RestClientBuilder name(String name){
        this.name = name;
        return this;
    }
 
    public final RestClient build(){
        return new RestClient(mUrl,PARAMS,mIRequest,mISuccess,mIFailure,mIError,mBody,mFile,mContext,mLoaderStyle,download_dir,extension,name);
    }
 
    public final RestClientBuilder Loader(Context context, LoaderStyle style){
        this.mContext = context;
        this.mLoaderStyle = style;
        return this;
    }
 
    public final RestClientBuilder loader(Context context){
        this.mContext = context;
        this.mLoaderStyle = LoaderStyle.BallClipRotatePulseIndicator;
        return this;
    }
}