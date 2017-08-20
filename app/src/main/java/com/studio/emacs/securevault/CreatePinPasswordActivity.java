package com.studio.emacs.securevault;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.studio.emacs.securevault.safe.CryptoHelper;
import com.studio.emacs.securevault.safe.CryptoHelperException;
import com.studio.emacs.securevault.safe.DBHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePinPasswordActivity extends AppCompatActivity {

    private static final String TAG = "CreatePinPasActivity";
    private ApplicationPreference.LoginMethod loginMethod = null;
    private int FINGERPRINT_PERMISSION_REQUEST_CODE = 1356;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @BindView(R.id.pinCreationLayt)
    View pinCreationLayout;

    @BindView(R.id.passwordCreationLayt)
    View passwordCreationLayt;

    @BindView(R.id.fingerprintCreationLyt)
    View fingerprintCreationLyt;

    @BindView(R.id.createLoginButton)
    Button loginBtn;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.confirmPasswd)
    EditText mConfirmPassword;

    @BindView(R.id.pin)
    EditText mPin;

    @BindView(R.id.confirmPin)
    EditText mConfirmPin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin_password);
        ButterKnife.bind(this);
        loginBtn.setEnabled(false);
        getSupportActionBar().setTitle("Create Password");

        keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

        ApplicationPreference appPrf = new ApplicationPreference(this);
        if(!appPrf.isLoginMethodSet()){

        }
    }


    @OnClick(R.id.passwordSelectionBtn)
    public void onPasswordSelectionBtn(){
        passwordCreationLayt.setVisibility(View.VISIBLE);
        pinCreationLayout.setVisibility(View.GONE);
        fingerprintCreationLyt.setVisibility(View.GONE);
        loginBtn.setEnabled(true);
        loginMethod = ApplicationPreference.LoginMethod.PASSWORD_LOGIN;
    }

    @OnClick(R.id.pinSelectionBtn)
    public void onPinBtn(){
        passwordCreationLayt.setVisibility(View.GONE);
        pinCreationLayout.setVisibility(View.VISIBLE);
        fingerprintCreationLyt.setVisibility(View.GONE);
        loginBtn.setEnabled(true);
        loginMethod= ApplicationPreference.LoginMethod.PIN_LOGIN;
    }

    @OnClick(R.id.fingerPrintSelectionBtn)
    public void onFingerPrintBtn(){
        passwordCreationLayt.setVisibility(View.GONE);
        pinCreationLayout.setVisibility(View.GONE);
        fingerprintCreationLyt.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(true);
        loginMethod= ApplicationPreference.LoginMethod.FINGER_PRINT_LOGIN;
    }

    @OnClick(R.id.createLoginButton)
    public void onCreateLoginButtonClick(){
        try {
            performValidation();
        } catch(Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            return;
        }

        ApplicationPreference appPrf = new ApplicationPreference(this);
        appPrf.setLoginMethod(loginMethod);

        String password = null;
        switch (loginMethod) {
            case PASSWORD_LOGIN:
                password = mPassword.getText().toString();
                break;
            case PIN_LOGIN:
                password = mPassword.getText().toString();
                break;
            case FINGER_PRINT_LOGIN:

                break;
            default:
        }

        generateMasterPasswordAndSave(password);
        checkUserPassword(password);

        setResult(RESULT_OK);
        finish();
    }

    private void generateMasterPasswordAndSave(String password) {
        CryptoHelper ch = new CryptoHelper(CryptoHelper.EncryptionStrong);
        ch.setPassword(password);
        String masterKey = CryptoHelper.generateMasterKey();
        Log.i(TAG, "Saving Password: " + masterKey);
        DBHelper dbHelper = null;
        try {
            dbHelper = new DBHelper(getApplicationContext());
            dbHelper.beginTransaction();
            String encryptedMasterKey = ch.encrypt(masterKey);
            dbHelper.storeMasterKey(encryptedMasterKey);
            dbHelper.commit();
        } catch (CryptoHelperException e) {
            Log.e(TAG, e.toString());
        } finally {
            if(dbHelper!=null) {
                dbHelper.close();
                dbHelper = null;
            }
        }
    }

    private boolean checkUserPassword(String password) {
        DBHelper dbHelper = null;
        String masterKey = null;
        try {
            CryptoHelper ch = new CryptoHelper(CryptoHelper.EncryptionStrong);
            ch.setPassword(password);
            dbHelper = new DBHelper(getApplicationContext());
            String encryptedMasterKey = dbHelper.fetchMasterKey();
            String decryptedMasterKey = "";
            try {
                decryptedMasterKey = ch.decrypt(encryptedMasterKey);
            } catch (CryptoHelperException e) {
                Log.e(TAG, e.toString());
            }
            if (ch.getStatus()==true) {
                masterKey=decryptedMasterKey;
                Log.i(TAG, "master password retrieved is:"+masterKey);
                return true;
            }
            masterKey=null;
        } finally {
            if(dbHelper!=null) {
                dbHelper.close();
                dbHelper = null;
            }
        }

        return false;
    }


    private void performValidation() throws Exception {
        switch (loginMethod) {
            case PASSWORD_LOGIN:
                String passwd = mPassword.getText().toString();
                String confirmPasswd = mConfirmPassword.getText().toString();
                if(passwd.trim().length()<=0 || confirmPasswd.trim().length()<=0) {
                    throw new Exception("Password cannot be empty");
                }

                if(!passwd.trim().equals(confirmPasswd.trim())){
                    throw new Exception("Password should be same as Confirm password");
                }

                break;
            case FINGER_PRINT_LOGIN:
                    /* check fingerprint login availabe */
                try {
                    checkFingerPrintLoginAvailable();
                } catch (Exception e) {
                    Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT);
                }

                break;
            case PIN_LOGIN:
                String pinStr = mPin.getText().toString();
                String confirmPinStr = mConfirmPin.getText().toString();
                if(pinStr.trim().length()<=0 || confirmPinStr.trim().length()<=0) {
                    throw new Exception("Pin cannot be empty");
                }

                if(!pinStr.trim().equals(confirmPinStr.trim())){
                    throw new Exception("Pin should be same as Confirm pin");
                }

                break;
        }
    }

    private void checkFingerPrintLoginAvailable() throws Exception {
        if (!keyguardManager.isKeyguardSecure()) {
            throw new Exception("Lock screen security not enabled in Settings");
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            throw new Exception("Fingerprint authentication permission not enabled");
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            throw new Exception("Register at least one fingerprint in Settings");
        }
    }

}
