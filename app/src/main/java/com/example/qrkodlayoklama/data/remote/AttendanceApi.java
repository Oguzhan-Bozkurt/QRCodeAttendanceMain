package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceStartRequest;
import com.example.qrkodlayoklama.data.remote.model.MarkRequest;
import com.example.qrkodlayoklama.data.remote.model.AttendanceCheckRequest;
import com.example.qrkodlayoklama.data.remote.model.ActiveSummaryDto;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AttendanceApi {
    @GET("courses/{courseId}/attendance/active")
    Call<AttendanceSessionDto> active(@Path("courseId") Long courseId);

    @POST("courses/{courseId}/attendance/start")
    Call<AttendanceSessionDto> start(@Path("courseId") Long courseId, @Body AttendanceStartRequest req);

    @POST("attendance/mark")
    Call<ResponseBody> mark(@Body MarkRequest req);

    @POST("attendance/check")
    Call<Void> check(@Body AttendanceCheckRequest req);

    @POST("courses/{courseId}/attendance/stop")
    Call<okhttp3.ResponseBody> stop(@Path("courseId") long courseId);

    @GET("courses/{courseId}/attendance/active/summary")
    Call<ActiveSummaryDto> activeSummary(@Path("courseId") long courseId);

    @GET("courses/{courseId}/attendance/active/records")
    Call<List<AttendanceRecordDto>> records(@Path("courseId") long courseId);

    @POST("courses/{courseId}/attendance/checkin")
    Call<ResponseBody> checkin(@Path("courseId") long courseId, @Body MarkRequest body);

    @GET("courses/{courseId}/attendance/history")
    Call<java.util.List<com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto>>
    history(@Path("courseId") long courseId);

}
