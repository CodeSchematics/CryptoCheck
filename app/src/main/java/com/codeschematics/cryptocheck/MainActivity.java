package com.codeschematics.cryptocheck;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity
{
    private final String BASE_URL = "https://apiv2.bitcoinaverage.com/indices/global/ticker/BTC";
    final String LOGCAT_TAG = "CryptoCheck";

    TextView mDateView;
    TextView mTimeZone;
    TextView mPriceTextView;
    TextView mDayAvTextView;
    TextView mWeekAvTextView;
    TextView mMonthAvTextView;

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

        mPriceTextView = findViewById(R.id.priceLabel);
        Spinner spinner = findViewById(R.id.currencySpinner);

        mDayAvTextView = findViewById((R.id.dayAverageValue));
        mWeekAvTextView = findViewById(R.id.weekAverageValue);
        mMonthAvTextView = findViewById(R.id.monthAverageValue);

        // Create an ArrayAdapter using the string array from strings.xml and the spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, R.layout.spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(LOGCAT_TAG, "" + parent.getItemAtPosition(position));
                letsDoSomeNetworking(BASE_URL + parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                Log.d(LOGCAT_TAG, "Nothing selected");
            }
        });

    }

    private void letsDoSomeNetworking(String url)
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
                    String bitcoinValue = response.getString("last");
                    String bitcoinDayAv = response.getJSONObject("averages").getString("day");
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

//    private boolean isNetworkAvailable()
//    {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }
}
