package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.CourseCreateRequest;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import java.util.List;

public interface CoursesApi {
    @GET("courses")
    Call<List<CourseDto>> all();

    @POST("courses")
    Call<CourseDto> create(@Body CourseCreateRequest req);
}
