package com.example.print_plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel;

public class PrintUtils {
    private static final String TAG = "PrintUtilsBaoanPrinter2";
    private static final String DEFAULT_SCAN_ACTION = "com.scanner.broadcast";
    private static final String DEFAULT_SCAN_DATA_KEY = "data";
    private static final String EXTRA_CODE_TYPE = "codetype";

    private static BroadcastReceiver scanReceiver;
    private static Context appContext;
    private static MethodChannel methodChannel;
    private static boolean couldUseScan;
    private static boolean receiverRegistered;
    private static String scanAction = DEFAULT_SCAN_ACTION;
    private static String scanDataKey = DEFAULT_SCAN_DATA_KEY;
    private static String lastError = "";

    public static boolean initPrintUtils(Context context, MethodChannel channel) {
        appContext = context == null ? null : context.getApplicationContext();
        methodChannel = channel;
        couldUseScan = appContext != null;
        registerScanReceiver();
        return false;
    }

    public static void printTest() {
        Log.w(TAG, "printTest is not implemented for baoanPrinter2.");
    }

    public static void printText(final int paperWidth, final int size, final int align,
                                 final String text, final boolean isLabel, final boolean tearPape) {
        Log.w(TAG, "printText is not implemented for baoanPrinter2.");
    }

    public static void labelEnable(boolean enable) {
        Log.w(TAG, "labelEnable is not implemented for baoanPrinter2.");
    }

    public static void openScan() {
        Log.w(TAG, "openScan is not supported for baoanPrinter2 broadcast-only scanner.");
    }

    public static void configureScanner(String inputScanAction, String inputScanDataKey) {
        scanAction = normalizeConfigValue(inputScanAction, scanAction);
        scanDataKey = normalizeConfigValue(inputScanDataKey, scanDataKey);
        restartScanReceiver();
    }

    public static Map<String, Object> getScannerStatus() {
        if (appContext != null && !receiverRegistered) {
            registerScanReceiver();
        }

        Map<String, Object> status = new HashMap<String, Object>();
        status.put("couldUseScan", couldUseScan);
        status.put("flavor", "baoanPrinter2");
        status.put("receiverRegistered", receiverRegistered);
        status.put("scanAction", scanAction);
        status.put("scanDataKey", scanDataKey);
        status.put("message", TextUtils.isEmpty(lastError)
                ? "baoanPrinter2 uses broadcast-only scanner implementation."
                : lastError);
        return status;
    }

    public static Boolean setScannerActive(boolean active) {
        Log.w(TAG, "setScannerActive is not supported for baoanPrinter2 broadcast-only scanner.");
        return null;
    }

    public static Boolean setScannerKeyEnabled(boolean enabled) {
        Log.w(TAG, "setScannerKeyEnabled is not supported for baoanPrinter2 broadcast-only scanner.");
        return null;
    }

    public static Boolean restoreScanner() {
        Log.w(TAG, "restoreScanner is not supported for baoanPrinter2 broadcast-only scanner.");
        return null;
    }

    public static Boolean startScan() {
        Log.w(TAG, "startScan is not supported for baoanPrinter2 broadcast-only scanner.");
        return null;
    }

    public static Boolean stopScan() {
        Log.w(TAG, "stopScan is not supported for baoanPrinter2 broadcast-only scanner.");
        return null;
    }

    public static void dispose() {
        unregisterScanReceiver();
        appContext = null;
        methodChannel = null;
        couldUseScan = false;
    }

    private static void registerScanReceiver() {
        if (appContext == null || receiverRegistered) {
            return;
        }

        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !scanAction.equals(intent.getAction())) {
                    return;
                }

                String usedDataKey = scanDataKey;
                String code = readExtraAsString(intent, usedDataKey);
                if (code == null && !DEFAULT_SCAN_DATA_KEY.equals(usedDataKey)) {
                    usedDataKey = DEFAULT_SCAN_DATA_KEY;
                    code = readExtraAsString(intent, usedDataKey);
                }

                Log.d(TAG, "scan result: " + code + ", action: " + intent.getAction()
                        + ", dataKey: " + usedDataKey);

                if (code != null && methodChannel != null) {
                    Map<String, Object> payload = new HashMap<String, Object>();
                    payload.put("message", code);
                    payload.put("action", intent.getAction());
                    payload.put("dataKey", usedDataKey);
                    payload.put("flavor", "baoanPrinter2");
                    payload.put("codeType", readExtraAsString(intent, EXTRA_CODE_TYPE));
                    payload.put("extras", collectExtras(intent));
                    methodChannel.invokeMethod("onBroadcastReceived", payload);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(scanAction);

        try {
            if (Build.VERSION.SDK_INT >= 33) {
                appContext.registerReceiver(scanReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                appContext.registerReceiver(scanReceiver, filter);
            }
            receiverRegistered = true;
            couldUseScan = true;
            lastError = "";
        } catch (Throwable e) {
            receiverRegistered = false;
            couldUseScan = false;
            scanReceiver = null;
            lastError = safeErrorText(e);
            Log.e(TAG, "registerScanReceiver failed", e);
        }
    }

    private static void restartScanReceiver() {
        unregisterScanReceiver();
        registerScanReceiver();
    }

    private static void unregisterScanReceiver() {
        if (!receiverRegistered || appContext == null || scanReceiver == null) {
            return;
        }
        try {
            appContext.unregisterReceiver(scanReceiver);
        } catch (Throwable e) {
            lastError = safeErrorText(e);
            Log.e(TAG, "unregisterScanReceiver failed", e);
        } finally {
            receiverRegistered = false;
            scanReceiver = null;
        }
    }

    private static String readExtraAsString(Intent intent, String key) {
        if (intent == null || TextUtils.isEmpty(key)) {
            return null;
        }
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(key)) {
            return null;
        }
        Object value = extras.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value);
        }
        return String.valueOf(value);
    }

    private static Map<String, Object> collectExtras(Intent intent) {
        Map<String, Object> extrasMap = new HashMap<String, Object>();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return extrasMap;
        }

        Set<String> keys = extras.keySet();
        for (String key : keys) {
            Object value = extras.get(key);
            extrasMap.put(key, toSupportedExtraValue(value));
        }
        return extrasMap;
    }

    private static Object toSupportedExtraValue(Object value) {
        if (value == null
                || value instanceof String
                || value instanceof Boolean
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Double
                || value instanceof byte[]
                || value instanceof int[]
                || value instanceof long[]
                || value instanceof double[]) {
            return value;
        }
        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }
        if (value instanceof Short) {
            return ((Short) value).intValue();
        }
        if (value instanceof Byte) {
            return ((Byte) value).intValue();
        }
        return String.valueOf(value);
    }

    private static String normalizeConfigValue(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return TextUtils.isEmpty(trimmed) ? fallback : trimmed;
    }

    private static String safeErrorText(Throwable throwable) {
        String message = throwable.getMessage();
        if (!TextUtils.isEmpty(message)) {
            return message;
        }
        return throwable.getClass().getSimpleName();
    }

    public static Bitmap qrCordAsBitmap(int size, int paperWidth, int paperHeight,
                                        final String qrCodeStr, int lineSpacing) {
        return createPlaceholderBitmap(paperWidth, paperHeight, "baoanPrinter2 QR not implemented");
    }

    public static Bitmap textAsBitmap(int size, int paperWidth, int paperHeight,
                                      final String qrCodeStr, String text,
                                      int lineSpacing, Double inputWidth) {
        return createPlaceholderBitmap(paperWidth, paperHeight, text);
    }

    public static void printTextAsBitmap(final int align, final Bitmap bitmap,
                                         final boolean isLabel, final boolean tearPape) {
        Log.w(TAG, "printTextAsBitmap is not implemented for baoanPrinter2.");
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
        float y = 12 - paint.getFontMetrics().ascent;
        canvas.drawText(text == null ? "" : text, 12, y, paint);
        return bitmap;
    }
}
