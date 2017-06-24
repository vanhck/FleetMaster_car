package com.fleetmaster.fleetmaster_car;

/**
 * Created by Thomas on 24.06.2017.
 */

public class Objekt
{
    public String lagerid;

    public String name;

    public String typ;

    public String getLagerid ()
    {
        return lagerid;
    }

    public void setLagerid (String lagerid)
    {
        this.lagerid = lagerid;
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

    @Override
    public String toString()
    {
        return "ClassPojo [lagerid = "+lagerid+", name = "+name+", typ = "+typ+"]";
    }
}
