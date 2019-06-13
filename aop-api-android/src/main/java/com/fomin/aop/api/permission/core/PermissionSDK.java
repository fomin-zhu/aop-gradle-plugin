package com.fomin.aop.api.permission.core;

import android.app.Activity;

/**
 * Created by Fomin on 2018/8/30.
 */
public class PermissionSDK {
    private static PermissionSDK instance;

    private PermissionSDK() {
    }

    public static PermissionSDK getInstance() {
        if (instance == null) {
            synchronized (PermissionSDK.class) {
                if (instance == null) {
                    instance = new PermissionSDK();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     * @param activity Activity
     */
    public void init(Activity activity) {
        PermissionAssistant.getInstance().setActivity(activity);
    }
}
