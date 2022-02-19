package com.example.posproject.fragment.Shop;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.posproject.Class.Discount;
import com.example.posproject.Class.DiscountAdapter;
import com.example.posproject.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Shop_Discounts#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Shop_Discounts extends Fragment {
    private View mView;
    private RecyclerView rcvDiscount;
    private DiscountAdapter discountAdapter;
    private List<Discount> discountList;
    private String userEmail;
    private TextInputLayout dropMenu;
    private AutoCompleteTextView dropItems;
    private String sortKey;
    private String searchKey;
    private SearchView searchView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Shop_Discounts() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Shop_Discounts.
     */
    // TODO: Rename and change types and number of parameters
    public static Shop_Discounts newInstance(String param1, String param2) {
        Shop_Discounts fragment = new Shop_Discounts();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_shop__discounts, container, false);

        initUI();
        sortSetting();
        searchSetting();
        getAllDiscounts(searchKey);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    }

    private void initUI(){
        searchKey = "";
        sortKey = "discountName";
        rcvDiscount = mView.findViewById(R.id.rcv_discounts);
        searchView = mView.findViewById(R.id.search_discount);

        dropMenu = mView.findViewById(R.id.drop_menu);
        dropItems = mView.findViewById(R.id.drop_items);
        String [] dropList = {"Name", "Value"};
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<>(getActivity(),R.layout.item_dropdown, dropList);
        dropItems.setAdapter(dropAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        int temp = userEmail.lastIndexOf("@");      //cắt chuỗi email chỉ lấy đằng trc @
        userEmail = userEmail.substring(0,temp);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvDiscount.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rcvDiscount.addItemDecoration(itemDecoration);

        discountList = new ArrayList<>();
        discountAdapter = new DiscountAdapter(discountList, new DiscountAdapter.itemClickListener() {
            @Override
            public void itemClickUpdate(Discount discount) {
                openDiscountDialog(discount);
            }
        });
        rcvDiscount.setAdapter(discountAdapter);
    }

    private void sortSetting(){
        dropItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = String.valueOf(adapterView.getItemAtPosition(i));
                if (selected.equals("Name")){
                    sortKey = "discountName";
                }
                else if(selected.equals("Value")){
                    sortKey = "discountValue";
                }
                getAllDiscounts(searchKey);
                rcvDiscount.invalidate();
            }
        });
    }

    private void searchSetting(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!s.isEmpty()){
                    searchKey = String.valueOf(s);
                    getAllDiscounts(searchKey);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                if(s.isEmpty()){
                    searchKey = "";
                }
                else{
                    searchKey = String.valueOf(s);
                }
                getAllDiscounts(searchKey);
                return false;
            }
        });
    }

    private void getAllDiscounts(String searchkey){
        discountList.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(userEmail+"/discounts");
        Query query = myRef.orderByChild(sortKey);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Discount discount = snapshot.getValue(Discount.class);
                if (discount != null){
                    if(discount.getDiscountName().contains(searchkey.toLowerCase(Locale.ROOT)) ||discount.getDiscountName().contains(searchkey.toUpperCase(Locale.ROOT))){
                        discountList.add(discount);
                    }
                }
                for (int i = 0; i < discountList.size()-1; i++){
                    if(discountList.get(i).getDiscountName().equals(discountList.get(i+1).getDiscountName())){
                        discountList.remove(discountList.get(i+1));
                    }
                }
                checkDuplicate();
                discountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Discount discount = snapshot.getValue(Discount.class);
                if (discountList == null  || discountList.isEmpty() || discount == null){
                    return;
                }
                for (int i =0; i < discountList.size(); i++){
                    if (discount.getDiscountName() == discountList.get(i).getDiscountName()){
                        if(discount.getDiscountName().contains(searchkey.toLowerCase(Locale.ROOT)) ||discount.getDiscountName().contains(searchkey.toUpperCase(Locale.ROOT))){
                            discountList.set(i,discount);
                            break;
                        }
                    }
                }
                checkDuplicate();
                discountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Discount discount = snapshot.getValue(Discount.class);
                if (discountList == null  || discountList.isEmpty() || discount == null){
                    return;
                }
                for (int i =0; i < discountList.size(); i++){
                    if (discount.getDiscountName().equals(discountList.get(i).getDiscountName())){
                        discountList.remove(discountList.get(i));
                        break;
                    }
                }
                discountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void openDiscountDialog(Discount discount) {
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

        TextView tv_dialog_discount = dialog.findViewById(R.id.tv_dialog_discount);
        EditText et_discount_name = dialog.findViewById(R.id.et_discount_name);
        EditText et_discount_value = dialog.findViewById(R.id.et_discount_prop);
        TextView btn_confirm = dialog.findViewById(R.id.btn_confirm);
        TextView btn_delete = dialog.findViewById(R.id.btn_cancel);

        tv_dialog_discount.setText("Update Discount");
        btn_delete.setText("Delete");
        et_discount_name.setText(discount.getDiscountName());
        et_discount_value.setText(String.valueOf(discount.getDiscountValue()));
        String oldName = et_discount_name.getText().toString().trim();
        int oldValue = Integer.parseInt(et_discount_value.getText().toString().trim());

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Discount discount = new Discount(et_discount_name.getText().toString().trim(),Integer.parseInt(et_discount_value.getText().toString().trim()));
                DeleteDiscount(discount);
                dialog.dismiss();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String path = userEmail + "/discounts";
                DatabaseReference myRef = database.getReference(path);

                int newValue = 0;
                String newName = et_discount_name.getText().toString().trim();
                String newStringValue = et_discount_value.getText().toString().trim();
                if (isStringInt(newStringValue)){
                    newValue =Integer.parseInt(newStringValue);
                }


                if (newName.isEmpty() || newStringValue.isEmpty() || newValue == 0){
                    Toast.makeText(getActivity(),"Please type in all the information",Toast.LENGTH_SHORT).show();
                }
                else if (newValue>100){
                    Toast.makeText(getActivity(),"Biggest discount is 100% only :(",Toast.LENGTH_SHORT).show();
                }
                else if(1==1){
                    if(newName.equals(oldName)){
                        discount.setDiscountName(newName);
                        discount.setDiscountValue(newValue);
                        myRef.child(String.valueOf(discount.getDiscountName())).updateChildren(discount.toMap(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(getActivity(),"Update Succeed",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                    else{       //nếu update đổi tên disc thì phải xóa disc cũ, add disc mới vô
                        Discount newDiscount = new Discount(newName,newValue);

                        myRef.child(String.valueOf(oldName)).removeValue();     //xóa thằng cũ

                        myRef.child(String.valueOf(newName)).setValue(newDiscount, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(getActivity(), "Succeed", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });

                    }
                }
            }
        });
        dialog.show();
    }

    private void DeleteDiscount(Discount discount) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_yes_no);

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

        TextView btn_yes = dialog.findViewById(R.id.dialog_yes_btn);
        TextView btn_no = dialog.findViewById(R.id.dialog_no_btn);

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference(userEmail+"/discounts");

                myRef.child(String.valueOf(discount.getDiscountName())).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        dialog.dismiss();
                        //khi xóa thì object sẽ null, mà trong getobject có toString, sẽ bị lỗi do toString không chạy đc, nên đặt try
                        Snackbar.make(getView(), "Delete Succeed", Snackbar.LENGTH_LONG)
                                .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myRef.child(String.valueOf(discount.getDiscountName())).setValue(discount, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                Toast.makeText(getActivity(), "Undo Succeed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .show();
                    }
                });
            }
        });

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void checkDuplicate(){

        if(discountList.size()>1){
            for (int i =0; i< discountList.size()-1; i++){
                for (int t = i+1; t< discountList.size();t++){
                    if(discountList.get(i).getDiscountName().equals(discountList.get(t).getDiscountName())){
                        discountList.remove(discountList.get(t));
                    }
                }
            }
            discountAdapter.notifyDataSetChanged();
        }
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