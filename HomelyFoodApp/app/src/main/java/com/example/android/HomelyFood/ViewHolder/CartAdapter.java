package com.example.android.HomelyFood.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.android.HomelyFood.Cart;
import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Database.Database;
import com.example.android.HomelyFood.Interface.ItemClickListener;
import com.example.android.HomelyFood.Model.Order;
import com.example.android.HomelyFood.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener{

    TextView txt_cart_name,txt_cart_price;
    ElegantNumberButton btn_quantity;
    ImageView img_cart;

    private ItemClickListener itemClickListener;

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_cart_name=itemView.findViewById(R.id.cart_item_name);
        txt_cart_price=itemView.findViewById(R.id.cart_item_price);

        btn_quantity=itemView.findViewById(R.id.btn_quantity);
        img_cart=itemView.findViewById(R.id.cart_image);

        itemView.setOnCreateContextMenuListener(this);

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);
    }

}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order> listData=new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(cart);
        View itemView=inflater.inflate(R.layout.cart_layout,viewGroup,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CartViewHolder cartViewHolder, final int i) {
//        TextDrawable drawable= TextDrawable.builder().buildRound(""+listData.get(i).getQuantity(), Color.RED);
//        cartViewHolder.img_cart_count.setImageDrawable(drawable);

        Picasso.with(cart.getBaseContext()).load(listData.get(i).getImage()).resize(60,60).centerCrop().into(cartViewHolder.img_cart);

        cartViewHolder.btn_quantity.setNumber(listData.get(i).getQuantity());
        cartViewHolder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order=listData.get(i);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //calculate total price
                int total=0;
                List<Order> orders=new Database(cart).getCarts();
                for (Order item:orders){
                    total+=(Integer.parseInt(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                }
                Locale locale=new Locale("en","IN");
                NumberFormat numberFormat= NumberFormat.getCurrencyInstance(locale);
                cart.txtTotalPrice.setText(numberFormat.format(total));


                int price=(Integer.parseInt(listData.get(i).getPrice()))*(Integer.parseInt(listData.get(i).getQuantity()));
                cartViewHolder.txt_cart_price.setText(numberFormat.format(price));
            }
        });
        Locale locale=new Locale("en","IN");
        NumberFormat numberFormat= NumberFormat.getCurrencyInstance(locale);
        int price=(Integer.parseInt(listData.get(i).getPrice()))*(Integer.parseInt(listData.get(i).getQuantity()));
        cartViewHolder.txt_cart_price.setText(numberFormat.format(price));
        cartViewHolder.txt_cart_name.setText(listData.get(i).getProductName());


    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


}
