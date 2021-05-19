package com.epic.weteri;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;



/*
*  Weteri weather program
*  Author: Roni Viitanen
*/
public class MainActivity extends AppCompatActivity {



    String APIdata="";
    HttpURLConnection httpURLConnection;
    URL Url;
    InputStream inputStream;
    InputStreamReader inputStreamReader;
    String UKUrl;
    SharedPreferences prefs;
    Typeface weatherFont;

    String detailsS,tempS,cityS,pres,hum,wind,visib;


    public class DownloadWeather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];

            try {
                Url = new URL(url);
                httpURLConnection = (HttpURLConnection) Url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while (data != -1) {
                    char current = (char) data;
                    APIdata += current;
                    data = inputStreamReader.read();
                }
                return APIdata;

            } catch (IOException e) {
                e.printStackTrace();

                Thread thread = new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "City not found. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                };
                thread.start();
                return null;
            }

        }
        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                TextView currentTempField=findViewById(R.id.showTemp);
                TextView detailsField=findViewById(R.id.showDetails);
                TextView cityField=findViewById(R.id.showCity);
                TextView windField=findViewById(R.id.wind);
                TextView presField=findViewById(R.id.pressure);
                TextView hField=findViewById(R.id.humidity);
                TextView visField=findViewById(R.id.visibility);

                JSONObject obj = new JSONObject(s);
                cityS=obj.getString("name");
                tempS=String.format("%.0f", obj.getJSONObject("main").getDouble("temp"));
                detailsS=obj.getJSONArray("weather").getJSONObject(0).get("description").toString();
                pres=obj.getJSONObject("main").get("pressure").toString();
                hum=obj.getJSONObject("main").get("humidity").toString();
                wind=obj.getJSONObject("wind").get("speed").toString();
                visib=obj.get("visibility").toString();
                currentTempField.setText(String.format("%s ℃", tempS));
                detailsField.setText(detailsS);
                cityField.setText(cityS);
                windField.setText(String.format("%s%s%s", getString(R.string.wSpeed), wind, getString(R.string.ms)));
                presField.setText(String.format("%s%s%s", getString(R.string.pres), pres, getString(R.string.pa)));
                visField.setText(String.format("%s%s%s", getString(R.string.vis), visib, getString(R.string.m)));
                hField.setText(String.format("%s%s%s", getString(R.string.h), hum, getString(R.string.p)));


                JSONObject details = obj.getJSONArray("weather").getJSONObject(0);
                setWeatherIcon(details.getInt("id"),
                        obj.getJSONObject("sys").getLong("sunrise") * 1000,
                        obj.getJSONObject("sys").getLong("sunset") * 1000);

            } catch (Exception e) {
                Log.i("Json",e.getMessage());
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");

        /*
        * Haetaan SharedPreferencellä tallennettu kaupunki tai näytetään Joensuun sää ensimmäisellä avauskerralla.
         */
        prefs = this.getPreferences(Activity.MODE_PRIVATE);

        showWeather(getCity());

    }

/*
*  InputDialog kaupungin asettamista varten.
 */
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", (dialog, which) -> {
            showWeather(input.getText().toString());

            
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showInputDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showWeather(String city)  {

        /*
        * Haetaan openweathermap palvelun API-avaimen avulla säätiedot.
         */
        if (city.length()>0){
            APIdata="";

            DownloadWeather downloadWeather=new DownloadWeather();
            UKUrl="https://api.openweathermap.org/data/2.5/weather?q="+city+"&units=metric&appid=9c4e7572d53ca2b45bf93c7c5422fb2d";

            downloadWeather.execute(UKUrl);

            /*
            * Tallennetaan kaupunki sharedPreference interface avulla seuraavaa sovelluksen avausta varten.
             */
            setCity(city);


        } else
            Toast.makeText(this, "Enter a valid city", Toast.LENGTH_SHORT).show();





    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = this.getString(R.string.weather_sunny);
            } else {
                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = this.getString(R.string.weather_thunder);
                    break;
                case 3 : icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = this.getString(R.string.weather_foggy);
                    break;
                case 8 : icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = this.getString(R.string.weather_snowy);
                    break;
                case 5 : icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        TextView weatherIcon=findViewById(R.id.wicon);
        weatherIcon.setTypeface(weatherFont);
        weatherIcon.setText(icon);
    }

    void setCity(String city) {

        prefs.edit().putString("city", city).commit();
    }

    public String getCity(){

        return prefs.getString("city", "Joensuu");
    }


}