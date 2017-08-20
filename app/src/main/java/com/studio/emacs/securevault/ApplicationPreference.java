package com.studio.emacs.securevault;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ganbhat on 11/12/2016.
 */

public class ApplicationPreference {

    public enum LoginMethod {
        FINGER_PRINT_LOGIN, PIN_LOGIN, PASSWORD_LOGIN
    }

    private static final String LOGIN_METHOD = "LOGIN_METHOD";
    private Context mContext;

    public ApplicationPreference(Context context){
        mContext = context.getApplicationContext();
    }

    public boolean isLoginMethodSet() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("secureValletAppPreference", Context.MODE_PRIVATE);
        String loginMethod = sharedPreferences.getString(LOGIN_METHOD,null);
        return loginMethod!=null;
    }

    public LoginMethod getLoginMethod() {
        LoginMethod loginMethod;
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("secureValletAppPreference", Context.MODE_PRIVATE);
        String loginMethodStr = sharedPreferences.getString(LOGIN_METHOD,null);
        if(loginMethodStr!=null) {
            loginMethod = LoginMethod.valueOf(loginMethodStr);
        } else {
            loginMethod = LoginMethod.PASSWORD_LOGIN;
        }
        return loginMethod;
    }

    public void setLoginMethod(LoginMethod loginMethod) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("secureValletAppPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editablePref = sharedPreferences.edit();
        editablePref.putString(LOGIN_METHOD,loginMethod.toString());
        editablePref.commit();
    }



}
