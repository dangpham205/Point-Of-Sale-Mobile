package com.example.posproject.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.posproject.Class.Product;
import com.example.posproject.Class.ProductAdapter;
import com.example.posproject.Class.Receipt;
import com.example.posproject.Class.ReceiptAdapter;
import com.example.posproject.R;
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
 * Use the {@link ReceiptsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiptsFragment extends Fragment {
    private View mView;
    RecyclerView rcvReceipt;
    ReceiptAdapter receiptAdapter;
    List<Receipt> receiptList;
    private String userEmail;
    private String sortKey;
    private String searchKey;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReceiptsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReceiptsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReceiptsFragment newInstance(String param1, String param2) {
        ReceiptsFragment fragment = new ReceiptsFragment();
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
        mView = inflater.inflate(R.layout.fragment_receipts, container, false);

        initUI();
        getAllReceipts(searchKey);
        return mView;
    }

    private void initUI(){
        searchKey = "";
        sortKey = "currTime";
        rcvReceipt = mView.findViewById(R.id.rcv_receipts);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = user.getEmail();
        int temp = userEmail.lastIndexOf("@");      //cắt chuỗi email chỉ lấy đằng trc @
        userEmail = userEmail.substring(0,temp);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvReceipt.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rcvReceipt.addItemDecoration(itemDecoration);

        receiptList = new ArrayList<>();
        receiptAdapter = new ReceiptAdapter(receiptList, new ReceiptAdapter.itemClickListener() {
            @Override
            public void itemClickUpdate(Receipt receipt) {
                openReceiptDialog(receipt);
            }
        });
        rcvReceipt.setAdapter(receiptAdapter);

        new ItemTouchHelper(itemTouch).attachToRecyclerView(rcvReceipt);
    }



    ItemTouchHelper.SimpleCallback itemTouch = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getPosition();
            Receipt receipt = receiptList.get(position);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(userEmail+"/receipts");

            myRef.child(String.valueOf(receipt.getCurrTime())).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                    Snackbar.make(getView(), "Delete Succeed", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    myRef.child(String.valueOf(receipt.getCurrTime())).setValue(receipt, new DatabaseReference.CompletionListener() {
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
    };


    private void openReceiptDialog(Receipt receipt) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_receipt);

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

        TextView time = dialog.findViewById(R.id.customer_bought_time);
        TextView email = dialog.findViewById(R.id.customer_info);
        TextView bought = dialog.findViewById(R.id.customer_bought);
        TextView paid = dialog.findViewById(R.id.customer_total);
        TextView btn_confirm = dialog.findViewById(R.id.receipt_ok);

        time.setText(receipt.getCurrTime());
        email.setText(receipt.getCustomerInfo());
        bought.setText(receipt.getCustomerCart());
        paid.setText(String.valueOf(receipt.getCustomerTotal()));

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void getAllReceipts(String searchkey){
        receiptList.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(userEmail+"/receipts");
        Query query = myRef.orderByChild(sortKey);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Receipt receipt = snapshot.getValue(Receipt.class);
                if (receipt != null){
                    if(receipt.getCurrTime().contains(searchkey.toLowerCase(Locale.ROOT)) || receipt.getCurrTime().contains(searchkey.toUpperCase(Locale.ROOT))) {
                        receiptList.add(receipt);
                    }
                }
                for (int i = 0; i < receiptList.size()-1; i++){
                    if(receiptList.get(i).getCurrTime().equals(receiptList.get(i+1).getCurrTime())){
                        receiptList.remove(receiptList.get(i+1));
                    }
                }
                checkDuplicate();
                receiptAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Receipt receipt = snapshot.getValue(Receipt.class);
                if (receiptList == null  || receiptList.isEmpty() || receipt == null){
                    return;
                }
                for (int i =0; i < receiptList.size(); i++){
                    if (receipt.getCurrTime() == receiptList.get(i).getCurrTime()){
                        if(receipt.getCurrTime().contains(searchkey.toLowerCase(Locale.ROOT)) || receipt.getCurrTime().contains(searchkey.toUpperCase(Locale.ROOT))){
                            receiptList.set(i,receipt);
                            break;
                        }
                    }
                }
                checkDuplicate();
                receiptAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Receipt receipt = snapshot.getValue(Receipt.class);
                if (receiptList == null  || receiptList.isEmpty() || receipt == null){
                    return;
                }
                for (int i =0; i < receiptList.size(); i++){
                    if (receipt.getCurrTime().equals(receiptList.get(i).getCurrTime())){
                        receiptList.remove(receiptList.get(i));
                        break;
                    }
                }
                receiptAdapter.notifyDataSetChanged();
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
        if(receiptList.size()>1){
            for (int i =0; i< receiptList.size()-1; i++){
                for (int t = i+1; t< receiptList.size();t++){
                    if(receiptList.get(i).getCurrTime().equals(receiptList.get(t).getCurrTime())){
                        receiptList.remove(receiptList.get(t));
                    }
                }
            }
            receiptAdapter.notifyDataSetChanged();
        }
    }
}