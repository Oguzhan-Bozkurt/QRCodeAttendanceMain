package com.example.qrkodlayoklama.ui.attendance;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AttendancePdf {

    public static class GeneralRow {
        public final long studentNo;
        public final String fullName;
        public final int totalSessions;
        public final int present;
        public final int absent;

        public GeneralRow(long studentNo,
                          String fullName,
                          int totalSessions,
                          int present,
                          int absent) {
            this.studentNo = studentNo;
            this.fullName = fullName;
            this.totalSessions = totalSessions;
            this.present = present;
            this.absent = absent;
        }
    }

    public static class WeeklyRow {
        public final long studentNo;
        public final String fullName;
        public final boolean[] presentWeeks;

        public WeeklyRow(long studentNo, String fullName, boolean[] presentWeeks) {
            this.studentNo = studentNo;
            this.fullName = fullName;
            this.presentWeeks = presentWeeks;
        }
    }

    private AttendancePdf() { }

    public static boolean generateGeneral(Context ctx,
                                          String courseName,
                                          String courseCode,
                                          int totalSessions,
                                          List<GeneralRow> rows) {

        PdfDocument pdf = new PdfDocument();

        final int pageWidth = 595;
        final int pageHeight = 842;
        final int margin = 40;

        Paint titlePaint = new Paint();
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(18);

        Paint subTitlePaint = new Paint();
        subTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        subTitlePaint.setTextSize(12);

        Paint headerPaint = new Paint();
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextSize(12);

        Paint cellPaint = new Paint();
        cellPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        cellPaint.setTextSize(11);

        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = margin;

        canvas.drawText("GENEL YOKLAMA ÇİZELGESİ", margin, y, titlePaint);
        y += 28;

        String courseLabel = "Ders: " + courseName + " (" + courseCode + ")";
        canvas.drawText(courseLabel, margin, y, subTitlePaint);
        y += 20;

        String totalLabel = "Toplam oturum sayısı: " + totalSessions;
        canvas.drawText(totalLabel, margin, y, subTitlePaint);
        y += 24;

        float colNoWidth        = 35f;
        float colStudentNoWidth = 110f;
        float colNameWidth      = 220f;
        float colPresentWidth   = 90f;
        float colAbsentWidth    = 90f;

        float x = margin;
        canvas.drawText("No", x, y, headerPaint);            x += colNoWidth;
        canvas.drawText("Öğrenci No", x, y, headerPaint);    x += colStudentNoWidth;
        canvas.drawText("Adı Soyadı", x, y, headerPaint);    x += colNameWidth;
        canvas.drawText("Katıldığı", x, y, headerPaint);     x += colPresentWidth;
        canvas.drawText("Katılmadığı", x, y, headerPaint);

        y += 6;
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
        y += 16;

        int rowIndex = 1;
        for (GeneralRow row : rows) {
            if (y > pageHeight - margin) {
                pdf.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                page = pdf.startPage(pageInfo);
                canvas = page.getCanvas();
                y = margin;
                x = margin;

                canvas.drawText("No", x, y, headerPaint);            x += colNoWidth;
                canvas.drawText("Öğrenci No", x, y, headerPaint);    x += colStudentNoWidth;
                canvas.drawText("Adı Soyadı", x, y, headerPaint);    x += colNameWidth;
                canvas.drawText("Katıldığı", x, y, headerPaint);     x += colPresentWidth;
                canvas.drawText("Katılmadığı", x, y, headerPaint);

                y += 6;
                canvas.drawLine(margin, y, pageWidth - margin, y, linePaint);
                y += 16;
            }

            x = margin;
            canvas.drawText(String.valueOf(rowIndex), x, y, cellPaint);          x += colNoWidth;
            canvas.drawText(String.valueOf(row.studentNo), x, y, cellPaint);     x += colStudentNoWidth;
            canvas.drawText(row.fullName, x, y, cellPaint);                      x += colNameWidth;
            canvas.drawText(String.valueOf(row.present), x, y, cellPaint);       x += colPresentWidth;
            canvas.drawText(String.valueOf(row.absent), x, y, cellPaint);

            y += 18;
            rowIndex++;
        }

        pdf.finishPage(page);

        String safeCourse = (courseName == null ? "Ders" : courseName)
                .replaceAll("[^\\w\\-]+", "_");
        String ts = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                .format(new Date());
        String fileName = "GenelYoklama_" + safeCourse + "_" + ts + ".pdf";

        try {
            ContentResolver resolver = ctx.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                pdf.close();
                return false;
            }

            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) {
                    pdf.close();
                    return false;
                }
                pdf.writeTo(out);
                out.flush();
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(uri, values, null, null);

            pdf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            pdf.close();
            return false;
        }
    }

    public static boolean generateWeekly(Context ctx,
                                         String courseName,
                                         String courseCode,
                                         int totalSessions,
                                         List<WeeklyRow> rows) {

        if (totalSessions <= 0) totalSessions = 1;

        PdfDocument pdf = new PdfDocument();

        final int pageWidth = 842;
        final int pageHeight = 595;
        final int margin = 20;

        final int weeksPerPage = 14;

        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(16);

        Paint subTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        subTitlePaint.setTextSize(11);

        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headerPaint.setTextSize(10);

        Paint cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        cellPaint.setTextSize(10);

        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(1f);

        Paint checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(2.5f);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);

        final float colStudentNoW = 90f;
        final float colNameW = 150f;
        final float colWeekW = 40f;

        final float headerH = 40f;
        final float rowH = 22f;

        int pageNumber = 1;

        for (int weekStart = 0; weekStart < totalSessions; weekStart += weeksPerPage) {
            int weekEndExclusive = Math.min(totalSessions, weekStart + weeksPerPage);
            int visibleWeeks = weekEndExclusive - weekStart;

            int rowIndex = 0;
            while (rowIndex < rows.size()) {

                PdfDocument.PageInfo pageInfo =
                        new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                PdfDocument.Page page = pdf.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                float y = margin;

                canvas.drawText("HAFTALIK YOKLAMA ÇİZELGESİ", margin, y, titlePaint);
                y += 22;

                String courseLabel = "Ders: " + (courseName == null ? "" : courseName)
                        + " (" + (courseCode == null ? "" : courseCode) + ")";
                canvas.drawText(courseLabel, margin, y, subTitlePaint);
                y += 16;

                String totalLabel = "Toplam oturum sayısı: " + totalSessions;
                canvas.drawText(totalLabel, margin, y, subTitlePaint);
                y += 18;

                float tableLeft = margin;
                float tableTop = y;

                float tableWidth = colStudentNoW + colNameW + (visibleWeeks * colWeekW);

                float x = tableLeft;

                drawCellRect(canvas, x, tableTop, colStudentNoW, headerH, linePaint);
                drawTwoLineCentered(canvas, x, tableTop, colStudentNoW, headerH,
                        "Öğrenci", "No", headerPaint);

                x += colStudentNoW;

                drawCellRect(canvas, x, tableTop, colNameW, headerH, linePaint);
                drawTwoLineCentered(canvas, x, tableTop, colNameW, headerH,
                        "Ad", "Soyad", headerPaint);

                x += colNameW;

                for (int w = 0; w < visibleWeeks; w++) {
                    drawCellRect(canvas, x, tableTop, colWeekW, headerH, linePaint);
                    int weekNo = weekStart + w + 1;
                    drawTwoLineCentered(canvas, x, tableTop, colWeekW, headerH,
                            weekNo + ".", "Hafta", headerPaint);
                    x += colWeekW;
                }

                float curY = tableTop + headerH;

                while (rowIndex < rows.size()) {
                    if (curY + rowH > pageHeight - margin) break;

                    WeeklyRow r = rows.get(rowIndex);

                    float cx = tableLeft;

                    drawCellRect(canvas, cx, curY, colStudentNoW, rowH, linePaint);
                    drawTextLeftCenter(canvas, cx, curY, colStudentNoW, rowH,
                            String.valueOf(r.studentNo), cellPaint);
                    cx += colStudentNoW;

                    drawCellRect(canvas, cx, curY, colNameW, rowH, linePaint);
                    drawTextLeftCenter(canvas, cx, curY, colNameW, rowH,
                            r.fullName == null ? "" : r.fullName, cellPaint);
                    cx += colNameW;

                    for (int w = 0; w < visibleWeeks; w++) {
                        drawCellRect(canvas, cx, curY, colWeekW, rowH, linePaint);

                        int realWeekIndex = weekStart + w; // 0-based
                        boolean present = false;
                        if (r.presentWeeks != null && realWeekIndex >= 0 && realWeekIndex < r.presentWeeks.length) {
                            present = r.presentWeeks[realWeekIndex];
                        }
                        if (present) {
                            drawCheckMark(canvas, cx, curY, colWeekW, rowH, checkPaint);
                        }

                        cx += colWeekW;
                    }

                    curY += rowH;
                    rowIndex++;
                }

                pdf.finishPage(page);
                pageNumber++;
            }
        }

        String safeCourse = (courseName == null ? "Ders" : courseName)
                .replaceAll("[^\\w\\-]+", "_");
        String ts = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                .format(new Date());
        String fileName = "HaftalikYoklama_" + safeCourse + "_" + ts + ".pdf";

        try {
            ContentResolver resolver = ctx.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                pdf.close();
                return false;
            }

            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) {
                    pdf.close();
                    return false;
                }
                pdf.writeTo(out);
                out.flush();
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(uri, values, null, null);

            pdf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            pdf.close();
            return false;
        }
    }

    private static void drawCellRect(Canvas c, float x, float y, float w, float h, Paint strokePaint) {
        c.drawRect(x, y, x + w, y + h, strokePaint);
    }

    private static void drawTwoLineCentered(Canvas c,
                                            float x, float y, float w, float h,
                                            String line1, String line2,
                                            Paint p) {
        p.setTextAlign(Paint.Align.CENTER);
        float cx = x + (w / 2f);
        c.drawText(line1 == null ? "" : line1, cx, y + 16f, p);
        c.drawText(line2 == null ? "" : line2, cx, y + 32f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private static void drawTextLeftCenter(Canvas c,
                                           float x, float y, float w, float h,
                                           String text, Paint p) {
        p.setTextAlign(Paint.Align.LEFT);
        float tx = x + 6f;
        float ty = y + (h / 2f) + 4f;
        c.drawText(text == null ? "" : text, tx, ty, p);
    }

    private static void drawCheckMark(Canvas c, float x, float y, float w, float h, Paint p) {
        // basit tik: iki çizgi
        float x1 = x + w * 0.25f;
        float y1 = y + h * 0.55f;
        float x2 = x + w * 0.45f;
        float y2 = y + h * 0.75f;
        float x3 = x + w * 0.75f;
        float y3 = y + h * 0.30f;

        c.drawLine(x1, y1, x2, y2, p);
        c.drawLine(x2, y2, x3, y3, p);
    }
}
