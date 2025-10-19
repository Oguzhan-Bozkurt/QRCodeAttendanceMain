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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScanActivity extends AppCompatActivity {

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
                Toast.makeText(this, "QR: " + content, Toast.LENGTH_SHORT).show(); // Deneme için
                if (content == null || content.isEmpty()) {
                    Toast.makeText(this, "Boş QR", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Beklenen format: ATT|<courseId>|<secret>
                String secret = parseSecret(content);
                Toast.makeText(this, "SECRET: " + secret, Toast.LENGTH_SHORT).show(); // Deneme için
                if (secret == null) {
                    Toast.makeText(this, "Geçersiz QR", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                secret = secret.trim();

                // Sunucuya yoklama işaretle
                ApiClient.attendance().mark(new MarkRequest(secret)).enqueue(new Callback<ResponseBody>() {
                    @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                        String msg;
                        if (resp.isSuccessful()) {
                            msg = "Yoklama gönderildi";
                        } else {
                            switch (resp.code()) {
                                case 404:
                                    msg = "Aktif yoklama bulunamadı ya da kod süresi dolmuş.";
                                    break;
                                case 409:
                                    msg = "Bu oturuma daha önce yoklama vermişsiniz.";
                                    break;
                                case 400:
                                    msg = "Geçersiz QR kodu.";
                                    break;
                                case 401:
                                    msg = "Oturumunuzun süresi dolmuş olabilir. Lütfen tekrar giriş yapın.";
                                    break;
                                default:
                                    msg = "Hata: " + resp.code();
                            }
                        }
                        Toast.makeText(QrScanActivity.this, msg, Toast.LENGTH_LONG).show();
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

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kamera izni var mı?
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

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
        content = content.trim(); // <--- çok önemli

        // 1) ATT|<courseId>|<secret>
        if (content.startsWith("ATT|")) {
            String[] parts = content.split("\\|");
            if (parts.length >= 3) return parts[2].trim(); // <--- güvenli
        }

        // 2) URL param: ...?secret=XXXX
        int idx = content.indexOf("secret=");
        if (idx >= 0) {
            String s = content.substring(idx + "secret=".length());
            int amp = s.indexOf('&');
            return (amp > 0 ? s.substring(0, amp) : s).trim(); // <--- güvenli
        }

        // 3) DÜZ SECRET (fallback)
        // Bazı QR üreticileri düz metin verebilir; destekleyelim:
        if (content.length() >= 8) { // çok kısa saçma içerikleri ele
            return content;
        }
        return null;
    }


}
