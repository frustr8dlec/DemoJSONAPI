package com.example.demojsonapi;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*  Import notes these imports are required to allow the code to function

Imports to allow easy processing of JSON
    import org.json.JSONException;
    import org.json.JSONObject;

Import to handle catching of any input output exceptions
    import java.io.IOException;

Imports for the parts of OkHttp3 needed to build a HTTP based API request
    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.HttpUrl;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.Response;

 */
public class MainActivity extends AppCompatActivity {

    private TextView mTextViewJSONName;
    private TextView mTextViewJSONTheme;
    private TextView mTextViewJSONData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the TextViews to allow us to update them
        mTextViewJSONName = (TextView)findViewById(R.id.textViewName);
        mTextViewJSONTheme = (TextView)findViewById(R.id.textViewTheme);
        mTextViewJSONData = (TextView)findViewById(R.id.textViewJSON);


        // This is the call to the  method getHTTPData(); that handles all the requesting
        // of the api data should anything fail it will throw an IOException
        try {
            getHTTPData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getHttpData()
     *
     * creates a OkHTTPClient schedules a request to be made from the API and sets up methods to
     * handle the success or failure of the request
     *
     * @throws IOException
     */
    void getHTTPData() throws IOException {

        /*
          Create a new OkHttp client to handle requesting data from and receiving back from the API
          see https://square.github.io/okhttp/  documentation, please be aware that this is a
          Java library it is not specifically written for Android.
        */

        OkHttpClient client = new OkHttpClient();

        /*
         Example get request from the ancient woodlands API as it would appear in a browser

         https://services.arcgis.com/JJzESW51TqeY9uat/arcgis/rest/services/Ancient_Woodland_England/FeatureServer/0/query
         ?where=NAME%20LIKE%20%27ABBOTS%20WOOD%25%27
         &outFields=OBJECTID,NAME,THEME,THEMNAME,THEMID,STATUS,X_COORD,Y_COORD
         &returnGeometry=false
         &returnDistinctValues=true
         &outSR=4326
         &f=json

         Building the same information using the HttpUrl.Builder object

         url is everything before the ? everything after the ? are API query parameters that are
         added using the addQueryParameter e.g.
         where=NAME%20LIKE%20%27ABBOTS%20WOOD%25%27 becomes
         .addQueryParameter("where", "NAME LIKE 'ABBOTS WOOD'") in the browser example above
         %20 is a space character and %27 is a single quote see
         https://www.w3schools.com/tags/ref_urlencode.ASP for a list.

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://services.arcgis.com/JJzESW51TqeY9uat/arcgis/rest/services/Ancient_Woodland_England/FeatureServer/0/query").newBuilder();

        urlBuilder.addQueryParameter("where", "NAME LIKE 'ABBOTS WOOD'");
        urlBuilder.addQueryParameter("outFields", "OBJECTID,NAME,THEME,THEMNAME,THEMID,STATUS,X_COORD,Y_COORD");
        urlBuilder.addQueryParameter("returnGeometry", "false");
        urlBuilder.addQueryParameter("returnDistinctValues", "true");
        urlBuilder.addQueryParameter("outSR", "4326");
        urlBuilder.addQueryParameter("f", "json");
        */
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.openweathermap.org/data/2.5/weather").newBuilder();

        urlBuilder.addQueryParameter("lat", "53.230606408229466");
        urlBuilder.addQueryParameter("lon", "-0.5407005174034509");
        urlBuilder.addQueryParameter("appid", ""); // your api key


        // Convert the built url to a string
        String url = urlBuilder.build().toString();

        // Create a new OkHTTP3 request object using the url created

        Request request = new Request.Builder()
                .url(url)
                .build();

        /*
          Create a new call request, this is put into a scheduler that will execute the request
          in the background when the request completes the appropriate Callback() object method is
          called

          onFailure() - error in the request or no able to send the request
          onResponse() - the server based API return information

         */

        client.newCall(request).enqueue(new Callback() {

            /**
             * Call back method that is triggered when the request failed
             * @param call the object in the queue that failed
             * @param e an exception object detailing the IO related error
             */
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            /**
             * Call back method that is triggered when a response was recieved from the API
             * @param call the object in the queue that succeeded
             * @param response a response object with the data returned
             * @throws IOException
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                // Grab the data from the API response as a string
                final String myResponse = response.body().string();

                response.close();

                // Run this part of the code on the UI thread so it can update the TextViews
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // output the response text to logcat
                       Log.d("OkHTTPResponse",myResponse);
                       // Decode the string to a JSON object and extract specific parts
                       try {
                            JSONObject json = new JSONObject(myResponse);

                            // Put the entire text into the bottom TextView
                            mTextViewJSONData.setText(json.toString());

                            /*
                             From the returned data we are interested in the rows of result
                             objects (attributes)
                             which are held in an array called features
                             in this example we are extracting the first row only multiple values
                             would require a loop


                            JSONObject oAttributes = json.getJSONArray("features")
                                    .getJSONObject(0)
                                    .getJSONObject("attributes");


                             Output the name of the woods and the theme
                             */

                           JSONArray oAttributes = json.getJSONArray("weather");
                           mTextViewJSONTheme.setText(oAttributes.getJSONObject(0).getString("description"));
                           mTextViewJSONName.setText(oAttributes.getJSONObject(0).getString("main"));

                       } catch (JSONException e) {
                            // Something went wrong when processing the JSON data output the error
                            e.printStackTrace();
                       }
                    }
                });

            }
        });
    }


}