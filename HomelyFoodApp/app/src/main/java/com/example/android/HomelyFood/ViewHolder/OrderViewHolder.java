package com.example.android.HomelyFood.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.android.HomelyFood.Interface.ItemClickListener;
import com.example.android.HomelyFood.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId,txtOrderStatus,txtOrderaddress,txtOrderPhone;

    public ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtOrderId=itemView.findViewById(R.id.order_id);
        txtOrderaddress=itemView.findViewById(R.id.order_address);
        txtOrderPhone=itemView.findViewById(R.id.order_phone);
        txtOrderStatus=itemView.findViewById(R.id.order_status);

        itemView.setOnClickListener(this);


    }
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
