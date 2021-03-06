package edu.monash.fit2081.countryinfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class CountryDetails extends AppCompatActivity {

    private TextView name;
    private TextView capital;
    private TextView code;
    private TextView population;
    private TextView area;
    private TextView currency;
    private TextView languages;
    private ImageView image;
    String countryName;
    private TextView borders;
    private TextView nativeName;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_details);

        getSupportActionBar().setTitle(R.string.title_activity_country_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String selectedCountry = getIntent().getStringExtra("country");

        name = findViewById(R.id.country_name);
        capital = findViewById(R.id.capital);
        code = findViewById(R.id.country_code);
        population = findViewById(R.id.population);
        area = findViewById(R.id.area);
        currency = findViewById(R.id.currency);
        languages = findViewById(R.id.languages);
        image = findViewById(R.id.image_view);
        borders = findViewById(R.id.borders);
        nativeName = findViewById(R.id.nativeName);


        ExecutorService executor = Executors.newSingleThreadExecutor();
        //Executor handler = ContextCompat.getMainExecutor(this);
        Handler uiHandler=new Handler(Looper.getMainLooper());
        Handler handler = new Handler(Looper.getMainLooper());


        executor.execute(() -> {
            //Background work here
            CountryInfo countryInfo = new CountryInfo();



            try {
                // Create URL
                URL webServiceEndPoint = new URL("https://restcountries.com/v2/name/" + selectedCountry); //

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) webServiceEndPoint.openConnection();

                if (myConnection.getResponseCode() == 200) {
                    //JSON data has arrived successfully, now we need to open a stream to it and get a reader
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");

                    //now use a JSON parser to decode data
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    List<String> currencies = null;
                    List<String> language = null;
                    List<String> border = null;
                    jsonReader.beginArray(); //consume arrays's opening JSON brace
                    String keyName;
                    // countryInfo = new CountryInfo(); //nested class (see below) to carry Country Data around in
                    boolean countryFound = false;
                    while (jsonReader.hasNext() && !countryFound) { //process array of objects
                        jsonReader.beginObject(); //consume object's opening JSON brace
                        while (jsonReader.hasNext()) {// process key/value pairs inside the current object
                            keyName = jsonReader.nextName();
                            if (keyName.equals("name")) {
                                countryInfo.setName(jsonReader.nextString());
                                String request = "https://countryflagsapi.com/png/" + countryInfo.getName();
                                java.net.URL url = new java.net.URL(request);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                                handler.post(() -> {image.setImageBitmap(myBitmap);});
                                if (countryInfo.getName().equalsIgnoreCase(selectedCountry)) {
                                    countryFound = true;
                                    countryName = countryInfo.getName();
                                }
                            } else if (keyName.equals("alpha3Code")) {
                                countryInfo.setAlpha3Code(jsonReader.nextString());
                            } else if (keyName.equals("capital")) {
                                countryInfo.setCapital(jsonReader.nextString());
                            } else if (keyName.equals("population")) {
                                countryInfo.setPopulation(jsonReader.nextInt());
                            } else if (keyName.equals("area")) {
                                countryInfo.setArea(jsonReader.nextDouble());
                            } else if(keyName.equals("currencies") && jsonReader.peek() != JsonToken.NULL){
                                currencies = readCurrencyArray(jsonReader);
                                countryInfo.setCurrency(currencies);
                            } else if(keyName.equals("languages") && jsonReader.peek() != JsonToken.NULL){
                                language = readLanguageArray(jsonReader);
                                countryInfo.setLanguage(language);
                            }else if(keyName.equals("borders")){
                                    border = readBordersArray(jsonReader);
                                    countryInfo.setBorders(border);
                            }else if(keyName.equals("nativeName")) {
                                countryInfo.setNativeName(jsonReader.nextString());
                            }else{
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.close();
                    uiHandler.post(()->{
                        name.setText(countryInfo.getName());
                        capital.setText(countryInfo.getCapital());
                        code.setText(countryInfo.getAlpha3Code());
                        population.setText(Integer.toString(countryInfo.getPopulation()));
                        area.setText(Double.toString(countryInfo.getArea()));
                        languages.setText(String.valueOf(countryInfo.getLanguage()));
                        currency.setText(String.valueOf(countryInfo.getCurrency()));
                        borders.setText(String.valueOf(countryInfo.getBorders()));
                        nativeName.setText(countryInfo.getNativeName());
                    });


                } else {
                    Log.i("INFO", "Error:  No response");
                }

                // All your networking logic should be here
            } catch (Exception e) {
                Log.i("INFO", "Error " + e.toString());
            }

        });


    }


    public List<String> readCurrencyArray(JsonReader jsonReader) throws IOException{
        String newName;
        List<String>
        currencies = new ArrayList<String>();
        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                newName = jsonReader.nextName();
                if (newName.equals("name")) {
                    currencies.add(jsonReader.nextString());
                } else {
                    jsonReader.skipValue();
                }
            }
        }
        jsonReader.endObject();
        jsonReader.endArray();
        return currencies;
    }

    public List<String> readLanguageArray(JsonReader jsonReader) throws IOException{
        String newName;
        List<String>
                languages = new ArrayList<String>();

        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    newName = jsonReader.nextName();
                    if (newName.equals("name")) {
                        //while(jsonReader.hasNext()){
                        languages.add(jsonReader.nextString());
                        //}
                    } else
                        jsonReader.skipValue();
                }
            jsonReader.endObject();
            }

        jsonReader.endArray();
        return languages;
    }

    public List<String> readBordersArray(JsonReader jsonReader) throws IOException{
        String newName;
        List<String>
                languages = new ArrayList<String>();

        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
                    languages.add(jsonReader.nextString());
        }

        jsonReader.endArray();
        return languages;
    }

    private class CountryInfo {
        private String name;
        private String alpha3Code;
        private String capital;
        private int population;
        private double area;
        private List<String> currency;
        private List<String> language;
        private String nativeName;
        private List<String> borders;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlpha3Code() {
            return alpha3Code;
        }

        public void setAlpha3Code(String alpha3Code) {
            this.alpha3Code = alpha3Code;
        }

        public String getCapital() {
            return capital;
        }

        public void setCapital(String capital) {
            this.capital = capital;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        public double getArea() {
            return area;
        }

        public void setArea(double area) {
            this.area = area;
        }

        public void setCurrency(List<String> currency){this.currency = currency;}

        public List<String> getCurrency(){return currency;}

        public void setLanguage(List<String> language){this.language = language;}
        /*public void setLanguage(String[] language){
            for(int size = 0; size < language.length; size++ ){this.language = this.language + language[size] + ", "; } }*/

        public List<String> getLanguage(){return language;}

        public void setBorders(List<String> borders){this.borders = borders;}

        public List<String> getBorders(){return borders;}

        public String getNativeName() {
            return nativeName;
        }

        public void setNativeName(String nativeName) {
            this.nativeName = nativeName;
        }
    }

    public void handleGetWikiButton(View v){
        Intent intent = new Intent(this, WebWiki.class);
        Bundle args = new Bundle();
        args.putString("COUNTRYNAME", countryName);
        intent.putExtra("BUNDLE", args);
        startActivity(intent);
    }
}
//String[] setLanguage(String[] language){
// for(int i = 0, i < language.getSize(), i++ ){this.language = language[i] + ", "; } }