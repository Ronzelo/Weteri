package com.epic.weteri;


import android.app.Activity;
import android.content.SharedPreferences;

public class SaveCity {

    SharedPreferences prefs;

    public SaveCity(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }


    public String getCity(){

        return prefs.getString("city", "Joensuu");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

}