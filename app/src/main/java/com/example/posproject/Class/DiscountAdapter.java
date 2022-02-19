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

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

    private List<Discount> listDiscount;
    private itemClickListener clickListener;

    public interface itemClickListener{
        void itemClickUpdate(Discount discount);
    }

    public DiscountAdapter(List<Discount> listDiscount, itemClickListener listener) {
        this.listDiscount = listDiscount;
        this.clickListener = listener;
    }

    public class DiscountViewHolder extends RecyclerView.ViewHolder{

        private TextView dcName;
        private TextView dcValue;
        private LinearLayout linearLayout;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            dcName = itemView.findViewById(R.id.item_discount_name);
            dcValue = itemView.findViewById(R.id.item_discount_value);
            linearLayout = itemView.findViewById(R.id.item_discount_layout);
        }
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discount,parent,false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        Discount discount = listDiscount.get(position);
        if (discount == null){
            return;
        }
        holder.dcName.setText( discount.getDiscountName());
        holder.dcValue.setText( discount.getDiscountValue() + "%");

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.itemClickUpdate(discount);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listDiscount != null){
            return  listDiscount.size();
        }
        return 0;
    }
}
