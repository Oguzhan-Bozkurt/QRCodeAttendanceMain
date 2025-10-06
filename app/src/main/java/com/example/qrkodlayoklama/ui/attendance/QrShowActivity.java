package com.example.qrkodlayoklama.ui.attendance;

import static java.time.Instant.parse;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceStartRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrShowActivity extends AppCompatActivity {

    public static final String EXTRA_COURSE_ID = "courseId";

    private ProgressBar progress;
    private TextView tvTitle, tvInfo, tvSecret, tvExpire, tvStatus;
    private ImageView imgQr;
    private Long courseId;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable ticker;
    private Instant expiresAt;
    private Button btnStop, btnRefresh;
    private Call<AttendanceSessionDto> inflight;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_show);

        courseId = getIntent().getLongExtra(EXTRA_COURSE_ID, -1);
        if (courseId == -1) {
            Toast.makeText(this, "Ders bilgisi bulunamadı", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progress = findViewById(R.id.progress);
        tvTitle  = findViewById(R.id.tvTitle);
        tvInfo   = findViewById(R.id.tvInfo);
        tvSecret = findViewById(R.id.tvSecret);
        tvExpire = findViewById(R.id.tvExpire);
        imgQr    = findViewById(R.id.imgQr);
        btnStop = findViewById(R.id.btnStop);
        tvStatus  = findViewById(R.id.tvStatus);
        btnRefresh= findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(v -> loadActive());

        btnStop.setOnClickListener(v -> {
            setLoading(true);

            ApiClient.attendance().stop(courseId).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call,
                                                 retrofit2.Response<okhttp3.ResponseBody> resp) {
                    setLoading(false);
                    if (resp.isSuccessful() || resp.code() == 404) {
                        if (imgQr != null) imgQr.setVisibility(View.GONE);
                        if (tvInfo != null) tvInfo.setText("Yoklama oturumu kapatıldı.");
                        if (btnStop != null) btnStop.setEnabled(false);
                        if (tvSecret != null) tvSecret.setText("Kod:");
                        if (tvExpire != null) tvExpire.setText("Kalan süre: -"); stopTicker();
                        Toast.makeText(QrShowActivity.this, "Oturum kapatıldı", Toast.LENGTH_SHORT).show();
                        showNoActive();
                    } else {
                        Toast.makeText(QrShowActivity.this, "Hata: " + resp.code(), Toast.LENGTH_LONG).show();
                        btnStop.setEnabled(true);
                    }
                }

                @Override public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(QrShowActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    btnStop.setEnabled(true);
                }
            });
        });

        tvTitle.setText("Yoklama - Ders ID: " + courseId);
        loadOrStart();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (inflight != null) inflight.cancel();
        stopTicker();
    }

    private void setLoading(boolean b) {
        progress.setVisibility(b ? View.VISIBLE : View.GONE);
        imgQr.setAlpha(b ? 0.3f : 1f);
    }

    private void loadOrStart() {
        setLoading(true);
        inflight = ApiClient.attendance().active(courseId);
        inflight.enqueue(new Callback<AttendanceSessionDto>() {
            @Override public void onResponse(Call<AttendanceSessionDto> call, Response<AttendanceSessionDto> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    bindSession(resp.body());
                } else if (resp.code() == 404) {
                    startSession(10); // aktif yoksa 10 dk’lık yeni oturum aç
                } else if (resp.code() == 401) {
                    setLoading(false);
                    Toast.makeText(QrShowActivity.this, "Oturum süresi doldu (401)", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    setLoading(false);
                    Toast.makeText(QrShowActivity.this, "Hata: " + resp.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<AttendanceSessionDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QrShowActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startSession(int minutes) {
        inflight = ApiClient.attendance().start(courseId, new AttendanceStartRequest(minutes));
        inflight.enqueue(new Callback<AttendanceSessionDto>() {
            @Override public void onResponse(Call<AttendanceSessionDto> call, Response<AttendanceSessionDto> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    bindSession(resp.body());
                } else {
                    setLoading(false);
                    Toast.makeText(QrShowActivity.this, "Başlatılamadı: " + resp.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<AttendanceSessionDto> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QrShowActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindSession(AttendanceSessionDto dto) {
        setLoading(false);
        tvInfo.setText("Yoklama aktif");
        tvSecret.setText("Kod: " + dto.getSecret());

        String payload = dto.getSecret();
        imgQr.setImageBitmap(makeQr(payload, 900));

        // geri sayım
        try {
            expiresAt = parse(dto.getExpiresAt());
        } catch (Exception e) {
            expiresAt = null;
        }
        startTicker();
    }

    private void startTicker() {
        stopTicker();
        ticker = new Runnable() {
            @Override public void run() {
                if (expiresAt == null) {
                    tvExpire.setText("Kalan süre: -");
                    return;
                }
                long secs = Duration.between(Instant.now(), expiresAt).getSeconds();
                if (secs <= 0) {
                    tvExpire.setText("Süre doldu");
                    tvInfo.setText("Oturum kapandı");
                    imgQr.setAlpha(0.3f);
                    return;
                }
                long m = secs / 60;
                long s = secs % 60;
                tvExpire.setText(String.format("Kalan süre: %02d:%02d", m, s));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(ticker);
    }

    private void stopTicker() {
        if (ticker != null) {
            handler.removeCallbacks(ticker);
            ticker = null;
        }
    }

    private Bitmap makeQr(String text, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new MultiFormatWriter()
                    .encode(text, BarcodeFormat.QR_CODE, size, size, hints);

            int w = matrix.getWidth();
            int h = matrix.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                int offset = y * w;
                for (int x = 0; x < w; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
            return bmp;
        } catch (Exception e) {
            Toast.makeText(this, "QR üretilemedi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void loadActive() {
        setLoading(true); // varsa, yoksa kaldırın
        ApiClient.attendance().active(courseId).enqueue(new retrofit2.Callback<AttendanceSessionDto>() {
            @Override public void onResponse(retrofit2.Call<AttendanceSessionDto> call,
                                             retrofit2.Response<AttendanceSessionDto> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    showActive(resp.body());
                } else if (resp.code() == 404) {
                    showNoActive();
                } else {
                    showError("Hata: " + resp.code());
                }
            }

            @Override public void onFailure(retrofit2.Call<AttendanceSessionDto> call, Throwable t) {
                setLoading(false);
                showError("Ağ hatası: " + t.getMessage());
            }
        });
    }

    private void showActive(AttendanceSessionDto dto) {
        if (imgQr != null) imgQr.setVisibility(View.VISIBLE);
        if (btnStop != null) { btnStop.setEnabled(true); btnStop.setVisibility(View.VISIBLE); }
        if (tvStatus != null) { tvStatus.setVisibility(View.GONE); tvStatus.setText(""); }
        if (btnRefresh != null) btnRefresh.setVisibility(View.GONE);

    }

    private void showNoActive() {
        if (imgQr != null) imgQr.setVisibility(View.GONE);
        if (btnStop != null) { btnStop.setEnabled(false); }
        if (tvStatus != null) {
            tvStatus.setText("Bu ders için aktif yoklama oturumu yok.");
            tvStatus.setVisibility(View.VISIBLE);
        }
        if (btnRefresh != null) btnRefresh.setVisibility(View.VISIBLE);
    }

    private void showError(String msg) {
        if (imgQr != null) imgQr.setVisibility(View.GONE);
        if (btnStop != null) btnStop.setEnabled(false);
        if (tvStatus != null) {
            tvStatus.setText(msg);
            tvStatus.setVisibility(View.VISIBLE);
        }
        if (btnRefresh != null) btnRefresh.setVisibility(View.VISIBLE);
    }
}
