package com.example.intranet;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue referenceQueue;
    private static Context mCtx;

    private MySingleton(Context context){
        mCtx = context;
        referenceQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue(){
        if(referenceQueue==null){
            referenceQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return referenceQueue;
    }
    public static synchronized MySingleton getInstance(Context context){
        if(mInstance==null){
            mInstance = new MySingleton(context);
        }
        return  mInstance;
    }
    public<T> void addToRequestQue(Request<T> request){
        getRequestQueue().add(request);
    }
}
