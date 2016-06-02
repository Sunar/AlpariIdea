package com.alpari.avia_kos.alpidea.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alpari.avia_kos.alpidea.DB;
import com.alpari.avia_kos.alpidea.R;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Avia-Kos on 06.05.16.
 */
public class RegistrationActivity extends Activity {
    private static final int LOGIN_VACANT = 1;
    private static final int LOGIN_ALREADY_EXISTS = 2;
    private static final int PASSWORDS_DONT_MATCH = 3;
    private static final int PASSWORDS_CORRECT = 4;
    private static final int START_REGISTRATION = 5;
    private static final int END_REGISTRATION = 6;
    private EditText etLogin;
    private EditText etPassword;
    private EditText etPassword2;
    private EditText etFIO;
    private EditText etTel;
    private CheckBox cbExpert;
    private Button btnRegister;
    private ProgressDialog pd;
    private StHandler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPassword2 = (EditText) findViewById(R.id.etPassword2);
        etFIO = (EditText) findViewById(R.id.etFIO);
        etTel = (EditText) findViewById(R.id.etTel);
        cbExpert = (CheckBox) findViewById(R.id.cbExpert);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        h = new StHandler(this);

        etLogin.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(((EditText)v).getText().toString().equals("")){
                        Toast.makeText(RegistrationActivity.this, "Логин обязателен для заполнения", Toast.LENGTH_SHORT);
                        return;
                    }
                    pd = new ProgressDialog(RegistrationActivity.this);
                    pd.setTitle("Подождите");
                    pd.setMessage("Проверка логина");
                    pd.show();
                    CheckLoginTask task = new CheckLoginTask(etLogin.getText().toString());
                    task.execute((Void)null);
                }

            }
        });

        etPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(etPassword.getText().toString().equals(etPassword2.getText().toString())){
                        h.sendEmptyMessage(PASSWORDS_CORRECT);
                    }
                    else {
                        h.sendEmptyMessage(PASSWORDS_DONT_MATCH);
                    }
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etLogin.getError() != null || etPassword2.getError() != null){
                    Toast.makeText(RegistrationActivity.this, "Проверьте данные и попробуйте еще раз", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(etFIO.getText().toString().equals("")){
                    Toast.makeText(RegistrationActivity.this, "ФИО обязательно для заполнения!", Toast.LENGTH_SHORT).show();
                    return;
                }
                h.sendEmptyMessage(START_REGISTRATION);
                UserRegisterTask task = new UserRegisterTask(etLogin.getText().toString(), etPassword.getText().toString(), etFIO.getText().toString(), etTel.getText().toString(), cbExpert.isChecked());
                task.execute((Void)null);
            }
        });
    }

    public class CheckLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mLogin;
        CheckLoginTask(String login){
            mLogin = login;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            DB db = DB.getInstance();
            try {
                if (db.loginIsVacant(mLogin)) {
                    h.sendEmptyMessage(LOGIN_VACANT);
                }
                else {
                    h.sendEmptyMessage(LOGIN_ALREADY_EXISTS);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mPassword;
        private final String mFullName;
        private final String mTelephone;
        private final boolean mExpert;

        public UserRegisterTask(String mLogin, String mPassword, String mFullName, String mTelephone, boolean mExpert) {
            this.mLogin = mLogin;
            this.mPassword = mPassword;
            this.mFullName = mFullName;
            this.mTelephone = mTelephone;
            this.mExpert = mExpert;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            DB db = DB.getInstance();
            try {
                return db.register(mLogin, mPassword, mFullName, mTelephone);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            try {
                Intent intent = new Intent();
                intent.putExtra("name", etLogin.getText().toString());
                intent.putExtra("password", etPassword.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            } finally {
                h.sendEmptyMessage(END_REGISTRATION);
            }
        }

    }

    static class StHandler extends Handler {

        WeakReference<RegistrationActivity> wrActivity;

        public StHandler(RegistrationActivity activity) {
            wrActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RegistrationActivity activity = wrActivity.get();
            if (activity != null){
                switch(msg.what){
                    case LOGIN_VACANT:
                        activity.pd.dismiss();
                        activity.etLogin.setError(null);
                        break;
                    case LOGIN_ALREADY_EXISTS:
                        activity.etLogin.setError("Логин занят");
                        activity.pd.dismiss();
                        break;
                    case PASSWORDS_DONT_MATCH:
                        activity.etPassword2.setError("Пароли не совпадают");
                        break;
                    case PASSWORDS_CORRECT:
                        activity.etPassword2.setError(null);
                        break;
                    case START_REGISTRATION:
                        activity.pd = new ProgressDialog(activity);
                        activity.pd.setTitle("Подождите");
                        activity.pd.setMessage("Регистрация");
                        activity.pd.show();
                        break;
                    case END_REGISTRATION:
                        activity.pd.dismiss();
                        break;
                }
            }

        }
    }

}
