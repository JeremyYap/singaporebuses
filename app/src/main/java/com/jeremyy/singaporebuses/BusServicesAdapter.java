package com.jeremyy.singaporebuses;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jeremyy on 8/31/2016.
 */
public class BusServicesAdapter extends ArrayAdapter<BusService> {

    public BusServicesAdapter(Context context, ArrayList<BusService> busServices) {
        super(context, 0, busServices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BusService busService = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.bus_arrival_item, parent, false);
            viewHolder.serviceNumber = (TextView) convertView.findViewById(R.id.service_number);
            viewHolder.nextBuses = new TextView[3];
            viewHolder.nextBuses[0] = (TextView) convertView.findViewById(R.id.bus_1_time);
            viewHolder.nextBuses[1] = (TextView) convertView.findViewById(R.id.bus_2_time);
            viewHolder.nextBuses[2] = (TextView) convertView.findViewById(R.id.bus_3_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.serviceNumber.setText(busService.serviceNumber);
        for (int i=0; i<3; i++) {
            int eta = busService.nextBuses.get(i).etaMinutes;
            String etaString = "";
            if (eta > 0) {
                etaString = String.valueOf(eta);
            } else if (eta == 0) {
                etaString = "Arr";
            }
            viewHolder.nextBuses[i].setText(etaString);
            int bgDrawableID = 0;
            switch (busService.nextBuses.get(i).load) {
                case EMPTY:     bgDrawableID = R.drawable.arrival_bg_empty;     break;
                case CROWDED:   bgDrawableID = R.drawable.arrival_bg_crowded;   break;
                case FULL:      bgDrawableID = R.drawable.arrival_bg_full;      break;
            }
            if (bgDrawableID == 0) {
                viewHolder.nextBuses[i].setBackground(null);
            } else {
                viewHolder.nextBuses[i].setBackground(ResourcesCompat.getDrawable(getContext().getResources(), bgDrawableID, null));
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView serviceNumber;
        TextView[] nextBuses;
    }
}
