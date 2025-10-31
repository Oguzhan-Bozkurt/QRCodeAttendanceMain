package com.example.qrkodlayoklama.ui.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.qrkodlayoklama.data.remote.ApiClient;
import com.example.qrkodlayoklama.data.remote.model.MarkRequest;
import com.example.qrkodlayoklama.ui.BaseActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScanActivity extends BaseActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<Intent> scanLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(
                        result.getResultCode(), result.getData());
                if (scanResult == null) {
                    Toast.makeText(this, "İşlem iptal edildi", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                String content = scanResult.getContents();
                if (content == null || content.isEmpty()) {
                    Toast.makeText(this, "Boş QR", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Parsed parsed = parseCourseIdAndSecret(content);
                if (parsed == null) {
                    Toast.makeText(this, "Geçersiz QR", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ApiClient.attendance().checkin(parsed.courseId, new MarkRequest(parsed.secret))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                                if (resp.isSuccessful()) {
                                    Toast.makeText(QrScanActivity.this, "Yoklama gönderildi", Toast.LENGTH_SHORT).show();
                                } else if (resp.code() == 404 || resp.code() == 410) {
                                    Toast.makeText(QrScanActivity.this, "Aktif yoklama bulunamadı veya süresi doldu.", Toast.LENGTH_LONG).show();
                                } else if (resp.code() == 409) {
                                    Toast.makeText(QrScanActivity.this, "Bu oturuma zaten katıldınız.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(QrScanActivity.this, "Hata: " + resp.code(), Toast.LENGTH_LONG).show();
                                }
                                finish();
                            }
                            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(QrScanActivity.this, "Ağ hatası: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
            });

    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startScan();
                } else {
                    Toast.makeText(this, "Kamera izni gerekli", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    private void startScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("QR kodu taratın");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        Intent intent = integrator.createScanIntent();
        scanLauncher.launch(intent);
    }

    private @Nullable String parseSecret(String content) {
        if (content == null) return null;
        content = content.trim();

        // 1) ATT|<courseId>|<secret>
        if (content.startsWith("ATT|")) {
            String[] parts = content.split("\\|");
            if (parts.length >= 3) return parts[2].trim();
        }

        // 2) URL param: ...?secret=XXXX
        int idx = content.indexOf("secret=");
        if (idx >= 0) {
            String s = content.substring(idx + "secret=".length());
            int amp = s.indexOf('&');
            return (amp > 0 ? s.substring(0, amp) : s).trim();
        }

        // 3) DÜZ SECRET (fallback)
        if (content.length() >= 8) {
            return content;
        }
        return null;
    }
    private static class Parsed {
        final long courseId;
        final String secret;
        Parsed(long c, String s){ this.courseId = c; this.secret = s; }
    }

    @Nullable
    private Parsed parseCourseIdAndSecret(String content) {
        if (content != null && content.startsWith("ATT|")) {
            String[] parts = content.split("\\|");
            if (parts.length >= 3) {
                try {
                    long cId = Long.parseLong(parts[1]);
                    String sec = parts[2] != null ? parts[2].trim() : null;
                    if (sec != null && !sec.isEmpty()) {
                        return new Parsed(cId, sec);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (content != null) {
            String lower = content.toLowerCase();
            int iCourse = lower.indexOf("courseid=");
            int iSecret = lower.indexOf("secret=");
            if (iCourse >= 0 && iSecret >= 0) {
                try {
                    String cPart = content.substring(iCourse + "courseId=".length());
                    int amp = cPart.indexOf('&');
                    String cStr = amp >= 0 ? cPart.substring(0, amp) : cPart;
                    long cId = Long.parseLong(cStr.trim());

                    String sPart = content.substring(iSecret + "secret=".length());
                    int amp2 = sPart.indexOf('&');
                    String sec = (amp2 >= 0 ? sPart.substring(0, amp2) : sPart).trim();

                    if (!sec.isEmpty()) return new Parsed(cId, sec);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

}
