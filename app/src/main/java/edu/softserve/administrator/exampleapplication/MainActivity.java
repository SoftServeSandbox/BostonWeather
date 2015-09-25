package edu.softserve.administrator.exampleapplication;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.softserve.administrator.exampleapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    private static final String OPEN_WEATHER_MAP_API = "http://api.openweathermap.org/data/2.5/weather?q=Boston&units=metric";
    private TextView mTemperatureTextView;
    private TextView mWindTextView;
    private TextView mPressureTextView;
    private TextView mHumidityTextView;
    private TextView mSunriseTextView;
    private TextView mSunsetTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mWindTextView = (TextView) findViewById(R.id.wind);
        mPressureTextView = (TextView)findViewById(R.id.pressure);
        mHumidityTextView = (TextView)findViewById(R.id.humidity);

        WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask();
        weatherAsyncTask.execute(OPEN_WEATHER_MAP_API);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class WeatherAsyncTask extends AsyncTask<String, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(String... params){
            URL url = null;
            InputStream is = null;
            JSONObject json = null;

            try {
                url = new URL(params[0]);
                is = url.openStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                json = new JSONObject(jsonText);
            } catch (IOException|JSONException ex) {
                ex.printStackTrace();

            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            return json;
        }

        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                Log.v("", jsonObject.toString());
                JSONObject main = jsonObject.getJSONObject("main");
                mTemperatureTextView.setText("Temperature: " + main.getString("temp") + "\u00b0 C");
                mWindTextView.setText("Wind speed: " + jsonObject.getJSONObject("wind").getString("speed") + "m/s");
                mHumidityTextView.setText("Humidity: " + main.getString("humidity") + "%");
                mPressureTextView.setText("Pressure : " + main.getString("pressure") + " hpa");
                JSONObject sys = jsonObject.getJSONObject("sys");
            //    Date sunrise= new Date(Integer.getInteger(sys.getString("sunrise"))*1000);
            //    Date sunset = new Date(Integer.getInteger(sys.getString("sunset"))*1000);
             //   DateFormat df = new SimpleDateFormat("HH:mm:ss");

            //    mSunriseTextView.setText("Sunrise : " + df.format(sunrise));
            //    mSunsetTextView.setText("Sunset : " + df.format(sunset));

            }catch(JSONException ex) {
                ex.printStackTrace();
            }
        }


    }
}
