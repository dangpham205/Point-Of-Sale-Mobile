package com.example.posproject.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.posproject.Class.Discount;
import com.example.posproject.Class.Product;
import com.example.posproject.R;
import com.example.posproject.fragment.Shop.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private boolean isOpen = false;
    private Button btnAddDiscount, btnAddItem;
    private View mView;
    private String userEmail;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ShopFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShopFragment newInstance(String param1, String param2) {
        ShopFragment fragment = new ShopFragment();
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
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_shop, container, false);
        initUI();
        btnAddDiscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //đừng qtam
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.shop_layout,new Shop_Discount_View());
//                transaction.commit();
                floatBtnOff();
                openDiscountDialog();
            }
        });


        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatBtnOff();
                openItemDialog();
            }
        });

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tabLayout = getView().findViewById(R.id.shop_tab_layout);
        viewPager = getView().findViewById(R.id.shop_view_pager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        setTabDividers();

        floatBtnSetting();
    }
    private void initUI(){
        btnAddDiscount = mView.findViewById(R.id.fab_add_categories);
        btnAddItem = mView.findViewById(R.id.fab_add_items);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        int temp = userEmail.lastIndexOf("@");      //cắt chuỗi email chỉ lấy đằng trc @
        userEmail = userEmail.substring(0,temp);
    }


    private void setTabDividers(){
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout){
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.parseColor("#a0a0a0"));
            drawable.setSize(5,2);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);

        }
    }

    private void floatBtnSetting(){
        getActivity().findViewById(R.id.fab_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen){
                    getActivity().findViewById(R.id.fab_add_items).setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close));
                    getActivity().findViewById(R.id.fab_add_categories).setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close));
                    getActivity().findViewById(R.id.fab_add_discounts).setVisibility(View.INVISIBLE);
                    isOpen = false;
                }else {
                    getActivity().findViewById(R.id.fab_add_items).setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_open));
                    getActivity().findViewById(R.id.fab_add_categories).setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_open));
                    getActivity().findViewById(R.id.fab_add_discounts).setVisibility(View.VISIBLE);
                    isOpen = true;
                }
            }
        });
    }

    private void floatBtnOff(){
        getActivity().findViewById(R.id.fab_add_items).setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close));
        getActivity().findViewById(R.id.fab_add_categories).setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close));
        getActivity().findViewById(R.id.fab_add_discounts).setVisibility(View.INVISIBLE);
        isOpen = false;
    }

    private void openDiscountDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_discount);

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

        EditText et_discount_name = dialog.findViewById(R.id.et_discount_name);
        EditText et_discount_prop = dialog.findViewById(R.id.et_discount_prop);
        TextView btn_confirm = dialog.findViewById(R.id.btn_confirm);
        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strDiscName = et_discount_name.getText().toString().trim();
                String strDiscValue = et_discount_prop.getText().toString().trim();

                if (strDiscName.isEmpty() || strDiscValue.isEmpty()){
                    Toast.makeText(getActivity(),"Please type in all the information",Toast.LENGTH_SHORT).show();
                }
                else if (isStringInt(strDiscValue) == false){
                    Toast.makeText(getActivity(),"Please type the right value",Toast.LENGTH_SHORT).show();
                }
                else if (1==1){
                    int intDiscValue = Integer.parseInt(strDiscValue);
                    if (intDiscValue >100){
                        Toast.makeText(getActivity(),"Biggest discount is 100% only :(",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Discount discount = new Discount(strDiscName,intDiscValue);
                        AddDiscount(discount);
                        dialog.dismiss();
                    }
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void AddDiscount(Discount discount) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String path = userEmail + "/discounts";
        DatabaseReference myRef = database.getReference(path);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(discount.getDiscountName())) {
                    Toast.makeText(getActivity(),"There is already a discount called: "+discount.getDiscountName(),Toast.LENGTH_SHORT).show();                }
                else{
                    String keyDiscount = String.valueOf(discount.getDiscountName());
                    myRef.child(keyDiscount).setValue(discount, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            Toast.makeText(getActivity(), "Succeed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Add discount ticket failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openItemDialog(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_item);

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

        EditText et_item_name = dialog.findViewById(R.id.et_item_name);
        EditText et_item_price = dialog.findViewById(R.id.et_item_price);
        TextView btn_confirm = dialog.findViewById(R.id.btn_confirm_product);
        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel_product);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strItemName = et_item_name.getText().toString().trim();
                String strItemValue = et_item_price.getText().toString().trim();

                if (strItemName.isEmpty() || strItemValue.isEmpty()){
                    Toast.makeText(getActivity(),"Please type in all the information",Toast.LENGTH_SHORT).show();
                }
                else if (isStringInt(strItemValue) == false){
                    Toast.makeText(getActivity(),"Please type the right price",Toast.LENGTH_SHORT).show();
                }
                else if (1==1){
                    int intItemValue = Integer.parseInt(strItemValue);
                    if (intItemValue<=0){
                        Toast.makeText(getActivity(),"Please type the right price",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Product product = new Product(strItemName, intItemValue);
                        AddItem(product);
                        dialog.dismiss();
                    }
                }
                else {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }


    private void AddItem(Product product) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String path = userEmail + "/products";
        DatabaseReference myRef = database.getReference(path);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(product.getProductName())) {
                    Toast.makeText(getActivity(),"There is already a product called: "+product.getProductName(),Toast.LENGTH_SHORT).show();                }
                else{
                    String keyProduct = String.valueOf(product.getProductName());
                    myRef.child(keyProduct).setValue(product, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            Toast.makeText(getActivity(), "Succeed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Add product failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public boolean isStringInt(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex)
        {
            return false;
        }
    }
}