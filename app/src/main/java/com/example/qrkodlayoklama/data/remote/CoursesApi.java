package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.AddStudentsRequest;
import com.example.qrkodlayoklama.data.remote.model.CourseCreateRequest;
import com.example.qrkodlayoklama.data.remote.model.CourseDto;
import com.example.qrkodlayoklama.data.remote.model.CourseStudentDto;

import okhttp3.ResponseBody;
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
    Call<List<CourseStudentDto>> listStudents(@Path("courseId") long courseId);

    @POST("courses/{courseId}/students")
    Call<ResponseBody> addStudents(@Path("courseId") long courseId, @Body AddStudentsRequest req);

    @DELETE("courses/{courseId}/students/{userName}")
    Call<ResponseBody> removeStudent(@Path("courseId") long courseId, @Path("userName") long userName);
}
