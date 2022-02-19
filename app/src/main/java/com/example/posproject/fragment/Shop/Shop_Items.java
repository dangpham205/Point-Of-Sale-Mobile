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

import com.example.posproject.Class.Product;
import com.example.posproject.Class.ProductAdapter;
import com.example.posproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
 * Use the {@link Shop_Items#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Shop_Items extends Fragment {
    private View mView;
    RecyclerView rcvProduct;
    ProductAdapter productAdapter;
    List<Product> productList;
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

    FloatingActionButton btn_add;

    public Shop_Items() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Shop_Items.
     */
    // TODO: Rename and change types and number of parameters
    public static Shop_Items newInstance(String param1, String param2) {
        Shop_Items fragment = new Shop_Items();
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
        mView = inflater.inflate(R.layout.fragment_shop__items, container, false);

        initUI();
        sortSetting();
        searchSetting();
        getAllProducts(searchKey);
        return mView;
    }
    private void initUI(){
        searchKey = "";
        sortKey = "productName";
        rcvProduct = mView.findViewById(R.id.rcv_products);
        searchView = mView.findViewById(R.id.search_product);

        dropMenu = mView.findViewById(R.id.drop_menu_product);
        dropItems = mView.findViewById(R.id.drop_items_product);
        String [] dropList = {"Name", "Price"};
        ArrayAdapter<String> dropAdapter = new ArrayAdapter<>(getActivity(),R.layout.item_dropdown, dropList);
        dropItems.setAdapter(dropAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        int temp = userEmail.lastIndexOf("@");      //cắt chuỗi email chỉ lấy đằng trc @
        userEmail = userEmail.substring(0,temp);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvProduct.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rcvProduct.addItemDecoration(itemDecoration);

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, new ProductAdapter.itemClickListener() {
            @Override
            public void itemClickUpdate(Product product) {
                openProductDialog(product);
            }
        });
        rcvProduct.setAdapter(productAdapter);
    }

    private void sortSetting(){
        dropItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = String.valueOf(adapterView.getItemAtPosition(i));
                if (selected.equals("Name")){
                    sortKey = "productName";
                }
                else if(selected.equals("Price")){
                    sortKey = "productValue";
                }
                getAllProducts(searchKey);
                rcvProduct.invalidate();
            }
        });
    }

    private void searchSetting(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!s.isEmpty()){
                    searchKey = String.valueOf(s);
                    getAllProducts(searchKey);
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
                getAllProducts(searchKey);
                return false;
            }
        });
    }

    private void getAllProducts(String searchkey){
        productList.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(userEmail+"/products");
        Query query = myRef.orderByChild(sortKey);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Product product = snapshot.getValue(Product.class);
                if (product != null){
                    if(product.getProductName().contains(searchkey.toLowerCase(Locale.ROOT)) || product.getProductName().contains(searchkey.toUpperCase(Locale.ROOT))) {
                        productList.add(product);
                    }
                }
                for (int i = 0; i < productList.size()-1; i++){
                    if(productList.get(i).getProductName().equals(productList.get(i+1).getProductName())){
                        productList.remove(productList.get(i+1));
                    }
                }
                checkDuplicate();
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Product product = snapshot.getValue(Product.class);
                if (productList == null  || productList.isEmpty() || product == null){
                    return;
                }
                for (int i =0; i < productList.size(); i++){
                    if (product.getProductName() == productList.get(i).getProductName()){
                        if(product.getProductName().contains(searchkey.toLowerCase(Locale.ROOT)) || product.getProductName().contains(searchkey.toUpperCase(Locale.ROOT))){
                            productList.set(i,product);
                            break;
                        }
                    }
                }
                checkDuplicate();
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (productList == null  || productList.isEmpty() || product == null){
                    return;
                }
                for (int i =0; i < productList.size(); i++){
                    if (product.getProductName().equals(productList.get(i).getProductName())){
                        productList.remove(productList.get(i));
                        break;
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    private void openProductDialog(Product product) {
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

        TextView tv_dialog_product = dialog.findViewById(R.id.tv_dialog_product);
        EditText et_product_name = dialog.findViewById(R.id.et_item_name);
        EditText et_product_value = dialog.findViewById(R.id.et_item_price);
        TextView btn_confirm = dialog.findViewById(R.id.btn_confirm_product);
        TextView btn_delete = dialog.findViewById(R.id.btn_cancel_product);

        tv_dialog_product.setText("Update Product");
        btn_delete.setText("Delete");
        et_product_name.setText(product.getProductName());
        et_product_value.setText(String.valueOf(product.getProductValue()));
        String oldName = et_product_name.getText().toString().trim();
        int oldValue = Integer.parseInt(et_product_value.getText().toString().trim());

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Product product = new Product(et_product_name.getText().toString().trim(),Integer.parseInt(et_product_value.getText().toString().trim()));
                DeleteProduct(product);
                dialog.dismiss();
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String path = userEmail + "/products";
                DatabaseReference myRef = database.getReference(path);

                int newValue = 0;
                String newName = et_product_name.getText().toString().trim();
                String newStringValue = et_product_value.getText().toString().trim();
                if(isStringInt(newStringValue)){
                    newValue =Integer.parseInt(newStringValue);
                }

                if (newName.isEmpty() || newStringValue.isEmpty() || newValue == 0){
                    Toast.makeText(getActivity(),"Please type in all the information",Toast.LENGTH_SHORT).show();
                }
                else if(1==1){
                    if(newName.equals(oldName)){
                        product.setProductName(newName);
                        product.setProductValue(newValue);
                        myRef.child(String.valueOf(product.getProductName())).updateChildren(product.toMap(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                Toast.makeText(getActivity(),"Update Succeed",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                    else{       //nếu update đổi tên disc thì phải xóa disc cũ, add disc mới vô
                        Product newProduct = new Product(newName,newValue);

                        myRef.child(String.valueOf(oldName)).removeValue();     //xóa thằng cũ

                        myRef.child(String.valueOf(newName)).setValue(newProduct, new DatabaseReference.CompletionListener() {
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

    private void DeleteProduct(Product product) {
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
                DatabaseReference myRef = database.getReference(userEmail+"/products");

                myRef.child(String.valueOf(product.getProductName())).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        dialog.dismiss();
                        //khi xóa thì object sẽ null, mà trong getobject có toString, sẽ bị lỗi do toString không chạy đc, nên đặt try
                        Snackbar.make(getView(), "Delete Succeed", Snackbar.LENGTH_LONG)
                                .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        myRef.child(String.valueOf(product.getProductName())).setValue(product, new DatabaseReference.CompletionListener() {
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
        if(productList.size()>1){
            for (int i =0; i< productList.size()-1; i++){
                for (int t = i+1; t< productList.size();t++){
                    if(productList.get(i).getProductName().equals(productList.get(t).getProductName())){
                        productList.remove(productList.get(t));
                    }
                }
            }
            productAdapter.notifyDataSetChanged();
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