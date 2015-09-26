package edu.softserve.administrator.exampleapplication;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.softserve.administrator.exampleapplication.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
    private static final String ICON_BASE_URL = "http://openweathermap.org/img/w/";
    private static final String IS_WEATHER_LOADED = "IS_WEATHER_LOADED";
    private boolean mIsWeatherLoaded = false;

    private TextView mTemperatureTextView;
    private TextView mWindTextView;
    private TextView mPressureTextView;
    private TextView mHumidityTextView;
    private TextView mTitle;
    private ImageView mWeatherIconImageView;
    private RelativeLayout mContentView;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mWindTextView = (TextView) findViewById(R.id.wind);
        mPressureTextView = (TextView)findViewById(R.id.pressure);
        mHumidityTextView = (TextView)findViewById(R.id.humidity);
        mTitle = (TextView) findViewById(R.id.title);
        mWeatherIconImageView = (ImageView) findViewById(R.id.weatherIcon);
        mContentView = (RelativeLayout) findViewById(R.id.content);

        mIsWeatherLoaded = savedInstanceState != null && savedInstanceState.getBoolean(IS_WEATHER_LOADED);
        if (!mIsWeatherLoaded) {
            WeatherAsyncTask weatherAsyncTask = new WeatherAsyncTask();
            weatherAsyncTask.execute(OPEN_WEATHER_MAP_API);
        } else {
            mTitle.setText(WeatherData.getInstance().description);
            mTemperatureTextView.setText(WeatherData.getInstance().temperature);
            mWindTextView.setText(WeatherData.getInstance().windSpeed);
            mPressureTextView.setText(WeatherData.getInstance().pressure);
            mHumidityTextView.setText(WeatherData.getInstance().humidity);
            mWeatherIconImageView.setImageBitmap(WeatherData.getInstance().icon);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_WEATHER_LOADED, mIsWeatherLoaded);
        super.onSaveInstanceState(outState);
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

    private class WeatherAsyncTask extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected void onPreExecute() {
            mContentView.setVisibility(View.GONE);
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setMessage("Asking Boston commons... already inquired:");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected JSONObject doInBackground(String... params){
            InputStream is = null;
            JSONObject json = null;

            try {
                for (int i = 0; i < 100; i++) {
                    Thread.sleep(50);
                    publishProgress((i + 1));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(params[0]);
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
                JSONObject weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0);

                WeatherData.getInstance().description = weatherDescription.getString("main") + ": " + weatherDescription.getString("description");
                WeatherData.getInstance().temperature = "Temperature: " + main.getString("temp") + "\u00b0 C";
                WeatherData.getInstance().windSpeed = "Wind speed: " + jsonObject.getJSONObject("wind").getString("speed") + "m/s";
                WeatherData.getInstance().humidity = "Humidity: " + main.getString("humidity") + "%";
                WeatherData.getInstance().pressure = "Pressure : " + main.getString("pressure") + " hpa";

                Picasso.with(getApplicationContext())
                    .load(ICON_BASE_URL + jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png")
                    .into(mWeatherIconImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mIsWeatherLoaded = true;
                            mProgressDialog.dismiss();
                            mContentView.setVisibility(View.VISIBLE);
                            WeatherData.getInstance().icon = ((BitmapDrawable) mWeatherIconImageView.getDrawable()).getBitmap();

                            if (mIsWeatherLoaded) {
                                mTitle.setText(WeatherData.getInstance().description);
                                mTemperatureTextView.setText(WeatherData.getInstance().temperature);
                                mWindTextView.setText(WeatherData.getInstance().windSpeed);
                                mPressureTextView.setText(WeatherData.getInstance().pressure);
                                mHumidityTextView.setText(WeatherData.getInstance().humidity);
                                mWeatherIconImageView.setImageBitmap(WeatherData.getInstance().icon);
                            }
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getApplicationContext(), "Unnable to load weather icon status", Toast.LENGTH_SHORT).show();
                        }
                    });

            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
}
