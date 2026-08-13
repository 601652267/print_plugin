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
import android.text.TextUtils;
import android.util.Log;

import com.android.scanner.impl.ReaderManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

public class PrintUtils {
    private static final String TAG = "PrintUtilsBaoanPrinter";
    private static final String DEFAULT_SCAN_ACTION = "com.scanner.broadcast";
    private static final String DEFAULT_SCAN_DATA_KEY = "data";
    private static final String LEGACY_SCAN_ACTION = "com.android.server.scannerservice.broadcast";
    private static final String LEGACY_SCAN_DATA_KEY = "scannerdata";
    private static final String EXTRA_CODE_TYPE = "codetype";
    private static final int OUTPUT_MODE_API = 2;
    private static final int END_CHAR_NONE = 3;

    private static BroadcastReceiver scanReceiver;
    private static Context appContext;
    private static MethodChannel methodChannel;
    private static ReaderManager readerManager;
    private static boolean couldUseScan;
    private static boolean receiverRegistered;
    private static boolean capturedOriginalSettings;
    private static String scanAction = DEFAULT_SCAN_ACTION;
    private static String scanDataKey = DEFAULT_SCAN_DATA_KEY;
    private static boolean originalActive;
    private static boolean originalScanKeyEnabled;
    private static int originalOutputMode = OUTPUT_MODE_API;
    private static int originalEndCharMode = END_CHAR_NONE;

    public static boolean initPrintUtils(Context context, MethodChannel channel) {
        appContext = context.getApplicationContext();
        methodChannel = channel;
        initScanner();
        registerScanReceiver();
        return couldUseScan;
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
        startScan();
    }

    public static void configureScanner(String inputScanAction, String inputScanDataKey) {
        scanAction = normalizeConfigValue(inputScanAction, scanAction);
        scanDataKey = normalizeConfigValue(inputScanDataKey, scanDataKey);
        restartScanReceiver();
    }

    public static Map<String, Object> getScannerStatus() {
        if (!couldUseScan || readerManager == null || !hasBoundScannerService(readerManager)) {
            initScanner();
        }

        Map<String, Object> status = new HashMap<String, Object>();
        status.put("couldUseScan", couldUseScan);
        status.put("flavor", "baoanPrinter");
        status.put("scanAction", scanAction);
        status.put("scanDataKey", scanDataKey);

        if (!couldUseScan || readerManager == null) {
            status.put("message", "Baoan scanner service is not available.");
            return status;
        }

        try {
            status.put("binderBound", hasBoundScannerService(readerManager));
            status.put("serviceRunning", appContext != null && readerManager.isServiceRunning(appContext));
            status.put("active", readerManager.GetActive());
            status.put("scanKeyEnabled", readerManager.isEnableScankey());
            status.put("outputMode", readerManager.getOutPutMode());
            status.put("endCharMode", readerManager.getEndCharMode());
            status.put("scannerModel", safeText(ReaderManager.getScannerModel()));
            status.put("scannerType", safeText(ReaderManager.getScannertype()));
        } catch (Throwable e) {
            Log.e(TAG, "getScannerStatus failed", e);
            status.put("message", safeErrorText(e));
            if (!hasBoundScannerService(readerManager)) {
                releaseReaderManagerOnly();
                status.put("couldUseScan", false);
            }
        }
        return status;
    }

    public static Boolean setScannerActive(boolean active) {
        if (!ensureScannerUsable()) {
            return false;
        }
        try {
            return readerManager.SetActive(active);
        } catch (Throwable e) {
            Log.e(TAG, "setScannerActive failed", e);
            return false;
        }
    }

    public static Boolean setScannerKeyEnabled(boolean enabled) {
        if (!ensureScannerUsable()) {
            return false;
        }
        try {
            readerManager.setEnableScankey(enabled);
            return readerManager.isEnableScankey() == enabled;
        } catch (Throwable e) {
            Log.e(TAG, "setScannerKeyEnabled failed", e);
            return false;
        }
    }

    public static Boolean restoreScanner() {
        if (!ensureScannerUsable()) {
            return false;
        }
        try {
            readerManager.SetActive(true);
            readerManager.setEnableScankey(true);
            readerManager.setOutPutMode(OUTPUT_MODE_API);
            readerManager.setEndCharMode(END_CHAR_NONE);
            return readerManager.GetActive() && readerManager.isEnableScankey();
        } catch (Throwable e) {
            Log.e(TAG, "restoreScanner failed", e);
            return false;
        }
    }

    public static Boolean startScan() {
        if (!ensureScannerUsable()) {
            return false;
        }
        try {
            if (!readerManager.GetActive()) {
                readerManager.SetActive(true);
            }
            readerManager.setOutPutMode(OUTPUT_MODE_API);
            readerManager.setEndCharMode(END_CHAR_NONE);
            return readerManager.beginScanAndDeocde();
        } catch (Throwable e) {
            Log.e(TAG, "startScan failed", e);
            return false;
        }
    }

    public static Boolean stopScan() {
        if (!ensureScannerUsable()) {
            return false;
        }
        try {
            return readerManager.stopScanAndDecode();
        } catch (Throwable e) {
            Log.e(TAG, "stopScan failed", e);
            return false;
        }
    }

    public static void dispose() {
        unregisterScanReceiver();
        restoreOriginalScannerSettings();
    }

    private static void initScanner() {
        try {
            couldUseScan = false;
            capturedOriginalSettings = false;
            readerManager = ReaderManager.getInstance();
            if (readerManager == null) {
                Log.w(TAG, "ReaderManager init failed.");
                return;
            }

            if (!hasBoundScannerService(readerManager)) {
                releaseReaderManagerOnly();
                Log.w(TAG, "Baoan scanner binder service is not bound.");
                return;
            }

            couldUseScan = true;
            try {
                originalActive = readerManager.GetActive();
                originalOutputMode = readerManager.getOutPutMode();
                originalEndCharMode = readerManager.getEndCharMode();
                originalScanKeyEnabled = readerManager.isEnableScankey();
                capturedOriginalSettings = true;
            } catch (Throwable e) {
                Log.e(TAG, "read original scanner settings failed", e);
            }

            restoreScanner();
        } catch (Throwable e) {
            Log.e(TAG, "initScanner failed", e);
            releaseReaderManagerOnly();
        }
    }

    private static boolean ensureScannerUsable() {
        if (couldUseScan && readerManager != null && hasBoundScannerService(readerManager)) {
            return true;
        }
        initScanner();
        return couldUseScan && readerManager != null;
    }

    private static void registerScanReceiver() {
        if (appContext == null || receiverRegistered) {
            return;
        }

        scanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }

                String action = intent.getAction();
                if (!scanAction.equals(action) && !LEGACY_SCAN_ACTION.equals(action)) {
                    return;
                }

                String dataKey = scanAction.equals(action) ? scanDataKey : LEGACY_SCAN_DATA_KEY;
                String code = intent.getStringExtra(dataKey);
                if (code == null && !LEGACY_SCAN_DATA_KEY.equals(dataKey)) {
                    code = intent.getStringExtra(LEGACY_SCAN_DATA_KEY);
                }
                if (code == null && !DEFAULT_SCAN_DATA_KEY.equals(dataKey)) {
                    code = intent.getStringExtra(DEFAULT_SCAN_DATA_KEY);
                }
                String codeType = intent.getStringExtra(EXTRA_CODE_TYPE);

                Log.d(TAG, "scan result: " + code + ", codeType: " + codeType + ", action: " + action);

                if (code != null && methodChannel != null) {
                    Map<String, Object> payload = new HashMap<String, Object>();
                    payload.put("message", code);
                    payload.put("codeType", codeType);
                    payload.put("action", action);
                    payload.put("dataKey", dataKey);
                    methodChannel.invokeMethod("onBroadcastReceived", payload);
                }

                if (couldUseScan && readerManager != null) {
                    try {
                        readerManager.stopScanAndDecode();
                    } catch (Throwable e) {
                        Log.e(TAG, "stopScanAndDecode after receive failed", e);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(scanAction);
        if (!LEGACY_SCAN_ACTION.equals(scanAction)) {
            filter.addAction(LEGACY_SCAN_ACTION);
        }
        if (Build.VERSION.SDK_INT >= 33) {
            appContext.registerReceiver(scanReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            appContext.registerReceiver(scanReceiver, filter);
        }
        receiverRegistered = true;
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
            Log.e(TAG, "unregisterScanReceiver failed", e);
        } finally {
            receiverRegistered = false;
            scanReceiver = null;
        }
    }

    private static boolean hasBoundScannerService(ReaderManager manager) {
        if (manager == null) {
            return false;
        }

        Class<?> currentClass = manager.getClass();
        while (currentClass != null) {
            try {
                Field serviceField = currentClass.getDeclaredField("mService");
                serviceField.setAccessible(true);
                return serviceField.get(null) != null;
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            } catch (Throwable e) {
                Log.e(TAG, "check scanner binder service failed", e);
                return false;
            }
        }
        return false;
    }

    private static void restoreOriginalScannerSettings() {
        if (readerManager == null) {
            return;
        }
        try {
            if (couldUseScan && capturedOriginalSettings) {
                readerManager.stopScanAndDecode();
                readerManager.setOutPutMode(originalOutputMode);
                readerManager.setEndCharMode(originalEndCharMode);
                readerManager.setEnableScankey(originalScanKeyEnabled);
                readerManager.SetActive(originalActive);
            }
            readerManager.Release();
        } catch (Throwable e) {
            Log.e(TAG, "restoreOriginalScannerSettings failed", e);
        } finally {
            readerManager = null;
            couldUseScan = false;
            capturedOriginalSettings = false;
        }
    }

    private static void releaseReaderManagerOnly() {
        if (readerManager == null) {
            return;
        }
        try {
            readerManager.Release();
        } catch (Throwable e) {
            Log.e(TAG, "releaseReaderManagerOnly failed", e);
        } finally {
            readerManager = null;
        }
    }

    private static String normalizeConfigValue(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return TextUtils.isEmpty(trimmed) ? fallback : trimmed;
    }

    private static String safeText(String value) {
        return TextUtils.isEmpty(value) ? "" : value;
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
