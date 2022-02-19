package com.example.posproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.posproject.fragment.ChangePasswordFragment;
import com.example.posproject.fragment.ProfileFragment;
import com.example.posproject.fragment.ReceiptsFragment;
import com.example.posproject.fragment.SalesFragment;
import com.example.posproject.fragment.SettingsFragment;
import com.example.posproject.fragment.ShopFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public static final int MY_REQUEST_CODE = 19; // Request from my profile

    private static final int frag_sales = 0;
    private static final int frag_receipts = 1;
    private static final int frag_shop = 2;
    private static final int frag_settings = 3;
    private static final int frag_profile = 4;
    private static final int frag_change_password = 5;
    private int frag_current = frag_sales;

    private long backPressedTime;
    private Toast backToast;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView img_avatar;
    private TextView tv_user_name, tv_email;
    final private ProfileFragment mProfileFragment = new ProfileFragment();

    private InternetBroadcast internetBroadcast;

    final private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK){
                Intent intent = result.getData();
                if (intent == null){
                    return;
                }
                Uri uri = intent.getData();
                mProfileFragment.setUri(uri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    mProfileFragment.setBitmapImageView(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internetBroadcast = new InternetBroadcast(){

        };

        initUI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,
                R.string.nav_drawer_open,R.string.nav_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        replaceFragment(new SalesFragment());           //mới vô(sau khi login)thì cần load sale fragment lên
        mNavigationView.getMenu().findItem(R.id.nav_sales).setChecked(true); //set trong nav là SALES đang được chọn
        getSupportActionBar().setTitle("Sales");

        showUserInfo();

    }

    private void initUI(){
        mNavigationView = findViewById(R.id.nav_view);
        img_avatar = mNavigationView.getHeaderView(0).findViewById(R.id.img_avatar);
        tv_user_name = mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_name);
        tv_email = mNavigationView.getHeaderView(0).findViewById(R.id.tv_email);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_sales){
            if(frag_current != frag_sales){
                replaceFragment(new SalesFragment());
                frag_current = frag_sales;
                getSupportActionBar().setTitle("Sales");
            }
        }
        else if (id == R.id.nav_receipts){
            if(frag_current != frag_receipts){
                replaceFragment(new ReceiptsFragment());
                frag_current = frag_receipts;
                getSupportActionBar().setTitle("Receipts");
            }
        }
        else if (id == R.id.nav_shop){
            if(frag_current != frag_shop){
                replaceFragment(new ShopFragment());
                frag_current = frag_shop;
                getSupportActionBar().setTitle("Shop Management");
            }
        }
        else if (id == R.id.nav_settings){
            if(frag_current != frag_settings){
                replaceFragment(new SettingsFragment());
                frag_current = frag_settings;
                getSupportActionBar().setTitle("Theme");
            }
        }
        else if (id == R.id.nav_profile){
            if(frag_current != frag_profile){
                replaceFragment(mProfileFragment);
                frag_current = frag_profile;
                getSupportActionBar().setTitle("My Profile");
            }
        }
        else if (id == R.id.nav_change_password){
            if(frag_current != frag_change_password){
                replaceFragment(new ChangePasswordFragment());
                frag_current = frag_change_password;
                getSupportActionBar().setTitle("Change Password");
            }
        }
        else if (id == R.id.nav_logout){
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_yes_no);
            Window window = dialog.getWindow();
            if (window == null) {
                return false;
            }
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttribute = window.getAttributes();
            windowAttribute.gravity = Gravity.CENTER;
            window.setAttributes(windowAttribute);

            dialog.setCancelable(true);

            TextView title = dialog.findViewById(R.id.tv_dialog_yes_no);
            TextView descript = dialog.findViewById(R.id.dialog_yes_no_des);
            TextView btn_yes = dialog.findViewById(R.id.dialog_yes_btn);
            TextView btn_no = dialog.findViewById(R.id.dialog_no_btn);

            title.setText("Log Out");
            descript.setText("Are you sure want to logout?");

            btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
            dialog.show();

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else{
            if(frag_current == frag_sales){
                if(backPressedTime + 2000 > System.currentTimeMillis()){
                    backToast.cancel();
                    super.onBackPressed();
                    return;
                } else{
                    backToast = Toast.makeText(this, "Press back again to exit !", Toast.LENGTH_LONG);
                    backToast.show();
                }

                backPressedTime = System.currentTimeMillis();
            }else{
                replaceFragment(new SalesFragment());
                frag_current = frag_sales;
                getSupportActionBar().setTitle("Sales");
                mNavigationView.getMenu().getItem(0).setChecked(true);
            }
        }
    }

    public void showUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Check if user is null
        if (user == null){
            return;
        }
        //Parse user Information
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        if (name == null){
            tv_user_name.setVisibility(View.GONE);
        }else {
            tv_user_name.setVisibility(View.VISIBLE);
            tv_user_name.setText(name);
        }
        tv_email.setText(email);
        Glide.with(this).load(photoUrl).error(R.drawable.ic_ava_default).into(img_avatar);
    }


    //code để chuyển đổi fragment
    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery();
            }
        }
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select picture"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetBroadcast, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetBroadcast);
    }
}