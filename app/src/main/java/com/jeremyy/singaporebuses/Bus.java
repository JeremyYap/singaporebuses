package com.jeremyy.singaporebuses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jeremyy on 8/31/2016.
 */
public class Bus implements Parcelable {
    public int etaMinutes;
    public Load load;
    public boolean wheelchairAccessible;

    public Bus() {}

    private Bus(Parcel in) {
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

    public static final Creator<Bus> CREATOR = new Creator<Bus>() {
        @Override
        public Bus createFromParcel(Parcel in) {
            return new Bus(in);
        }

        @Override
        public Bus[] newArray(int size) {
            return new Bus[size];
        }
    };

    public enum Load {
        EMPTY, CROWDED, FULL, UNKNOWN
    }
}
