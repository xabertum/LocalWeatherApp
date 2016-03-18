package es.xabertum.weatheron.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.xabertum.weatheron.R;
import es.xabertum.weatheron.adapters.HourAdapter;
import es.xabertum.weatheron.weather.HourlyWeather;

/**
 * Activity con la prediccion por horas.
 */
public class HourlyForecastActivity extends ActionBarActivity {

    private HourlyWeather[] mHourlyWeathers;

   @Bind(R.id.recyclerView) RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.HOURLY_FORECAST);
        mHourlyWeathers = Arrays.copyOf(parcelables, parcelables.length, HourlyWeather[].class);

        HourAdapter adapter = new HourAdapter(mHourlyWeathers);
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);


    }

  
}
