package com.example.qrkodlayoklama.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    //private static final String BASE_URL = "http://1192.168.1.3:8080/"; ev
    private static final String BASE_URL = "http://192.168.214.254:8080/";

    private static Retrofit retrofit;

    private static UsersApi usersApi;

    private ApiClient() {}

    public static Retrofit get() {
        if (retrofit == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(log)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }

    public static AuthApi auth() {
        return get().create(AuthApi.class);
    }

    public static MeApi me() {
        return get().create(MeApi.class);
    }

    public static CoursesApi courses() { return get().create(CoursesApi.class); }

    public static AttendanceApi attendance() { return get().create(AttendanceApi.class); }

    public static UsersApi users() { return retrofit.create(UsersApi.class); }
}
