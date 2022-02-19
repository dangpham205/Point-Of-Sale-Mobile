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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> listProduct;
    private ProductAdapter.itemClickListener clickListener;

    public interface itemClickListener{
        void itemClickUpdate(Product product);
    }

    public ProductAdapter(List<Product> listProduct, itemClickListener listener) {
        this.listProduct = listProduct;
        this.clickListener = listener;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder{

        private TextView productName;
        private TextView productValue;
        private LinearLayout linearLayout;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.item_product_name);
            productValue = itemView.findViewById(R.id.item_product_value);
            linearLayout = itemView.findViewById(R.id.item_product_layout);
        }
    }

    @NonNull
    @Override
    public ProductAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProductViewHolder holder, int position) {
        Product product = listProduct.get(position);
        if (product == null){
            return;
        }
        holder.productName.setText(product.getProductName());
        holder.productValue.setText(product.getProductValue() + " VND");

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.itemClickUpdate(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listProduct != null){
            return  listProduct.size();
        }
        return 0;
    }

}
