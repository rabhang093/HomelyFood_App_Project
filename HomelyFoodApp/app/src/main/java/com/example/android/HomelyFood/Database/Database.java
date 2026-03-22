package com.example.android.HomelyFood.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.example.android.HomelyFood.Model.Order;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {

    private static final String DB_NAME="mydb.db";
    private static final int DB_VERSION=1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query="CREATE TABLE \"OrderDetail\" (\n" +
                "\t\"ID\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
                "\t\"ProductId\"\tTEXT,\n" +
                "\t\"ProductName\"\tTEXT,\n" +
                "\t\"Quantity\"\tTEXT,\n" +
                "\t\"Price\"\tTEXT,\n" +
                "\t\"Discount\"\tTEXT,\n" +
                "\t\"Image\"\tTEXT\n" +
                ");";
        db.execSQL(query);
        query="CREATE TABLE \"Favorites\" (\n" +
                "\t\"FoodId\"\tTEXT UNIQUE,\n" +
                "\tPRIMARY KEY(\"FoodId\")\n" +
                ");";
        db.execSQL(query);
        Log.d("TAG","inside onCreate");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("TAG","inside onUpgrade");
    }


    public List<Order> getCarts(){

        SQLiteDatabase db= getReadableDatabase();
        SQLiteQueryBuilder qb= new SQLiteQueryBuilder();

        String[] sqlSelect={"ID","ProductId","ProductName","Quantity","Price","Discount","Image"};
        String sqlTable="OrderDetail";
        qb.setTables(sqlTable);
        Cursor cursor=qb.query(db,sqlSelect,null,null,null,null,null);

        final List<Order> result=new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                result.add(new Order(cursor.getInt(cursor.getColumnIndex("ID")),
                        cursor.getString(cursor.getColumnIndex("ProductId")),
                        cursor.getString(cursor.getColumnIndex("ProductName")),
                        cursor.getString(cursor.getColumnIndex("Quantity")),
                        cursor.getString(cursor.getColumnIndex("Price")),
                        cursor.getString(cursor.getColumnIndex("Discount")),
                        cursor.getString(cursor.getColumnIndex("Image"))));
            }while (cursor.moveToNext());
        }
        return result;

    }

    public void addToCart(Order order){
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("INSERT INTO OrderDetail(ProductId,ProductName,Quantity,Price,Discount,Image) VALUES('%s','%s','%s','%s','%s','%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());
        db.execSQL(query);
    }

    public void cleanCart(){
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
    }

    public void addToFavorites(String foodId){
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("INSERT INTO Favorites(FoodId) VALUES('%S');",foodId);
        db.execSQL(query);
    }

    public void removeFromFavorites(String foodId){
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("DELETE FROM Favorites WHERE FoodId='%S';",foodId);
        db.execSQL(query);
    }

    public boolean isFavorite(String foodId){
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("SELECT * FROM Favorites WHERE FoodId='%S';",foodId);
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.getCount()<=0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public int getCountCart() {
        int count=0;
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("SELECT COUNT(*) FROM OrderDetail");
        Cursor cursor=db.rawQuery(query,null);
        if(cursor.moveToFirst()){
            do{
                count=cursor.getInt(0);
            }while (cursor.moveToNext());
        }
        return count;
    }

    public void updateCart(Order order) {
        SQLiteDatabase db=getReadableDatabase();
        String query=String.format("UPDATE OrderDetail SET Quantity='%S' WHERE ID=%d",order.getQuantity(),order.getID());
        db.execSQL(query);
    }
}
