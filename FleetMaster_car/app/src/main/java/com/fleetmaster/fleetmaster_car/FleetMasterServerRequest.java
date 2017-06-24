package com.fleetmaster.fleetmaster_car;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Thomas on 23.06.2017.
 */

public interface FleetMasterServerRequest {
    @GET("lagerliste")
    Call<List<Lager>> listRepos();

    @GET("{lagerid}/{warenid}/{longitude}/{latitude}")
    Call<List<Objekt>> ok(@Path("lagerid")String lagerid,@Path("warenid")String warenid,@Path("longitude")String longs ,@Path("latitude")String lat);

    @GET("{lagerid}/{lon}/{lat}")
    void updateLagerPos(@Path("lagerid")String lagerid,@Path("lon")String lon, @Path("lat")String lat);
}
