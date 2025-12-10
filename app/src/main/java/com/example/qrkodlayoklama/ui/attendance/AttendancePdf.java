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

import com.example.qrkodlayoklama.data.remote.model.CourseDto;

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

    private AttendancePdf() {
        // static util
    }

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
        canvas.drawText("Katılmadığı", x, y, headerPaint);   // x += colAbsentWidth;

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
}
