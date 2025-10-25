package com.example.qrkodlayoklama.ui.attendance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrkodlayoklama.R;
import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.AttendanceSessionDto;
import com.example.qrkodlayoklama.data.remote.model.AttendanceStartRequest;
import com.example.qrkodlayoklama.ui.BaseActivity;
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

public class QrShowActivity extends BaseActivity {

    public static final String EXTRA_COURSE_ID = "courseId";
    private boolean polling = false;
    private Call<AttendanceSessionDto> inflight;
    private ProgressBar progress;
    private TextView tvTitle, tvInfo, tvSecret, tvExpire, tvStatus, tvJoined;
    private ImageView imgQr;
    private Long courseId;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable ticker, pollTask;
    private Instant expiresAt;
    private Button btnStop, btnRefresh, btnStart, btnSeeJoined, btnHistory;
    private Spinner spMinutes;
    private LinearLayout boxStart;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_show);
        setupToolbar("Ekran Başlığı", true);
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
        tvJoined = findViewById(R.id.tvJoined);
        spMinutes = findViewById(R.id.spMinutes);
        boxStart  = findViewById(R.id.boxStart);
        btnStart  = findViewById(R.id.btnStart);
        btnSeeJoined = findViewById(R.id.btnSeeJoined);
        btnHistory = findViewById(R.id.btnHistory);

        ArrayAdapter<CharSequence> adp =
                ArrayAdapter.createFromResource(this, R.array.minutes_labels,
                        android.R.layout.simple_spinner_item);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMinutes.setAdapter(adp);

        btnStart.setOnClickListener(v -> {
            int mins = getSelectedMinutes();
            startSession(mins);
        });

        btnSeeJoined.setOnClickListener(v -> {
            Intent i = new Intent(this, com.example.qrkodlayoklama.ui.attendance.AttendanceRecordsActivity.class);
            i.putExtra(com.example.qrkodlayoklama.ui.attendance.AttendanceRecordsActivity.EXTRA_COURSE_ID, courseId);
            startActivity(i);
        });

        btnHistory.setOnClickListener(v -> {
            startActivity(
                    new android.content.Intent(
                            QrShowActivity.this,
                            com.example.qrkodlayoklama.ui.attendance.AttendanceHistoryActivity.class
                    ).putExtra(
                            com.example.qrkodlayoklama.ui.attendance.AttendanceHistoryActivity.EXTRA_COURSE_ID,
                            courseId
                    )
            );
        });

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
                        if (tvExpire != null) tvExpire.setText("Kalan süre: -");
                        stopTicker();
                        stopPolling();
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

    private int getSelectedMinutes() {
        String[] vals = getResources().getStringArray(R.array.minutes_values);
        int idx = spMinutes.getSelectedItemPosition();
        try { return Integer.parseInt(vals[idx]); } catch (Exception e) { return 10; }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (inflight != null) inflight.cancel();
        stopTicker();
    }

    @Override protected void onStop() {
        super.onStop();
        stopPolling();
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
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    bindSession(resp.body());
                } else if (resp.code() == 404) {
                    showNoActive();
                } else if (resp.code() == 401) {
                    Toast.makeText(QrShowActivity.this, "Oturum süresi doldu (401)", Toast.LENGTH_LONG).show();
                    finish();
                } else {
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

        if (imgQr != null)      imgQr.setVisibility(View.VISIBLE);
        if (btnStop != null) {  btnStop.setEnabled(true); btnStop.setVisibility(View.VISIBLE); }
        if (tvStatus != null) { tvStatus.setText(""); tvStatus.setVisibility(View.GONE); }
        if (btnRefresh != null) btnRefresh.setVisibility(View.GONE);
        if (boxStart != null)   boxStart.setVisibility(View.GONE);
        if (btnSeeJoined != null) btnSeeJoined.setVisibility(View.VISIBLE);

        tvInfo.setText("Yoklama aktif");
        tvSecret.setText("Kod: " + dto.getSecret());

        String payload = "ATT|" + courseId + "|" + dto.getSecret();
        Bitmap bmp = makeQr(payload, 900);
        if (bmp != null) imgQr.setImageBitmap(bmp);

        try { expiresAt = java.time.Instant.parse(dto.getExpiresAt()); }
        catch (Exception e) { expiresAt = null; }

        startPolling(courseId);
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
                    stopPolling();
                    showNoActive();
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
        setLoading(true);
        ApiClient.attendance().active(courseId).enqueue(new retrofit2.Callback<AttendanceSessionDto>() {
            @Override public void onResponse(retrofit2.Call<AttendanceSessionDto> call,
                                             retrofit2.Response<AttendanceSessionDto> resp) {
                setLoading(false);
                if (resp.isSuccessful() && resp.body() != null) {
                    bindSession(resp.body());
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
        if (boxStart != null) boxStart.setVisibility(View.GONE);
    }

    private void showNoActive() {
        stopTicker();
        if (imgQr != null) imgQr.setVisibility(View.GONE);
        if (btnStop != null) btnStop.setEnabled(false);
        if (tvStatus != null) {
            tvStatus.setText("Bu ders için aktif yoklama oturumu yok.");
            tvStatus.setVisibility(View.VISIBLE);
        }
        if (btnRefresh != null) btnRefresh.setVisibility(View.VISIBLE);
        if (boxStart != null)   boxStart.setVisibility(View.VISIBLE);
        tvSecret.setText("Kod:");
        tvExpire.setText("Kalan süre: -");
        if (btnSeeJoined != null) btnSeeJoined.setVisibility(View.GONE);
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

    private void startPolling(long courseId) {
        if (polling) return;
        polling = true;
        pollTask = pollRunnable(courseId);
        handler.post(pollTask);
    }

    private void stopPolling() {
        polling = false;
        if (pollTask != null) {
            handler.removeCallbacks(pollTask);
            pollTask = null;
        }
    }

    private Runnable pollRunnable(long courseId) {
        return new Runnable() {
            @Override public void run() {
                if (!polling) return;

                ApiClient.attendance().activeSummary(courseId)
                        .enqueue(new retrofit2.Callback<com.example.qrkodlayoklama.data.remote.model.ActiveSummaryDto>() {
                            @Override public void onResponse(
                                    retrofit2.Call<com.example.qrkodlayoklama.data.remote.model.ActiveSummaryDto> call,
                                    retrofit2.Response<com.example.qrkodlayoklama.data.remote.model.ActiveSummaryDto> resp) {

                                if (!polling) return;

                                if (resp.isSuccessful() && resp.body() != null) {
                                    long c = resp.body().getCount();
                                    tvJoined.setText("Katılan: " + c);
                                    if (polling && pollTask != null) {
                                        handler.postDelayed(pollTask, 5000);
                                    }
                                } else if (resp.code() == 404) {
                                    tvJoined.setText("Oturum kapandı");
                                    stopPolling();
                                } else {
                                    if (polling && pollTask != null) {
                                        handler.postDelayed(pollTask, 5000);
                                    }
                                }
                            }

                            @Override public void onFailure(
                                    retrofit2.Call<com.example.qrkodlayoklama.data.remote.model.ActiveSummaryDto> call,
                                    Throwable t) {
                                if (!polling) return;
                                if (polling && pollTask != null) {
                                    handler.postDelayed(pollTask, 5000);
                                }
                            }
                        });
            }
        };
    }
}
