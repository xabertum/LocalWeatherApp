package es.xabertum.weatheron.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import butterknife.Bind;
import es.xabertum.weatheron.R;
import es.xabertum.weatheron.weather.DailyWeather;

/**
 * Created by Xabertum on 30-Aug-15.
 */
public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private DailyWeather[] mDAys;


    public DayAdapter (Context context, DailyWeather[] days) {
        mContext = context;
        mDAys = days;

    }



    @Override
    public int getCount() {
        return mDAys.length;
    }

    @Override
    public Object getItem(int position) {
        return mDAys[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if( convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.daily_list__view, null);
            holder = new ViewHolder();
            holder.iconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);
            holder.tempeartureLabel = (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.dayLabel = (TextView) convertView.findViewById(R.id.dayNameLabel);

            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder) convertView.getTag();

        }

        DailyWeather day = mDAys[position];


        holder.iconImageView.setImageResource(day.getIconId());
        holder.tempeartureLabel.setText(day.getTemperatureMax() + "º");



        if (position == 0) {
            holder.dayLabel.setText("Today");
        }
        else {
            holder.dayLabel.setText(day.getDayOfTheWeek());
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView iconImageView;
        TextView tempeartureLabel;
        TextView dayLabel;
   }

}
