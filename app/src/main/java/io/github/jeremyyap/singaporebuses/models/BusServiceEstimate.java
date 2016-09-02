package io.github.jeremyyap.singaporebuses.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jeremyy on 8/31/2016.
 */
public class BusServiceEstimate implements Parcelable {
    public String serviceNumber;
    public boolean operating;
    public List<BusEstimate> nextBuses;

    private static final String LOG_TAG = BusServiceEstimate.class.getSimpleName();

    public BusServiceEstimate() {
        nextBuses = new ArrayList<>();
    }

    private BusServiceEstimate(Parcel in) {
        serviceNumber = in.readString();
        operating = in.readByte() == 1;
        in.readList(nextBuses, BusEstimate.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceNumber);
        dest.writeByte((byte) (operating ? 1: 0));
        dest.writeList(nextBuses);
    }

    public static final Parcelable.Creator<BusServiceEstimate> CREATOR = new Parcelable.Creator<BusServiceEstimate>() {

        @Override
        public BusServiceEstimate createFromParcel(Parcel source) {
            return new BusServiceEstimate(source);
        }

        @Override
        public BusServiceEstimate[] newArray(int size) {
            return new BusServiceEstimate[size];
        }
    };

    public static ArrayList<BusServiceEstimate> fromJson(JSONObject jsonObject, long currentMillis) {
        final String[] nextBusParams = { "NextBus", "SubsequentBus", "SubsequentBus3" };
        final String EMPTY_TEXT = "Seats Available";
        final String CROWDED_TEXT = "Standing Available";
        final String FULL_TEXT = "Limited Standing";
        final SimpleDateFormat dataMallDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.US);

        try {
            JSONArray jsonArray = jsonObject.getJSONArray("Services");
            ArrayList<BusServiceEstimate> results = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonBusService = jsonArray.getJSONObject(i);
                BusServiceEstimate busServiceEstimate = new BusServiceEstimate();
                busServiceEstimate.serviceNumber = jsonBusService.getString("ServiceNo");
                busServiceEstimate.operating = jsonBusService.getString("Status").equals("In Operation");

                for (String nextBusParam : nextBusParams) {
                    JSONObject jsonBus = jsonBusService.getJSONObject(nextBusParam);
                    BusEstimate busEstimate = new BusEstimate();

                    busEstimate.etaMinutes = -1;

                    String eta = jsonBus.getString("EstimatedArrival");
                    if (!eta.equals("")) {
                        try {
                            long arrivalMillis = dataMallDateFormat.parse(eta).getTime();
                            busEstimate.etaMinutes = (int) ((arrivalMillis - currentMillis) / 60000);
                            if (busEstimate.etaMinutes <= 0) busEstimate.etaMinutes = 0;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    String busLoadDesc = jsonBus.getString("Load");
                    switch (busLoadDesc) {
                        case EMPTY_TEXT:
                            busEstimate.load = BusEstimate.Load.EMPTY;
                            break;
                        case CROWDED_TEXT:
                            busEstimate.load = BusEstimate.Load.CROWDED;
                            break;
                        case FULL_TEXT:
                            busEstimate.load = BusEstimate.Load.FULL;
                            break;
                        default:
                            busEstimate.load = BusEstimate.Load.UNKNOWN;
                    }

                    busEstimate.wheelchairAccessible = jsonBus.getString("Feature").equals("WAB");
                    busServiceEstimate.nextBuses.add(busEstimate);
                }

                results.add(busServiceEstimate);
            }
            return results;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error", e);
            e.printStackTrace();
        }
        return null;
    }
}
