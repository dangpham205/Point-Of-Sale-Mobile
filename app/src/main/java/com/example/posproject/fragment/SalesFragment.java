package com.example.posproject.fragment;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.posproject.Class.Discount;
import com.example.posproject.Class.DiscountAdapter;
import com.example.posproject.Class.Product;
import com.example.posproject.Class.ProductAdapter;
import com.example.posproject.Class.Receipt;
import com.example.posproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SalesFragment extends Fragment {
    private static final int NOTI_ID = 1;
    private View mView;
    RecyclerView rcvProduct;
    ProductAdapter productAdapter;
    List<Product> productList;
    private RecyclerView rcvDiscount;
    private DiscountAdapter discountAdapter;
    private List<Discount> discountList;
    private String userEmail;
    private TextInputLayout dropMenu;
    private AutoCompleteTextView dropItems;
    private String sortKey;
    private String searchKey;
    private String searchKeyDiscount;
    private SearchView searchView;
    private TextView cartTotal;
    private LinearLayout cart;
    private String cartDescription;
    int discvalue = 0;
    int finalPrice= 0;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SalesFragment() {
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
    public static SalesFragment newInstance(String param1, String param2) {
        SalesFragment fragment = new SalesFragment();
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
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {     //phải có sethasoption menu trong oncreate
        inflater.inflate(R.menu.menu_sales, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_sales, container, false);

        initUI();
        sortSetting();
        searchSetting();
        getAllProducts(searchKey);
        return mView;
    }
    private void initUI(){
        searchKey = "";
        searchKeyDiscount = "";
        sortKey = "productName";
        rcvProduct = mView.findViewById(R.id.rcv_sales);
        searchView = mView.findViewById(R.id.search_sales);
        cart = mView.findViewById(R.id.cart);
        cartTotal = mView.findViewById(R.id.cart_total);
        cartDescription = "";

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCart();
            }
        });

        dropMenu = mView.findViewById(R.id.drop_menu_sale);
        dropItems = mView.findViewById(R.id.drop_items_sale);
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
                openQuantityDialog(product);
            }
        });
        rcvProduct.setAdapter(productAdapter);

    }

    private void openCart(){
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.cart_view);
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttribute = window.getAttributes();
        windowAttribute.gravity = Gravity.CENTER;
        window.setAttributes(windowAttribute);

        dialog.setCancelable(true);

        ScrollView selectedProductsScroll = dialog.findViewById(R.id.scroll);
        LinearLayout expand = dialog.findViewById(R.id.after_discount);
        TextView selectedProducts = dialog.findViewById(R.id.cart_selected);
        TextView discountvalue = dialog.findViewById(R.id.cart_discount);
        TextView price = dialog.findViewById(R.id.cart_price);
        TextView btn_yes = dialog.findViewById(R.id.cart_confirm);
        TextView btn_no = dialog.findViewById(R.id.cart_cancel);
        selectedProducts.setText(cartDescription);
        price.setText(cartTotal.getText().toString().trim());

        rcvDiscount = dialog.findViewById(R.id.cart_rcv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvDiscount.setLayoutManager(linearLayoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rcvDiscount.addItemDecoration(itemDecoration);
        discountList = new ArrayList<>();

        int currPrice = Integer.parseInt(price.getText().toString().trim());
        finalPrice = currPrice;
        discountAdapter = new DiscountAdapter(discountList, new DiscountAdapter.itemClickListener() {
            @Override
            public void itemClickUpdate(Discount discount) {
                if (currPrice == 0){
                    Toast.makeText(getContext(),"You can't have a discount yet.",Toast.LENGTH_SHORT).show();
                }
                else {
                    discvalue = discount.getDiscountValue();
                    finalPrice = currPrice - (currPrice * discvalue / 100);
                    discountvalue.setText(String.valueOf(discvalue));
                    price.setText(String.valueOf(finalPrice));
                    Toast.makeText(getActivity(),"Discount ticket "+ discount.getDiscountName()+" picked",Toast.LENGTH_SHORT);
                    rcvDiscount.setVisibility(View.GONE);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1000);
                    selectedProductsScroll.setLayoutParams(layoutParams);
                }
            }
        });
        rcvDiscount.setAdapter(discountAdapter);
        getAllDiscounts(searchKeyDiscount);

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });



        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currPrice == 0) {
                    Toast.makeText(getContext(),"You need to add some products first.",Toast.LENGTH_SHORT).show();
                }
                else {
                    openPayDialog(cartDescription,currPrice,discvalue,finalPrice);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openPayDialog(String content, int beforePrice, int discount, int finalPay) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bill_view);

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

        EditText customer = dialog.findViewById(R.id.customer_email);
        TextView btn_confirm = dialog.findViewById(R.id.bill_yes);
        TextView btn_cancel = dialog.findViewById(R.id.bill_no);
        Date date=java.util.Calendar.getInstance().getTime();
        String currentDate = String.valueOf(date);
        String emailBody = currentDate
                + "\nYour products:"
                + "\n"+content
                + "\n"
                + "\nCost (vnđ):          " + String.valueOf(beforePrice)
                + "\nDiscount (%):        "+ String.valueOf(discount)
                + "\nTotal price (vnđ):   "+ String.valueOf(finalPay)
                + "\n"
                + "\nPlease complete your payment so that we can ship our products!";
        String receiptContent =
                "Products:"
                +"\n"+content
                + "\n"
                + "\nCost (vnđ):     " + String.valueOf(beforePrice)
                + "\nDiscount (%):   "+ String.valueOf(discount);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(),"Sending",Toast.LENGTH_LONG).show();
                final String appEmail = "ipos10d@gmail.com";
                final String appPassword = "ipos10diem";
                Properties properties = new Properties();
                properties.put("mail.smtp.auth","true");
                properties.put("mail.smtp.starttls.enable","true");
                properties.put("mail.smtp.host","smtp.gmail.com");
                properties.put("mail.smtp.port","587");
                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(appEmail,appPassword);
                    }
                });
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(appEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(customer.getText().toString().trim()));
                    message.setSubject("Thank you for shopping");
                    message.setText(emailBody);
                    Transport.send(message);
                    Receipt receipt = new Receipt(currentDate,customer.getText().toString().trim(),finalPay,receiptContent);
                    AddReceipt(receipt);
                    dialog.dismiss();
                    TranslateAnimation animObj= new TranslateAnimation(cartTotal.getWidth(),0, 0, 0);
                    animObj.setDuration(2000);
                    cartTotal.startAnimation(animObj);
                    cartTotal.setText("0");
                    cartTotal.setTextColor(getResources().getColor(R.color.empty_cart));
                    cartDescription = "";
                    discvalue= 0;
                    finalPrice = 0;
                }
                catch (Exception e){
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void AddReceipt(Receipt receipt) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String path = userEmail + "/receipts";
        DatabaseReference myRef = database.getReference(path);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String keyReceipt = String.valueOf(receipt.getCurrTime());
                myRef.child(keyReceipt).setValue(receipt, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        Toast.makeText(getActivity(), "Succeed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Add receipt failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openQuantityDialog(Product product) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_quantity);

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

        FloatingActionButton btn_plus = dialog.findViewById(R.id.dialog_qtt_plus);
        FloatingActionButton btn_minus = dialog.findViewById(R.id.dialog_qtt_minus);
        TextView tv_quantity = dialog.findViewById(R.id.dialog_qtt);
        TextView btn_confirm = dialog.findViewById(R.id.dialog_qtt_yes);
        TextView btn_cancel = dialog.findViewById(R.id.dialog_qtt_no);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = Integer.parseInt(tv_quantity.getText().toString().trim());
                count = count + 1;
                tv_quantity.setText(String.valueOf(count));
            }
        });

        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count =Integer.parseInt(tv_quantity.getText().toString().trim());
                if ( count > 1){
                    count = count -1;
                    tv_quantity.setText(String.valueOf(count));
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                int old_price = Integer.parseInt(cartTotal.getText().toString().trim());
                int new_price = old_price + product.getProductValue()*Integer.parseInt(tv_quantity.getText().toString().trim());
                TranslateAnimation animObj= new TranslateAnimation(cartTotal.getWidth(),0, 0, 0);
                animObj.setDuration(2000);
                cartTotal.startAnimation(animObj);
                cartTotal.setText(String.valueOf(new_price));
                if (new_price>0){
                    cartTotal.setTextColor(getResources().getColor(R.color.yellow));
                }
                if (cartDescription.equals("")){
                    cartDescription+= "-"+"x"+ Integer.parseInt(tv_quantity.getText().toString().trim())+"   "+product.getProductName() + ":   " +String.valueOf(product.getProductValue()) + " vnđ";
                }
                else {
                    cartDescription+= "\n" + "-"+"x"+ Integer.parseInt(tv_quantity.getText().toString().trim())+"   "+product.getProductName() + ":   " +String.valueOf(product.getProductValue()) + " vnđ";
                }
            }
        });

        dialog.show();
    }

    private void sendNotification(String content){
        Notification notification = new Notification.Builder(getContext())
                .setContentTitle("Email send succeed")
                .setContentText(content)
                .setSmallIcon(R.drawable.notifi)
                .setColor(getResources().getColor(R.color.primary_color))
                .build();

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager != null){
            manager.notify(NOTI_ID,notification);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_sales_clear:
                final Dialog dialog = new Dialog(getActivity());
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

                title.setText("Clear Cart");
                descript.setText("Are you sure want to empty your cart?");

                btn_no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TranslateAnimation animObj= new TranslateAnimation(cartTotal.getWidth(),0, 0, 0);
                        animObj.setDuration(2000);
                        cartTotal.startAnimation(animObj);
                        cartTotal.setText("0");
                        cartTotal.setTextColor(getResources().getColor(R.color.empty_cart));
                        cartDescription = "";
                        dialog.dismiss();
                        discvalue= 0;
                        finalPrice = 0;
                    }
                });
                dialog.show();
                break;
        }
        return true;
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
                checkDuplicateDiscount();
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
                checkDuplicateDiscount();
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

    private void checkDuplicateDiscount(){
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
}