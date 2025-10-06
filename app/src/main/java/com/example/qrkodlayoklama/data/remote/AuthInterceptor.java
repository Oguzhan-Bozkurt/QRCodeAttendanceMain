package com.example.qrkodlayoklama.data.remote;

import androidx.annotation.NonNull;
import com.example.qrkodlayoklama.data.local.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @NonNull @Override public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();

        String path = req.url().encodedPath(); // örn: /courses
        if (path.equals("/auth/login")) {
            return chain.proceed(req);
        }

        String token = SessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            req = req.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }
        return chain.proceed(req);
    }
}
