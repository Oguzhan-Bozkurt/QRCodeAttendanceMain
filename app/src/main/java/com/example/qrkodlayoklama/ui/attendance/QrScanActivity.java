package com.example.qrkodlayoklama.ui.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
                if (content == null || content.isEmpty()) {
                    Toast.makeText(this, "Boş QR", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Beklenen format: ATT|<courseId>|<secret>
                String secret = parseSecret(content);
                if (secret == null) {
                    Toast.makeText(this, "Geçersiz QR", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Sunucuya yoklama işaretle
                ApiClient.attendance().mark(new MarkRequest(secret)).enqueue(new Callback<ResponseBody>() {
                    @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                        if (resp.isSuccessful()) {
                            Toast.makeText(QrScanActivity.this, "Yoklama gönderildi", Toast.LENGTH_SHORT).show();
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
        // 1) ATT|<courseId>|<secret>
        if (content.startsWith("ATT|")) {
            String[] parts = content.split("\\|");
            if (parts.length >= 3) return parts[2];
        }
        // 2) Alternatif: URL içeriyorsa "...?secret=XXXX"
        int idx = content.indexOf("secret=");
        if (idx >= 0) {
            String s = content.substring(idx + "secret=".length());
            int amp = s.indexOf('&');
            return amp > 0 ? s.substring(0, amp) : s;
        }
        return null;
    }
}
