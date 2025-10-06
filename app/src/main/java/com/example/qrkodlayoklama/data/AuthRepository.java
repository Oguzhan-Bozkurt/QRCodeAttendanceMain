package com.example.qrkodlayoklama.data;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {

    public interface Callback {
        void onResult(boolean success, String message, @Nullable String role);
    }

    private final UserDao userDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public AuthRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public void login(String username, String password, Callback cb) {
        io.execute(() -> {
            User user = userDao.findByCredentials(username, password);
            if (user == null) {
                main.post(() -> cb.onResult(false, "Kullanıcı bulunamadı veya şifre hatalı", null));
            } else {
                main.post(() -> cb.onResult(true, "Giriş başarılı", user.role));
            }
        });
    }
}
