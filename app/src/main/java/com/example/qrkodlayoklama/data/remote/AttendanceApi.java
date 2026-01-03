package com.example.qrkodlayoklama.data.remote;

import com.example.qrkodlayoklama.data.remote.model.ActiveSummaryDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceRecordDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceStartRequest;
import com.example.qrkodlayoklama.data.remote.model.AttendanceUpdateRequest;
import com.example.qrkodlayoklama.data.remote.model.ManualAddRequest;
import com.example.qrkodlayoklama.data.remote.model.MarkRequest;
import com.example.qrkodlayoklama.data.remote.model.MyAttendanceSummaryDto; // Yeni DTO import edildi
import com.example.qrkodlayoklama.data.remote.model.SessionHistoryDto;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AttendanceApi {

    @GET("courses/{courseId}/attendance/active")
    Call<AttendanceSessionDto> active(@Path("courseId") Long courseId);

    @POST("courses/{courseId}/attendance/start")
    Call<AttendanceSessionDto> start(@Path("courseId") Long courseId, @Body AttendanceStartRequest req);

    @POST("attendance/mark")
    Call<ResponseBody> mark(@Body MarkRequest req);

    @POST("courses/{courseId}/attendance/stop")
    Call<ResponseBody> stop(@Path("courseId") long courseId);

    @GET("courses/{courseId}/attendance/active/summary")
    Call<ActiveSummaryDto> activeSummary(@Path("courseId") long courseId);

    @GET("courses/{courseId}/attendance/active/records")
    Call<List<AttendanceRecordDto>> records(@Path("courseId") long courseId);

    @POST("courses/{courseId}/attendance/checkin")
    Call<ResponseBody> checkin(@Path("courseId") long courseId, @Body MarkRequest body);

    @GET("courses/{courseId}/attendance/history")
    Call<List<SessionHistoryDto>> history(@Path("courseId") long courseId);

    @GET("courses/{courseId}/attendance/{sessionId}")
    Call<AttendanceSessionDto> sessionDetail(@Path("courseId") long courseId, @Path("sessionId") long sessionId);

    @GET("courses/{courseId}/attendance/{sessionId}/records")
    Call<List<AttendanceRecordDto>> sessionRecords(@Path("courseId") long courseId, @Path("sessionId") long sessionId);

    @GET("attendance/my")
    Call<List<MyAttendanceSummaryDto>> myAttendance();

    @POST("courses/{courseId}/attendance/{sessionId}/manual-add")
    Call<ResponseBody> manualAdd(@Path("courseId") long courseId, @Path("sessionId") long sessionId, @Body ManualAddRequest body);

    @PUT("courses/{courseId}/attendance/{sessionId}")
    Call<AttendanceSessionDto> updateSession(@Path("courseId") long courseId, @Path("sessionId") long sessionId, @Body AttendanceUpdateRequest body);

    @DELETE("courses/{courseId}/attendance/{sessionId}")
    Call<ResponseBody> deleteSession(@Path("courseId") long courseId, @Path("sessionId") long sessionId);

    @DELETE("courses/{courseId}/attendance/{sessionId}/records/{studentId}")
    Call<ResponseBody> manualRemove(@Path("courseId") long courseId, @Path("sessionId") long sessionId, @Path("studentId") long studentId);

}
