package com.fomin.aop.api.permission.core;

import android.app.Activity;

/**
 * Created by Fomin on 2018/8/30.
 */
public class PermissionAssistant {
    private PermissionAssistant() {
    }

    private static PermissionAssistant instance;

    public static PermissionAssistant getInstance() {
        if (instance == null) {
            synchronized (PermissionAssistant.class) {
                if (instance == null) {
                    instance = new PermissionAssistant();
                }
            }
        }
        return instance;
    }

    private Activity activity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
