package com.example.munia.mapdirection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by munia on 1/17/2018.
 */

public interface DirectionService {

    @GET
    Call<DirectionResponse> getDirection(@Url String urlString);
}