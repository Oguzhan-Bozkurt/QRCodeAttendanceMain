package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.LoginRequest;
import com.example.qrkodlayoklama.data.remote.model.LoginResponse;
import com.example.qrkodlayoklama.data.remote.model.RegisterRequest;
import com.example.qrkodlayoklama.data.remote.model.UserDto;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest body);

    @GET("/auth/me")
    Call<UserDto> me();

    @POST("auth/register")
    Call<ResponseBody> register(@Body RegisterRequest req);
}