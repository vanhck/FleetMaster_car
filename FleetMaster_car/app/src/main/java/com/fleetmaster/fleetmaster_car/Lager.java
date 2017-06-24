package com.fleetmaster.fleetmaster_car;

import android.os.Parcel;
import android.os.Parcelable;

/** Object to handle storage capacities in the system. Used as pojo-element for the json-api.
 * Created by Thomas on 23.06.2017.
 */

public class Lager implements Parcelable
{
    public String id;

    public String name;

    public String typ;

    protected Lager(Parcel in) {
        id = in.readString();
        name = in.readString();
        typ = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(typ);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Lager> CREATOR = new Creator<Lager>() {
        @Override
        public Lager createFromParcel(Parcel in) {
            return new Lager(in);
        }

        @Override
        public Lager[] newArray(int size) {
            return new Lager[size];
        }
    };


    @Override
    public String toString()
    {
        return "ClassPojo [lagerid = "+id+", name = "+name+", typ = "+typ+"]";
    }
}
