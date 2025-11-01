package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.UserDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface UsersApi {
    @GET("users/students")
    Call<java.util.List<com.example.qrkodlayoklama.data.remote.model.UserDto>> allStudents();
}
