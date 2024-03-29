package com.zht.personnel.http.net;

import android.content.Context;


import com.zht.personnel.http.net.callback.IError;
import com.zht.personnel.http.net.callback.IFailure;
import com.zht.personnel.http.net.callback.IRequest;
import com.zht.personnel.http.net.callback.ISuccess;
import com.zht.personnel.http.net.callback.RequestCallbacks;
import com.zht.personnel.http.ui.LatteLoader;
import com.zht.personnel.http.ui.LoaderStyle;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class RestClient {
    private final String URL;
    private static final WeakHashMap<String, Object> PARAMS = RestCreator.getParams();
    private final IRequest IREQUEST;
    private final String DOWNLOAD_DIR;
    private final String EXTENSION;
    private final String NAME;
    private final ISuccess ISUCCESS;
    private final IFailure IFAILURE;
    private final IError IERROR;
    private final RequestBody BODY;
    private final LoaderStyle LOADER_STYLE;
    private final File FILE;
    private final Context CONTEXT;
 
    public RestClient(String url, Map<String, Object> params, IRequest iRequest, ISuccess iSuccess, IFailure iFailure,
                      IError iError, RequestBody body, File file, Context context, LoaderStyle loaderStyle, String download_dir, String extension, String name) {
        this.URL = url;
        PARAMS.putAll(params);
        this.IREQUEST = iRequest;
        this.ISUCCESS = iSuccess;
        this.IFAILURE = iFailure;
        this.IERROR = iError;
        this.BODY = body;
        this.FILE = file;
        this.CONTEXT = context;
        this.LOADER_STYLE = loaderStyle;
        this.DOWNLOAD_DIR = download_dir;
        this.EXTENSION = extension;
        this.NAME = name;
    }
 
    public static RestClientBuilder builder() {
        return new RestClientBuilder();
    }
 
    private void request(HttpMethod method) {
        final RestService service = RestCreator.getRestService();
        Call<String> call = null;
        if (IREQUEST != null) {
            IREQUEST.onRequestStart();
        }
        if (LOADER_STYLE != null) {
            LatteLoader.showLoading(CONTEXT, LOADER_STYLE);
        }
        switch (method) {
            case GET:
                call = service.get(URL, PARAMS);
                break;
            case POST:
                call = service.post(URL, PARAMS);
                break;
            case POST_RAW:
                call = service.postRaw(URL, BODY);
                break;
            case PUT:
                call = service.put(URL, PARAMS);
                break;
            case PUT_RAW:
                call = service.putRaw(URL, BODY);
                break;
            case DELETE:
                call = service.delete(URL, PARAMS);
                break;
            case UPLOAD:
                final RequestBody requestBody = RequestBody.create(MediaType.parse(MultipartBody.FORM.toString()),FILE);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("file",FILE.getName(),requestBody);
                call = RestCreator.getRestService().upload(URL,body);
                break;
            default:
                break;
        }
        if (call != null) {
            call.enqueue(getRequestCallback());
        }
    }
 
    private Callback<String> getRequestCallback() {
        return new RequestCallbacks(
                IREQUEST,
                ISUCCESS,
                IFAILURE,
                IERROR,
                LOADER_STYLE
        );
    }
 
    public final void get() {
        request(HttpMethod.GET);
    }
 
    public final void post() {
        if (BODY == null){
            request(HttpMethod.POST);
        }else{
            if (!PARAMS.isEmpty()){
                throw new RuntimeException("params must be null!");
            }
            request(HttpMethod.POST_RAW);
        }
    }
 
    public final void put() {
        if (BODY == null){
            request(HttpMethod.PUT);
        }else{
            if (!PARAMS.isEmpty()){
                throw new RuntimeException("params must be null!");
            }
            request(HttpMethod.PUT_RAW);
        }
    }
 
    public final void delete() {
        request(HttpMethod.DELETE);
    }
 

}