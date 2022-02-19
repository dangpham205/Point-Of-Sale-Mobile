package com.example.posproject.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.display.DeviceProductInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.posproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collection;

public class ChangePasswordFragment extends Fragment {
    private View mView;
    private EditText et_new_password, et_rePassword;
    private TextView btn_change_password;
    private ProgressDialog progressDialog;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SalesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangePasswordFragment newInstance(String param1, String param2) {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_change_password, container, false);

        initUI();

        btn_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_new_password.getText().toString().isEmpty() || et_rePassword.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Please enter new password",Toast.LENGTH_SHORT).show();
                }
                else if (et_new_password.getText().toString().equals(et_rePassword.getText().toString())){
                    onClickChangePassword();
                }else{
                    Toast.makeText(getActivity(), "Password do not match, retry !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return mView;
    }

    private void initUI(){
        progressDialog = new ProgressDialog(getActivity());
        et_new_password = mView.findViewById(R.id.et_new_password);
        et_rePassword = mView.findViewById(R.id.et_rePassword);
        btn_change_password = mView.findViewById(R.id.btn_change_password);
    }

    private void onClickChangePassword() {
        String strNewPassword = et_new_password.getText().toString().trim();
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updatePassword(strNewPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            openReAuthDialog();
                        }
                    }
                });
    }

    private void openReAuthDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reauthenticate);

        Window window = dialog.getWindow();
        if (window == null){
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = Gravity.CENTER;
        window.setAttributes(windowAttribute);

        dialog.setCancelable(true);

        EditText et_email = dialog.findViewById(R.id.et_email);
        EditText et_password = dialog.findViewById(R.id.et_password);
        TextView btn_confirm = dialog.findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = et_email.getText().toString().trim();
                String strPassword = et_password.getText().toString().trim();

                reAuthenticate(strEmail, strPassword);
                dialog.dismiss();
                et_email.setText("");
                et_password.setText("");
            }
        });

        dialog.show();
    }

    private void reAuthenticate(String email, String password){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            onClickChangePassword();
                        }else{
                            Toast.makeText(getActivity(), "Authenticate failed, try again !", Toast.LENGTH_SHORT).show();
                            openReAuthDialog();
                        }
                    }
                });
    }

}