package com.fomin.aop.api.login.core;

import android.content.Context;

/**
 * Created by Fomin on 2018/8/30.
 */
public class LoginAssistant {
    private LoginAssistant() {
    }

    private static LoginAssistant instance;

    public static LoginAssistant getInstance() {
        if (instance == null) {
            synchronized (LoginAssistant.class) {
                if (instance == null) {
                    instance = new LoginAssistant();
                }
            }
        }
        return instance;
    }

    private ILogin login;
    private Context context;

    public ILogin getLogin() {
        return login;
    }

    public void setLogin(ILogin login) {
        this.login = login;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
