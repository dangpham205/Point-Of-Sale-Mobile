package com.example.posproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninActivity extends AppCompatActivity {
    EditText tv_login_userName, tv_login_password;
    TextView tv_signupRedirect, tv_login_btn, btn_forgot_password;
    ImageView iv_login_fbLogo;
    private long backPressedTime;
    private Toast backToast;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        initUI();

        initListener();

    }

    //    Ham tham chieu
    public void initUI(){
        progressDialog = new ProgressDialog(this);

        tv_login_password = findViewById(R.id.tv_login_password);
        tv_login_userName = findViewById(R.id.tv_login_userName);
        tv_login_btn = findViewById(R.id.tv_login_btn);
        tv_signupRedirect = findViewById(R.id.tv_signupRedirect);
        iv_login_fbLogo = findViewById(R.id.iv_login_fbLogo);
        btn_forgot_password = findViewById(R.id.btn_forgot_password);
    }

    public void initListener(){
        tv_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = tv_login_userName.getText().toString().trim();
                String strPassword = tv_login_password.getText().toString().trim();

                if (strEmail.equals("") || strPassword.equals("")){
                    Toast.makeText(SigninActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                }else{
                    onClickLogin(strEmail, strPassword);
                }
            }
        });

        tv_signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, SignupAvtivity.class);
                startActivity(intent);
            }
        });

        btn_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickForgotPassword();
            }
        });

//        iv_login_fbLogo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getBaseContext(), FacebookAuthActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                startActivity(intent);
//            }
//        });
    }

    private void onClickForgotPassword() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_forgot_password);

        Window window = dialog.getWindow();
        if (window == null){
            return;
        }

        EditText et_email = dialog.findViewById(R.id.et_email);
        TextView btn_confirm = dialog.findViewById(R.id.btn_confirm);

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = Gravity.CENTER;
        window.setAttributes(windowAttribute);

        dialog.setCancelable(true);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_email.getText().toString().equals("")){
                    Toast.makeText(SigninActivity.this, "Please enter an Email", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.show();
                    String strEmail = et_email.getText().toString().trim();
                    sendForgotPasswordEmail(strEmail);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void sendForgotPasswordEmail(String strEmail){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(strEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(SigninActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(SigninActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onClickLogin(String strEmail, String strPassword) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SigninActivity.this, "Login Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        } else{
            backToast = Toast.makeText(this, "Press back again to exit !", Toast.LENGTH_LONG);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }
}