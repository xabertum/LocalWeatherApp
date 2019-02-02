package es.xabertum.weatheron.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import java.util.Arrays;

import es.xabertum.weatheron.R;
import es.xabertum.weatheron.adapters.DayAdapter;
import es.xabertum.weatheron.weather.DailyWeather;

/**
 * Activity con la prediccion por Días.
 */
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
