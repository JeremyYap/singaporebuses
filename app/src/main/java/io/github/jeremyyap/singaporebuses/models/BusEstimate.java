package io.github.jeremyyap.singaporebuses.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jeremyy on 8/31/2016.
 */
public class BusEstimate implements Parcelable {
    public int etaMinutes;
    public Load load;
    public boolean wheelchairAccessible;

    public BusEstimate() {}

    private BusEstimate(Parcel in) {
        etaMinutes = in.readInt();
        load = (Load) in.readSerializable();
        wheelchairAccessible = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(etaMinutes);
        dest.writeSerializable(load);
        dest.writeByte((byte) (wheelchairAccessible ? 1 : 0));
    }

    public static final Creator<BusEstimate> CREATOR = new Creator<BusEstimate>() {
        @Override
        public BusEstimate createFromParcel(Parcel in) {
            return new BusEstimate(in);
        }

        @Override
        public BusEstimate[] newArray(int size) {
            return new BusEstimate[size];
        }
    };

    public enum Load {
        EMPTY, CROWDED, FULL, UNKNOWN
    }
}
