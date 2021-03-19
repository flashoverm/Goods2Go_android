package org.goods2go.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.goods2go.authentication.SessionItem;
import com.goods2go.models.ShipmentSize;
import com.goods2go.models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class OfflineStorage {

    private static final int MODE = Context.MODE_PRIVATE;

    private static final String FILE = "OfflineStorageFile";
    private static final String KEY_ITEM = "SessionItem";
    private static final String KEY_USER = "User";
    private static final String KEY_SIZES = "Sizes";

    public static SessionItem loadSessionItem(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(FILE, MODE);
        String sessionItemString = preferences.getString(KEY_ITEM, null);
        Gson gson = new Gson();
        return gson.fromJson(sessionItemString, SessionItem.class);
    }

    public static User loadUser(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(FILE, MODE);
        String sessionItemString = preferences.getString(KEY_USER, null);
        Gson gson = new Gson();
        return gson.fromJson(sessionItemString, User.class);
    }

    public static List<ShipmentSize> loadSizes(Context context){
        SharedPreferences preferences = context.getSharedPreferences(FILE, MODE);
        String sessionItemString = preferences.getString(KEY_SIZES, null);
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ShipmentSize>>(){}.getType();
        return gson.fromJson(sessionItemString, listType);
    }

    public static boolean saveSessionItem(Context context, SessionItem sessionItem){
        SharedPreferences preferences = context.getSharedPreferences(FILE, MODE);
        SharedPreferences.Editor edit = preferences.edit();
        if(sessionItem != null){
            Gson gson = new Gson();
            edit.putString(KEY_ITEM, gson.toJson(sessionItem));
        } else {
            edit.putString(KEY_ITEM, null);
        }
        return edit.commit();
    }

    public static boolean saveUser(Context context, User user){
        SharedPreferences preferences = context.getSharedPreferences(FILE, MODE);
        SharedPreferences.Editor edit = preferences.edit();
        if(user != null){
            Gson gson = new Gson();
            edit.putString(KEY_USER, gson.toJson(user));
        } else {
            edit.putString(KEY_USER, null);
        }
        return edit.commit();
    }

    public static boolean saveSizes(Context context, List<ShipmentSize> sizes){
        SharedPreferences preferences = context.getSharedPreferences(FILE, MODE);
        SharedPreferences.Editor edit = preferences.edit();
        if(sizes != null){
            Gson gson = new Gson();
            edit.putString(KEY_SIZES, gson.toJson(sizes));
        } else {
            edit.putString(KEY_SIZES, null);
        }
        return edit.commit();
    }

}
