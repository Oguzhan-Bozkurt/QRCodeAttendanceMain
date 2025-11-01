package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.CourseCreateRequest;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;
import com.example.qrkodlayoklama.data.remote.model.UserDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface CoursesApi {
    @GET("courses")
    Call<List<CourseDto>> all();

    @POST("courses")
    Call<CourseDto> create(@Body CourseCreateRequest req);

    @GET("courses/{courseId}/students")
    Call<List<UserDto>> students(@Path("courseId") long courseId);

    @POST("courses/{courseId}/students")
    Call<okhttp3.ResponseBody> addStudents(@Path("courseId") long courseId, @Body com.example.qrkodlayoklama.data.remote.model.AddStudentsRequest body);

    @DELETE("courses/{courseId}/students/{userName}")
    Call<Void> removeStudent(@Path("courseId") long courseId, @Path("userName") long userName);
}
