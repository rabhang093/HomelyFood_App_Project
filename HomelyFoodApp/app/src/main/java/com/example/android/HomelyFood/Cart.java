package com.example.android.HomelyFood;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.HomelyFood.Common.Common;
import com.example.android.HomelyFood.Common.Config;
import com.example.android.HomelyFood.Database.Database;
import com.example.android.HomelyFood.Model.Order;
import com.example.android.HomelyFood.Model.Request;
import com.example.android.HomelyFood.Remote.IGoogleService;
import com.example.android.HomelyFood.ViewHolder.CartAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICES_REQUEST = 9997;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlaceOrder;

    List<Order> cart= new ArrayList<>();
    CartAdapter adapter;

    //Declare google map api retrofit
    IGoogleService mGoogleMapService;

//    PlacesClient placesClient;
//    Place shippingAddress;
//    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

    private static final int PAYPAL_REQUEST_CODE=9999;

    static PayPalConfiguration config= new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId(Config.PAYPAL_CLIENT_ID);//sandbox for testing purpose later we change
    String address,comment;

    //location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL=5000;
    private static final int FASTEST_INTERVAL=3000;
    private static final int DISPLACEMENT=10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //init GoogleMapApi
        mGoogleMapService=Common.getGoogleMapApi();

        //firebase init
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        //init paypal
        Intent intent=new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);


        //init
        recyclerView=findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice=findViewById(R.id.total);
        btnPlaceOrder=findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(Cart.this, "order placed", Toast.LENGTH_SHORT).show();
                if(cart.size()>0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty!!!", Toast.LENGTH_SHORT).show();

            }
        });

        if(Common.isConnectedToInternet(this))
            loadListFood();
        else
            Toast.makeText(this, "Please check your Internet connection!!!", Toast.LENGTH_SHORT).show();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },LOCATION_REQUEST_CODE);
        }else{
            if(checkPlayServices()){
                buildGoogleAPIClient();
                createLocationRequest();
            }
        }
    }
    private void createLocationRequest() {

        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleAPIClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!= ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_REQUEST).show();
            }else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Only one more step!!");
        alertDialog.setMessage("Enter Your Address:     ");

        final LayoutInflater inflater=this.getLayoutInflater();
        View order_address_comment =inflater.inflate(R.layout.order_address_comment,null);
//        PlaceAutocompleteFragment edtAddress= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//        //Hide search icon before fragment appears
//        edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);
//        //Set hint for autocomplete text
//        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
//                .setHint("Enter your address");
//        //set text size
//        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
//                .setTextSize(14);
//        //Get address from place autocomplete
//        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                shippingAddress=place;
//            }
//
//            @Override
//            public void onError(Status status) {
//                Log.e("ERROR",status.getStatusMessage());
//            }
//        });

//        Places.initialize(this,getString(R.string.google_place_api));
//        placesClient = Places.createClient(this);
//
//        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//        //hide search icon before fragment
//        autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);
//        //set hint for autocomplete EditText
//        ((EditText)autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input)).setHint("Enter your Address");
//        //set Text size
//        ((EditText)autocompleteSupportFragment.getView().findViewById(R.id.places_autocomplete_search_input)).setTextSize(14);
//        autocompleteSupportFragment.setPlaceFields(placeFields);
//        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override public void onPlaceSelected(@NonNull Place place) {
//                   // shippingAddress = place;
//                    address=place.getAddress();
//            }
//            @Override public void onError(@NonNull Status status) {
//                Toast.makeText(Cart.this,""+status.getStatusMessage(),Toast.LENGTH_LONG).show();
//            }
//        });
        final MaterialEditText edtAddress= order_address_comment.findViewById(R.id.edtAddress);
        final MaterialEditText edtComment=order_address_comment.findViewById(R.id.edtComment);

        final RadioButton rdbShipToAddress=order_address_comment.findViewById(R.id.rdbShipToAddress);
        final RadioButton rdbHomeAddress=order_address_comment.findViewById(R.id.rdbHomeAddress);


        //Radio event
        rdbHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!TextUtils.isEmpty(Common.currentUser.getHomeAddress()) ||
                    Common.currentUser.getHomeAddress()!=null){
                        address=Common.currentUser.getHomeAddress();
                        edtAddress.setText(address);
                    }
                    else
                        Toast.makeText(Cart.this, "Please update your home address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rdbShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=false",mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    try {
                                        JSONObject jsonObject=new JSONObject(response.body().toString());

                                        JSONArray resultArray=jsonObject.getJSONArray("results");

                                        JSONObject firstObject=resultArray.getJSONObject(0);

                                        address=firstObject.getString("formatted_address");
                                        //set this address to edtAddress
                                        edtAddress.setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //show paypal for payment
                //get address and comment
                if(!rdbShipToAddress.isChecked() && !rdbHomeAddress.isChecked())
                    address=edtAddress.getText().toString();
                else if(rdbHomeAddress.isChecked())
                    address=edtAddress.getText().toString();



                comment=edtComment.getText().toString();

                String formatAmount=txtTotalPrice.getText().toString()
                        .replace("$","")
                        .replace(",","");
                //float amount=Float.parseFloat(formatAmount);
                PayPalPayment payPalPayment=new PayPalPayment(new BigDecimal(formatAmount),
                        "USD",
                        "Homely Food",
                        PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent =new Intent(getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
                startActivityForResult(intent,PAYPAL_REQUEST_CODE);

//                //Remove Fragment
//                getSupportFragmentManager().beginTransaction()
//                        .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
//                        .commit();

//                /*Request request= new Request(
//                        Common.currentUser.getPhone(),
//                        Common.currentUser.getName(),
//                        edtAddress.getText().toString(),
//                        txtTotalPrice.getText().toString(),
//                        "0",
//                        edtComment.getText().toString(),
//                        cart
//                );
//                //submit to firebase
//                //we will using System.currentTimeMillie to key
//                String order_number=String.valueOf(System.currentTimeMillis());
//                requests.child(order_number).setValue(request);
//                //delete cart
//                new Database(getBaseContext()).cleanCart();
//
//                Toast.makeText(Cart.this, "Thank you,  Order placed!!!", Toast.LENGTH_SHORT).show();
//                finish();*/
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

//                //Remove Fragment
//                getSupportFragmentManager().beginTransaction()
//                        .remove(getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
//                        .commit();
            }
        });

        alertDialog.show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleAPIClient();
                        createLocationRequest();
                    }
                }
            }break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==PAYPAL_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                PaymentConfirmation paymentConfirmation=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(paymentConfirmation!=null){
                    try {
                        String paymentDetail=paymentConfirmation.toJSONObject().toString(4);
                        JSONObject jsonObject=new JSONObject(paymentDetail);

                        Request request= new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                               address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                jsonObject.getJSONObject("response").getString("state"),//state from jsonobject,
                                //String.format("%s,%s",shippingAddress.getLatLng().latitude,shippingAddress.getLatLng().longitude),
                                cart
                        );
                        //submit to firebase
                        //we will using System.currentTimeMillie to key
                        String order_number=String.valueOf(System.currentTimeMillis());
                        requests.child(order_number).setValue(request);
                        //delete cart
                        new Database(getBaseContext()).cleanCart();

                        Toast.makeText(Cart.this, "Thank you,  Order placed!!!", Toast.LENGTH_SHORT).show();
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else if(resultCode== Activity.RESULT_CANCELED){
                Toast.makeText(this, "payment cancelled", Toast.LENGTH_SHORT).show();
            }else if(resultCode==PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void loadListFood() {
        cart= new Database(this).getCarts();
        adapter= new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate Total price
        int total=0;
        for (Order order:cart){
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        }
        Locale locale=new Locale("en","US");
        NumberFormat numberFormat= NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(numberFormat.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteCart(int position) {
        cart.remove(position);

        new Database(this).cleanCart();

        for (Order item:cart){
            new Database(this).addToCart(item);
        }
        loadListFood();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null){
            Log.d("LOCATION","Your Location "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
        }else {
            Log.d("LOCATION","Couldn't get your location ");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        displayLocation();
    }
}
