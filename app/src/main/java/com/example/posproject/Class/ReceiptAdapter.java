package com.example.posproject.Class;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.posproject.R;

import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>{
    private List<Receipt> listReceipt;
    private itemClickListener clickListener;

    public ReceiptAdapter() {
    }
    public interface itemClickListener{
        void itemClickUpdate(Receipt receipt);
    }

    public ReceiptAdapter(List<Receipt> listReceipt, itemClickListener listener) {
        this.listReceipt = listReceipt;
        this.clickListener = listener;
    }

    public class ReceiptViewHolder extends RecyclerView.ViewHolder{

        private TextView rcTime;
        private TextView cusInfo;
        private TextView cusTotal;
        private LinearLayout linearLayout;

        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);
            rcTime = itemView.findViewById(R.id.item_receipt_time);
            cusInfo = itemView.findViewById(R.id.item_receipt_customer);
            cusTotal = itemView.findViewById(R.id.item_receipt_total);
            linearLayout = itemView.findViewById(R.id.item_receipt_layout);
        }
    }

    @NonNull
    @Override
    public ReceiptAdapter.ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receipt,parent,false);
        return new ReceiptAdapter.ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptAdapter.ReceiptViewHolder holder, int position) {
        Receipt receipt = listReceipt.get(position);
        if (receipt == null){
            return;
        }
        holder.rcTime.setText(receipt.getCurrTime());
        holder.cusInfo.setText( receipt.getCustomerInfo());
        holder.cusTotal.setText(receipt.getCustomerTotal()+ " vnđ");

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.itemClickUpdate(receipt);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listReceipt != null){
            return  listReceipt.size();
        }
        return 0;
    }

}
