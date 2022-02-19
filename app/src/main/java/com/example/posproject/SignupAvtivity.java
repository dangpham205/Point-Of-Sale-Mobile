package com.example.posproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupAvtivity extends AppCompatActivity {

    EditText tv_signup_userName, tv_signup_password, tv_signup_rePassword;
    TextView tv_loginRedirect, tv_signup_btn;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initUI();

        initListener();
    }

    //    Ham tham chieu
    private void initUI(){
        progressDialog = new ProgressDialog(this);

        tv_signup_userName = findViewById(R.id.tv_signup_userName);
        tv_signup_password = findViewById(R.id.tv_signup_password);
        tv_signup_btn = findViewById(R.id.tv_signup_btn);
        tv_loginRedirect = findViewById(R.id.tv_loginRedirect);
        tv_signup_rePassword = findViewById(R.id.tv_signup_rePassword);
    }

    public void initListener(){
        tv_signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
//            Kiem tra dieu kien logic
            public void onClick(View view) {
                if (tv_signup_userName.getText().toString().equals("") || tv_signup_password.getText().toString().equals("") ||tv_signup_rePassword.getText().toString().equals("")){
                    Toast.makeText(SignupAvtivity.this, "Please fill all fields !", Toast.LENGTH_SHORT).show();
                }else{
                    if (tv_signup_password.getText().toString().equals(tv_signup_rePassword.getText().toString())){
                        if (tv_signup_password.getText().toString().length() < 6){
                            Toast.makeText(SignupAvtivity.this, "Password must be at least 6 letters", Toast.LENGTH_SHORT).show();
                        }else{
                            onClickSignup();
                        }
                    }else {
                        Toast.makeText(SignupAvtivity.this, "Password do not match, retry !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tv_loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupAvtivity.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void onClickSignup() {
        progressDialog.show();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String strEmail = tv_signup_userName.getText().toString().trim();
        String strPassword = tv_signup_password.getText().toString().trim();
        String strRePassword = tv_signup_rePassword.getText().toString().trim();

//        if (strEmail.equals("") || strPassword.equals("") || strRePassword.equals("")) {
//            Toast.makeText(this, "Please fill all fields !", Toast.LENGTH_SHORT).show();
//        }else if (strPassword.equals(strRePassword) == false ){
//            Toast.makeText(this, "Password do not match, Retry !", Toast.LENGTH_SHORT).show();
//        }
        auth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignupAvtivity.this, "Signup failed, please check your Email",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//        }
    }
}
