package es.xabertum.weatheron.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.xabertum.weatheron.R;
import es.xabertum.weatheron.weather.Current;
import es.xabertum.weatheron.weather.DailyWeather;
import es.xabertum.weatheron.weather.Forecast;
import es.xabertum.weatheron.weather.HourlyWeather;


/**
 * Created by Xabertum on 23-Aug-15.
 */

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int ACCESS_FINE_LOCATION_PERMISSION = 101;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    protected Location mLastLocation;
    protected TextView mLocationAddressTextView;
    public static final String TAG = MainActivity.class.getSimpleName();
    public Forecast mforecast;
    public DailyWeather mDailyWeather;
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    public static final String WEEK_SUMMARY = "";
    public String weekSummary;
    public String locality;
    public String mIcon;


    @BindView(R.id.timeLabel)
    TextView mTimeLabel;
    @BindView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @BindView(R.id.humidityValue)
    TextView mHumidityValue;
    @BindView(R.id.precipValue)
    TextView mPrecipValue;
    @BindView(R.id.summaryLabel)
    TextView mSummaryLabel;
    @BindView(R.id.iconImageView)
    ImageView mIconImageView;
    @BindView(R.id.refreshImageView)
    ImageView mRefreshImageView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.mainBackground)
    RelativeLayout mMainBackground;
    @BindView(R.id.location)
    TextView mLocation;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        Log.d(TAG, "Main UI code is running!");

        mLocationAddressTextView = (TextView) findViewById(R.id.location);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        buildGoogleApiClient();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(60000); // 1 second, in milliseconds

        mLocationAddressTextView.setText(locality);

    }

    /**
     * Construye el objeto cliente del API de Google.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Devuelve el pronóstico del tiempo en la latitude y longitude pasadas como argumento.
     *
     * @param latitude  - latitud geografica.
     * @param longitude - longitud geográfica.
     */
    private void getForecast(double latitude, double longitude) {
        String apiKey = "6038b2fca99d5702cc65f0245857ae6b";

        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude + "?units=si";

        if (isNetworkAvailable()) {

            toogleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toogleRefresh();
                        }
                    });
                    alertUserAboutError(getString(R.string.error_title), getString(R.string.error_message));
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toogleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mforecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError(getString(R.string.error_title), getString(R.string.error_message));
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exeception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exeception caught: ", e);
                    }
                }
            });
        } else {
            alertUserAboutError(getString(R.string.error_title), getString(R.string.network_unavailable_message));
        }
    }

    /**
     * @param jsonData
     * @return
     * @throws JSONException
     */
    public DailyWeather[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");
        weekSummary = daily.getString("summary");

        DailyWeather[] days = new DailyWeather[data.length()];

        for (int i = 0; i < data.length(); i++) {

            JSONObject jsonDay = data.getJSONObject(i);
            DailyWeather dailyWeather = new DailyWeather();

            dailyWeather.setSummary(weekSummary);
            dailyWeather.setIcon(jsonDay.getString("icon"));
            dailyWeather.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            dailyWeather.setTime(jsonDay.getLong("time"));
            dailyWeather.setTimezone(timezone);

            days[i] = dailyWeather;
        }

        Log.i(TAG, weekSummary);

        return days;

    }


    /**
     * @param jsonData
     * @return
     * @throws JSONException
     */
    private HourlyWeather[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        HourlyWeather[] hourlyWeathers = new HourlyWeather[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            HourlyWeather hourlyWeather = new HourlyWeather();

            hourlyWeather.setSummary(jsonHour.getString("summary"));
            hourlyWeather.setTemperature(jsonHour.getDouble("temperature"));
            hourlyWeather.setIcon(jsonHour.getString("icon"));
            hourlyWeather.setTime(jsonHour.getLong("time"));
            hourlyWeather.setTimezone(timezone);

            hourlyWeathers[i] = hourlyWeather;

        }

        return hourlyWeathers;
    }

    /**
     * @param jsonData
     * @return
     * @throws JSONException
     */
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());

        return current;
    }


    //------------------------------- METODOS CONEXION APIs ----------------------------------------

    /**
     * @return
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;

        }
        return isAvailable;
    }

    /**
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        permissionsCheck();

    }


    public void permissionsCheck () {

        final Geocoder geocoder = new Geocoder(this);
        final String errorMessage = "";


        // Verficamos si la API es = 23 o mayor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            Log.d("permissionCheck", "Value" + permissionCheck);

            //verificamos si el permiso no existe
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // verificamos si el usuario a rechazado anteriormente el permiso
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // si a rechazado el permiso anteriormente muestro un mensaje
                    mostrarExplicación();
                } else {
                    // De lo contrario carga la ventana para autorizar el permiso
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
                }
            } else {

                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(final Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object

                                    final double lastLatitude = location.getLatitude();
                                    final double lastLongitude = location.getLongitude();

                                    mRefreshImageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            getForecast(lastLatitude, lastLongitude);
                                        }
                                    });

                                    getForecast(lastLatitude, lastLongitude);

                                    List<Address> addresses;
                                    try {

                                        addresses = geocoder.getFromLocation(lastLatitude, lastLongitude, 1);

                                        if (addresses != null) {

                                            String geocoderAPIKey = "AIzaSyAyId31OM3GoKX1pkYvRg4DkVjcWGm2P7o";

                                            String revGeocoderUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lastLatitude + "," + lastLongitude
                                                    + "&key=" + geocoderAPIKey;

                                            OkHttpClient client2 = new OkHttpClient();
                                            Request request2 = new Request.Builder().url(revGeocoderUrl).build();

                                            Call call = client2.newCall(request2);
                                            call.enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Request request2, IOException e) {

                                                    alertUserAboutError(getString(R.string.error_title), getString(R.string.error_message));
                                                }

                                                @Override
                                                public void onResponse(Response response) throws IOException {

                                                    try {
                                                        String jsonDataGeocoder = response.body().string();

                                                        if (response.isSuccessful()) {

                                                            Log.v(TAG, " ESTO " + jsonDataGeocoder);
                                                            JSONObject geocoderAPI = new JSONObject(jsonDataGeocoder);

                                                            JSONObject compound_code = geocoderAPI.getJSONObject("plus_code");
                                                            String compound_code_array = compound_code.getString("compound_code");
                                                            locality = compound_code_array.substring(8);

                                                            Log.i(TAG, "Geocoder API connected:" + locality);

                                                        } else {
                                                            alertUserAboutError(getString(R.string.error_title), getString(R.string.error_message));
                                                        }

                                                    } catch (JSONException e) {
                                                        Log.e(TAG, "Exeception caught: ", e);
                                                    }
                                                }
                                            });

                                            mLocationAddressTextView.setText(locality);
                                            Log.v(TAG, "texView:" + locality);

                                        }
                                    } catch (IOException io) {
                                        Log.e(TAG, errorMessage, io);
                                    }
                                } else {
                                    alertUserAboutError(getString(R.string.error_title), getString(R.string.no_address));
                                }
                            }
                        });


            }
        } else {
            Toast.makeText(getApplicationContext(), "API es mayor de 23", Toast.LENGTH_SHORT).show();

        }

    }




    private void mostrarExplicación() {
        new AlertDialog.Builder(this)
                .setTitle("Autorización")
                .setMessage("Necesito permiso para acceder a la ubicacion de tu dispositivo.")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERMISSION);
                        }

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Mensaje acción cancelada
                        mensajeAccionCancelada();
                    }


                })
                .show();
    }

    public void mensajeAccionCancelada(){
        Toast.makeText(getApplicationContext(),
                "Haz rechazado la petición, por favor considere en aceptarla.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_FINE_LOCATION_PERMISSION:
                // Si el permiso ha sido concedido llamamos a onConnected()
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "Callback funcionando",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mensajeAccionCancelada();
                }
        }

    }

    /**
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        final double currentLatitude = location.getLatitude();
        final double currentLongitude = location.getLongitude();
        Log.i(TAG, "current Location" + currentLatitude + currentLongitude);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(currentLatitude, currentLongitude);
            }
        });

        getForecast(currentLatitude, currentLongitude);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);

            if (addresses != null || addresses.size() == 0) {
                alertUserAboutError(getString(R.string.error_title), getString(R.string.no_address));
            } else {
                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                String countryName = addresses.get(0).getAddressLine(2);

                Log.i(TAG, "Location services connected." + cityName.replaceAll("\\d", ""));
                Log.i(TAG, "Location services connected." + stateName.replaceAll("\\d", ""));
                Log.i(TAG, "Location services connected." + countryName);

                mLocationAddressTextView.setText(cityName.replaceAll("\\d", "") + ", " + stateName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateDisplay();
    }

    /**
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    //-------------------------------- METODOS AUXILIARES ------------------------------------------

    /**
     * Boton de refresco de los datos con el pronostico del tiempo.
     */
    private void toogleRefresh() {

        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mforecast.getCurrent();

        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + "");
        mHumidityValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
        mLocation.setText(locality);

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);

        Drawable drawableBg = getResources().getDrawable(current.getBackgroundId());
        mMainBackground.setBackground(drawableBg);

    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void alertUserAboutError(String alertTitle, String alertBody) {
        Bundle messageArgs = new Bundle();
        messageArgs.putString(AlertDialogFragment.TITLE_ID, alertTitle);
        messageArgs.putString(AlertDialogFragment.MESSAGE_ID, alertBody);

        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.setArguments(messageArgs);
        dialog.show(getFragmentManager(), "error_dialog");

    }


    //--------------------------------------------- BOTONES ----------------------------------------
    @OnClick(R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mforecast.getDailyForecast());
        intent.putExtra(WEEK_SUMMARY, weekSummary);
        startActivity(intent);

    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mforecast.getHourlyForecast());
        startActivity(intent);
    }
}
