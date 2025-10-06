package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.UserDto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MeApi {
    @GET("/auth/me")
    Call<UserDto> me();
}
