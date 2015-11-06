package es.xabertum.weatheron.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.xabertum.weatheron.R;
import es.xabertum.weatheron.adapters.DayAdapter;
import es.xabertum.weatheron.weather.DailyWeather;
import es.xabertum.weatheron.weather.Forecast;

public class DailyForecastActivity extends ListActivity {

    private DailyWeather[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);

        String weekSummary = intent.getStringExtra(MainActivity.WEEK_SUMMARY);
        TextView textView = (TextView) findViewById(R.id.weekSummary);
        textView.setText(weekSummary);


        mDays = Arrays.copyOf(parcelables, parcelables.length, DailyWeather[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);

    }


}
