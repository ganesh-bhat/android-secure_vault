package com.studio.emacs.securevault;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private static final int CREATE_CREDENIALS = 1234;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApplicationPreference appPrf = new ApplicationPreference(this);
        if(!appPrf.isLoginMethodSet()){
            Intent intent = new Intent(this, CreatePinPasswordActivity.class);
            startActivityForResult(intent,CREATE_CREDENIALS);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CREATE_CREDENIALS) {
            ApplicationPreference appPrf = new ApplicationPreference(this);
            if(!appPrf.isLoginMethodSet()){
                Toast.makeText(this, "Setting credentials is mandatory. Exiting",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
