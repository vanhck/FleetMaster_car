package com.fleetmaster.fleetmaster_car;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Thomas on 23.06.2017.
 */

public interface FleetMasterServerRequest {
    @GET("lagerliste")
    Call<List<Lager>> listRepos();
}
