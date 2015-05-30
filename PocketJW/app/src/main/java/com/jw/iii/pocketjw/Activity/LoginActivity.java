package com.jw.iii.pocketjw.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.jw.iii.pocketjw.IIIApplication;
import com.jw.iii.pocketjw.UI.CircularImage;
import com.jw.iii.pocketjw.R;


public class LoginActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化IIIApplication
        iiiApplication = (IIIApplication)getApplication();

        // 以下两行连接LeanCloud，切勿修改
        AVOSCloud.initialize(this, "dohthw768v153y9t2eldqiwvtwf9vu07vvyzxv4kjdqbpdsf", "gs5r4j0xg7wg0xkmvjrgdv4gt1hxiaqxpso3jfzani5w8hhk");
        AVAnalytics.trackAppOpened(getIntent());

        // 已有登陆记录则自动登陆
        SharedPreferences preferences = getSharedPreferences("jw_pref", MODE_PRIVATE);
        loginUsername = preferences.getString("lastLoginUsername", "");
        loginPassword = preferences.getString("lastLoginPassword", "");
        if (loginUsername != "" && loginPassword != "") {
            login();
        }

        // 获取布局资源
        avatar = (CircularImage)findViewById(R.id.default_avatar);
        etUsername = (EditText)findViewById(R.id.username);
        etPassword = (EditText)findViewById(R.id.password);
        btLogin = (Button)findViewById(R.id.login);

        // 初始化
        avatar.setImageResource(R.drawable.default_avatar);

        if (loginUsername != "") {
            // 自动填充用户名
            etUsername.setText(loginUsername);
        }

        // 监听事件
        btLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                loginUsername = etUsername.getText().toString();
                loginPassword = etPassword.getText().toString();
                login();

                break;
            default:
                break;
        }
    }

    private void login() {
        if (loginQuery()) {

            // 保存登陆信息
            SharedPreferences preferences = getSharedPreferences("jw_pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("lastLoginUsername", loginUsername);
            // editor.putString("lastLoginPassword", loginPassword);
            editor.commit();

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(intent);
                    LoginActivity.this.finish();
                }
            });
        }
    }

    private boolean loginQuery() {
        AVUser.logInInBackground(loginUsername, loginPassword, new LogInCallback<AVUser>() {
            @Override
            public void done(AVUser avUser, AVException e) {
                if (avUser != null) {
                    iiiApplication.currentUser = avUser;
                    loginStatus = true;
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    loginStatus = false;
                }
            }
        });
        return loginStatus;
    }

    private CircularImage avatar;
    private EditText etUsername;
    private EditText etPassword;
    private Button btLogin;

    private String loginUsername;
    private String loginPassword;

    private boolean loginStatus;

    private Intent intent;

    private IIIApplication iiiApplication;
}
