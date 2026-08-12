package com.example.print_plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.text.TextPaint;
import android.util.Log;

import io.flutter.plugin.common.MethodChannel;

public class PrintUtils {
    private static final String TAG = "PrintUtilsBaoanPrinter";
    private static final String SCANNER_ACTION = "com.scanner.broadcast";
    private static final String SCANNER_DATA_KEY = "data";
    private static BroadcastReceiver scanReceiver;
    private static Context appContext;
    private static MethodChannel methodChannel;

    public static boolean initPrintUtils(Context context, MethodChannel channel) {
        appContext = context.getApplicationContext();
        methodChannel = channel;
        registerScanReceiver();
        return true;
    }

    public static void printTest() {
        Log.w(TAG, "printTest is not implemented for baoanPrinter.");
    }

    public static void printText(final int paperWidth, final int size, final int align,
                                 final String text, final boolean isLabel, final boolean tearPape) {
        Log.w(TAG, "printText is not implemented for baoanPrinter.");
    }

    public static void labelEnable(boolean enable) {
        Log.w(TAG, "labelEnable is not implemented for baoanPrinter.");
    }

    public static void openScan() {
        Log.w(TAG, "openScan is controlled by the PDA scanner service for baoanPrinter.");
    }

    private static void registerScanReceiver() {
        if (appContext == null || scanReceiver != null) {
            return;
        }

        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !SCANNER_ACTION.equals(intent.getAction())) {
                    return;
                }

                String code = intent.getStringExtra(SCANNER_DATA_KEY);
                Log.d(TAG, "scan result: " + code);

                if (code != null && methodChannel != null) {
                    methodChannel.invokeMethod("onBroadcastReceived", code);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SCANNER_ACTION);
        if (Build.VERSION.SDK_INT >= 33) {
            appContext.registerReceiver(scanReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            appContext.registerReceiver(scanReceiver, filter);
        }
    }

    public static Bitmap qrCordAsBitmap(int size, int paperWidth, int paperHeight,
                                        final String qrCodeStr, int lineSpacing) {
        return createPlaceholderBitmap(paperWidth, paperHeight, "baoanPrinter QR not implemented");
    }

    public static Bitmap textAsBitmap(int size, int paperWidth, int paperHeight,
                                      final String qrCodeStr, String text,
                                      int lineSpacing, Double inputWidth) {
        return createPlaceholderBitmap(paperWidth, paperHeight, text);
    }

    public static void printTextAsBitmap(final int align, final Bitmap bitmap,
                                         final boolean isLabel, final boolean tearPape) {
        Log.w(TAG, "printTextAsBitmap is not implemented for baoanPrinter.");
    }

    private static Bitmap createPlaceholderBitmap(int paperWidth, int paperHeight, String text) {
        int width = Math.max(1, paperWidth * 8);
        int height = Math.max(1, paperHeight * 8);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        TextPaint paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(24);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float y = 12 - fontMetrics.ascent;
        canvas.drawText(text == null ? "" : text, 12, y, paint);
        return bitmap;
    }
}
