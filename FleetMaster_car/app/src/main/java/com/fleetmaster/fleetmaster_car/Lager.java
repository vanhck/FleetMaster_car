package com.fleetmaster.fleetmaster_car;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Thomas on 23.06.2017.
 */

public class Lager implements Parcelable
{
    private String lon;

    private String hash;

    private String name;

    private String typ;

    private String lat;

    protected Lager(Parcel in) {
        lon = in.readString();
        hash = in.readString();
        name = in.readString();
        typ = in.readString();
        lat = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lon);
        dest.writeString(hash);
        dest.writeString(name);
        dest.writeString(typ);
        dest.writeString(lat);
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

    public String getLon ()
    {
        return lon;
    }

    public void setLon (String lon)
    {
        this.lon = lon;
    }

    public String getHash ()
    {
        return hash;
    }

    public void setHash (String hash)
    {
        this.hash = hash;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getTyp ()
    {
        return typ;
    }

    public void setTyp (String typ)
    {
        this.typ = typ;
    }

    public String getLat ()
    {
        return lat;
    }

    public void setLat (String lat)
    {
        this.lat = lat;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [lon = "+lon+", hash = "+hash+", name = "+name+", typ = "+typ+", lat = "+lat+"]";
    }
}