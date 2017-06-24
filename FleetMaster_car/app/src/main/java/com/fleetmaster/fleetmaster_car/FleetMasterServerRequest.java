package com.fleetmaster.fleetmaster_car;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Interface for the usage of the Retrofit with different get requests.
 * Created by Thomas on 23.06.2017.
 */

public interface FleetMasterServerRequest {
    @GET("lagerliste")
    Call<List<Lager>> listRepos();

    @GET("{lagerid}/{warenid}/{longitude}/{latitude}")
    Call<List<Objekt>> ok(@Path("lagerid")String lagerid,@Path("warenid")String warenid,@Path("longitude")String longs ,@Path("latitude")String lat);

    @GET("{lagerid}/{lon}/{lat}")
    Call<ResponseBody> updateLagerPos(@Path("lagerid")String lagerid, @Path("lon")String lon, @Path("lat")String lat);

    @GET("{userid}/{lagerid}")
    Call<ResponseBody> checkUser(@Path("userid")String userid, @Path("lagerid")String lagerid);
}
