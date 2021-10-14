package com.erdatamedia.estimator.networking;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface NetworkService {

    @GET("app/dashboard")
    Call<ResponseDashboard> dashboard();

    @FormUrlEncoded
    @POST("app/formula")
    Call<EstimasiResponse> estimate(
            @FieldMap Map<String, String> param
    );

    @FormUrlEncoded
    @POST("app/update_param")
    Call<Boolean> update_param(
            @Field("param") String param
    );

}
