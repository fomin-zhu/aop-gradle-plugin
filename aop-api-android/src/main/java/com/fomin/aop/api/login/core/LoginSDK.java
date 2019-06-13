package com.fomin.aop.api.login.core;

import android.content.Context;

/**
 * Created by Fomin on 2018/8/30.
 */
public class LoginSDK {
    private static LoginSDK instance;

    private LoginSDK() {
    }

    public static LoginSDK getInstance() {
        if (instance == null) {
            synchronized (LoginSDK.class) {
                if (instance == null) {
                    instance = new LoginSDK();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     * @param context Context
     * @param iLogin 登录事件
     */
    public void init(Context context, ILogin iLogin) {
        LoginAssistant.getInstance().setContext(context);
        LoginAssistant.getInstance().setLogin(iLogin);
    }
}
