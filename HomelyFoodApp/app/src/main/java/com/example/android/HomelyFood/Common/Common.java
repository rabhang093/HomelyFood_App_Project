package com.example.android.HomelyFood.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.android.HomelyFood.Model.User;
import com.example.android.HomelyFood.Remote.IGoogleService;
import com.example.android.HomelyFood.Remote.RetrofitGoogleApi;

public class Common {

    public static User currentUser;
    public static final String UPDATE="Update";
    public static final String DELETE="Delete";

    public static final String USR_KEY="User";
    public static final String PWD_KEY="Password";

    public static final String INTENT_FOOD_ID="FoodId";

    public static final int PICK_IMAGE_REQUEST =71;

    public static final String GOOGLE_API_URL="https://maps.googleapis.com/";

    public static IGoogleService getGoogleMapApi(){
        return RetrofitGoogleApi.getClient(GOOGLE_API_URL).create(IGoogleService.class);
    }

    public static String convertCodeToStatus(String code) {
        String status="";
        if(code.equals("0"))
            status="Placed";
        else if(code.equals("1"))
            status="On Your Way";
        else if(code.equals("2"))
            status="Shipped";
        return status;
    }
    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null){
            NetworkInfo[] info=connectivityManager.getAllNetworkInfo();
            if(info!=null){
                for (int i=0;i<info.length;i++){
                    if(info[i].getState()==NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
