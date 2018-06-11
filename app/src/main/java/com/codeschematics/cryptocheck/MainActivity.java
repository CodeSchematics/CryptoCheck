package com.codeschematics.cryptocheck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
{
    private final String BASE_URL = "https://apiv2.bitcoinaverage.com/indices/global/ticker/BTC";
    final String LOGCAT_TAG = "CryptoCheck";

    String bitcoinValue;
    String bitcoinDayAv;

    TextView mDateView;
    TextView mTimeZone;
    TextView mPriceTextView;
    TextView mDayAvTextView;
    TextView mWeekAvTextView;
    TextView mMonthAvTextView;

    Calendar mCalendar = GregorianCalendar.getInstance();
    String today;
    String lastDay;
    GraphView graph;

    String day0Value;
    String day1Value;
    String day2Value;
    String day3Value;
    String day4Value;
    String day5Value;
    String day6Value;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Display current date
        mDateView = findViewById(R.id.textDate);
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        mDateView.setText(date);
        // and time zone
        mTimeZone = findViewById(R.id.timeZone);
        String timeZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        mTimeZone.setText(timeZone);

        // set dates for graph
        mCalendar.setTime(new Date());
        mCalendar.add(Calendar.DAY_OF_YEAR, -7);
        today = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date());
        lastDay = new SimpleDateFormat("MMM dd").format(mCalendar.getTime());

        mPriceTextView = findViewById(R.id.priceLabel);
        Spinner spinner = findViewById(R.id.currencySpinner);

        mDayAvTextView = findViewById((R.id.dayAverageValue));
        mWeekAvTextView = findViewById(R.id.weekAverageValue);
        mMonthAvTextView = findViewById(R.id.monthAverageValue);

        graph = findViewById(R.id.graph);

        // Create an ArrayAdapter using the string array from strings.xml and the spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapter);

        //load previous selection with loadSpinnerPosition()
        spinner.setSelection(loadSpinnerPosition());
        Log.d(LOGCAT_TAG, "The pref is: "+loadSpinnerPosition());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(LOGCAT_TAG, "" + parent.getItemAtPosition(position));
                saveSpinnerPosition(position);
                if(day0Value != null)
                {
                    Log.d(LOGCAT_TAG, "restarting activity");
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                getTicker(BASE_URL + parent.getItemAtPosition(position));
                getHistory("https://apiv2.bitcoinaverage.com/indices/global/history/BTC"+parent.getItemAtPosition(position)+"?period=alltime&format=json");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                Log.d(LOGCAT_TAG, "Nothing selected");
            }
        });

    }

    private void getTicker(String url)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // called when response HTTP status is "200 OK"
                Log.d(LOGCAT_TAG, "JSON: " + response.toString());

                // Parse JSON
                try
                {
                    bitcoinValue = response.getString("last");
                    bitcoinDayAv = response.getJSONObject("averages").getString("day");
                    String bitcoinWeekAv = response.getJSONObject("averages").getString("week");
                    String bitcoinMonthAv = response.getJSONObject("averages").getString("month");

                    mPriceTextView.setText(bitcoinValue);
                    mPriceTextView.setTextSize(36);

                    mDayAvTextView.setText(bitcoinDayAv);
                    mWeekAvTextView.setText(bitcoinWeekAv);
                    mMonthAvTextView.setText(bitcoinMonthAv);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(LOGCAT_TAG, "Request fail! Status code: " + statusCode);
                Log.d(LOGCAT_TAG, "Fail response: " + response);
                Log.e("ERROR", e.toString());
                mPriceTextView.setTextSize(26);
                mPriceTextView.setText(R.string.label_error_text);
            }
        });
    }

    private void getHistory(String url)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response)
            {
                // called when response HTTP status is "200 OK"

                // Parse JSON
                try
                {
                    //day0 is yesterday
                    day0Value = response.getJSONObject(0).getString("average");
                    day1Value = response.getJSONObject(1).getString("average");
                    day2Value = response.getJSONObject(2).getString("average");
                    day3Value = response.getJSONObject(3).getString("average");
                    day4Value = response.getJSONObject(4).getString("average");
                    day5Value = response.getJSONObject(5).getString("average");
                    day6Value = response.getJSONObject(6).getString("average");

                    loadGraph();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    Log.d(LOGCAT_TAG, "try-catch ERROR");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response)
            {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d(LOGCAT_TAG, "Request fail! Status code: " + statusCode);
                Log.d(LOGCAT_TAG, "Fail response: " + response);
                Log.e(LOGCAT_TAG, e.toString());
            }
        });
    }

//    private boolean isNetworkAvailable()
//    {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }

    private void loadGraph()
    {
        double numbertwo = Double.valueOf(day1Value);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, Double.valueOf(day0Value)),
                new DataPoint(1, numbertwo),
                new DataPoint(2, Double.valueOf(day2Value)),
                new DataPoint(3, Double.valueOf(day3Value)),
                new DataPoint(4, Double.valueOf(day4Value)),
                new DataPoint(5, Double.valueOf(day5Value)),
                new DataPoint(6, Double.valueOf(day6Value)),
                new DataPoint(7, Double.valueOf(bitcoinDayAv))
        });

        Paint paint = new Paint();
        int color = ContextCompat.getColor(this, R.color.colorAccent);
        paint.setColor(color);
        paint.setStrokeWidth(8);
        series.setCustomPaint(paint);

        graph.addSeries(series);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(7);
        graph.getViewport().setMinY(numbertwo - 1000);
        graph.getViewport().setMaxY(numbertwo + 1000);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.setTitle(lastDay + " - " + today);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }

    private void saveSpinnerPosition(int position)
    {
        SharedPreferences prefs = getSharedPreferences("PREFS",0);
        prefs.edit().putInt("POSITION_KEY",position).apply();
        Log.d(LOGCAT_TAG, "saving: " + position);
    }
    private int loadSpinnerPosition()
    {
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        int position = prefs.getInt("POSITION_KEY", 0);
        return position;
    }
}
