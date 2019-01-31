package es.xabertum.weatheron.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.xabertum.weatheron.R;
import es.xabertum.weatheron.adapters.HourAdapter;
import es.xabertum.weatheron.weather.HourlyWeather;

/**
 * Activity con la prediccion por horas.
 */
public class HourlyForecastActivity extends AppCompatActivity {

    private HourlyWeather[] mHourlyWeathers;

   @BindView(R.id.recyclerView) RecyclerView mRecyclerView;

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
