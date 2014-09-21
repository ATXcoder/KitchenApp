package home.citadel.apps.inventory;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class CreateItem extends Activity {

    private final String LOG_KEY = "Inventory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_item);

        //Get Intent extras
        String item_name = getIntent().getStringExtra("name");
        String item_upc = getIntent().getStringExtra("upc");

        // Get buttons
        Button bttn_save = (Button)findViewById(R.id.bttn_save);

        // Get the inputs
        final EditText itemName = (EditText)findViewById(R.id.txt_itemName);
        final EditText itemUPC = (EditText)findViewById(R.id.txt_upc);

        itemName.setText(item_name);
        itemUPC.setText(item_upc);

        bttn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the array
                String[] item = new String[2];
                item[0] = itemName.getText().toString();
                item[1] = itemUPC.getText().toString();

                // Send array to web service
                SaveItem saveitem = new SaveItem();
                saveitem.execute(item);

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class SaveItem extends AsyncTask<String, Void, JSONObject>
    {
        // Call to API to save item
        @Override
        protected JSONObject doInBackground(String... strings) {

            JSONObject item = new JSONObject();
            JSONObject responseObject = null;

            try{
                HttpResponse response;
                InputStream inputStream = null;
                String result = null;


                // Get database URL
                String dbURL = Settings.Read(getApplicationContext(), "pref_db");
                dbURL = dbURL + "items";

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(dbURL);


                item.put("name",strings[0]);
                item.put("upc", strings[1]);
                StringEntity se = new StringEntity(item.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);

                response = client.execute(post);

                // Get the result of the HTTP call
                //HttpEntity entity = response.getEntity();

                // Get the content of the result
                inputStream = response.getEntity().getContent();

                // Read the response from the web service in JSON. JSON is UTF-8 by default
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder sb = new StringBuilder();

                String line = null;

                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }

                // Convert response to a JSON formatted string
                result = sb.toString();

                // Convert JSON formatted string (result) to a JSONObject
                responseObject = new JSONObject(result);

            } catch (Exception e){

            }
            return responseObject;
        }

        @Override
        protected void onPostExecute(JSONObject result)
        {
            try {
                String validation = result.getString("valid");
                String message = result.getString("message");

                Log.i(LOG_KEY, message);

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            } catch (Exception e)
            {

            }
        }
    }
}
