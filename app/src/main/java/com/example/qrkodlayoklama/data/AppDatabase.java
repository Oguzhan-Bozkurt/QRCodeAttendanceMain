package com.example.qrkodlayoklama.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "qrkodla.db")
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    IO.execute(() -> {
                                        UserDao dao = INSTANCE.userDao();

                                        User u1 = new User();
                                        u1.username = "a_ali";
                                        u1.password = "123456";
                                        u1.role = "ACADEMIC";

                                        User u2 = new User();
                                        u2.username = "s_ayse";
                                        u2.password = "123456";
                                        u2.role = "STUDENT";

                                        dao.insertAll(u1, u2);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
