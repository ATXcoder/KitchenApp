package home.citadel.apps.inventory;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by thomas on 9/7/14.
 */
public class Scanner extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        Log.i("Inventory", result.getText()); // Prints scan results
        Log.i("Inventory", result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        //Build out URL
        String url = "http://atxcoder.ddns.net:82/api/v1/menu/items/" + result.getText();
        Log.i("Inventory", "API URL: " + url);
        ScanItem scan = new ScanItem();
        scan.execute(url);
    }

    private class ScanItem extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            // New instance of HTTP Client
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            // Create HTTP POST
            HttpGet httppost = new HttpGet(strings[0]);

            //Depends on your web service
            httppost.setHeader("Content-type", "application/json");

            InputStream inputStream = null;
            String result;
            JSONObject responseObject = null;

            try {
                // Execute the HTTP call
                HttpResponse response = httpclient.execute(httppost);

                // Get the result of the HTTP call
                HttpEntity entity = response.getEntity();

                // Get the content of the result
                inputStream = entity.getContent();

                // Read the response from the web service in JSON. JSON is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                result = sb.toString();

                Log.i("Inventory", "JSON String: " + result);

                // Create a JSON Object from result
                JSONObject jObject = new JSONObject(result);

                String validation = jObject.getString("valid");
                String message = jObject.getString("message");

                Log.i("Inventory", "Valid: " + validation + " \n Message: " + message);

                responseObject = jObject;

            } catch (Exception e) {

                Log.e("Inventory", "ERR: " + e.getMessage());
            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
            }
            return responseObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            // execution of result of Long time consuming operation
           // Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_LONG).show();
            //Log.i("Inventory", "JSONObj: " + result.toString());


            try {
                String validation = result.getString("valid");
                String message = result.getString("message");
                String source = result.getString("source");



                // Make sure we didn't get a error back
                if(validation.equals("false"))
                {
                    // Show user error
                    Toast.makeText(getApplicationContext(),"ERROR: " + message, Toast.LENGTH_LONG).show();
                }
                else
                {
                    // We got a response, lets see if it's from the web or DB
                    if(source.equals("Web"))
                    {
                        JSONObject item = result.getJSONObject("item");
                        String name = item.getString("name");
                        String upc = item.getString("upc");

                        Intent i = new Intent(getApplicationContext(), CreateItem.class);
                        i.putExtra("name",name);
                        i.putExtra("upc",upc);
                        startActivity(i);
                    }
                    else
                    {
                        // Let user know we found item
                        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();


                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }





        }
    }
}
