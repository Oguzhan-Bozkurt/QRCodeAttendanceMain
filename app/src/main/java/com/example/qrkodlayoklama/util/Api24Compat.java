package com.example.qrkodlayoklama.util;

import android.annotation.SuppressLint;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.time.Instant;


public final class Api24Compat {
    private Api24Compat() {}

    @SuppressLint("NewApi")
    public static Instant parseIsoInstant(String iso) throws Exception {
        if (iso == null || iso.isEmpty()) throw new IllegalArgumentException("empty iso");

        if (Build.VERSION.SDK_INT >= 26) {
            return Instant.parse(iso);
        } else {
            String t = iso;
            int dot = iso.indexOf('.');
            int z   = iso.indexOf('Z', dot);
            if (dot > 0 && z > 0) {
                String frac = iso.substring(dot + 1, z);
                if (frac.length() > 3) frac = frac.substring(0, 3);
                t = iso.substring(0, dot + 1) + frac + "Z";
            }

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(t);
            if (d == null) throw new IllegalArgumentException("parse failed: " + iso);
            return Instant.ofEpochMilli(d.getTime());
        }
    }
}
