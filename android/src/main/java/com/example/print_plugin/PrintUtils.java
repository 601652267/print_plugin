package com.example.print_plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.qs.wiget.BitmapDeleteNoUseSpaceUtil;
import com.example.print_plugin.R;
import com.example.print_plugin.ICommService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android_serialport_api.MyApp;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortHelper;

import java.util.Map;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import org.json.JSONException;

/**
 * 请注意，整个代码中均只能使用这里的mCommonApi变量， 不能另外再重复实例化mCommonApi，否则将会出现打印延迟或者不打印，
 * 扫描会扫描二次或者不出扫描光的情况，请注意
 *
 * @author wsl
 */
public class PrintUtils {

    public static Context context;

    public static boolean isCanprint = false;

    public static boolean isCanSend = true;

    public static boolean temHigh = false;

    private static boolean isOpen = true;
    private final static int NOPAPER = -1;
    private final static int SHOW_RECV_DATA2 = 2;
    private final static int SHOW_RECV_DATA = 1;
    private static byte[] recv;
    // private static String strRead;
    public static boolean isCanScan = true;
    // GreenOnReceiver greenOnReceiver;

    // 扫描信息解码方式
    public static String decode = "GBK";

    public static StringBuffer sb1 = new StringBuffer();

    static Handler handler1 = new Handler();

    static int barNum = 0;
    static int sendNum = 0;
    NumberFormat numberFormat = NumberFormat.getInstance();
    static ArrayList<byte[]> list = new ArrayList<byte[]>();

    static ReadThread mReadThread;

    static Handler h;

    // 串口连接方式
    public static SerialPort mSerialPort;
    public static OutputStream mOutputStream;
    public static InputStream mInputStream;
    SerialPortHelper serialport = new SerialPortHelper();
    public static MyApp mApplication;
    public static Bitmap mBitmap_write;


    public static boolean initPrintUtils(Context context1) {

        //控制GPIO口给单片机上电
        StartTestService(context1);

        context = context1;

        mApplication = new MyApp();

        try {
            mSerialPort = mApplication.getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (InvalidParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        // 初始
        init();

        mReadThread = new ReadThread();
        mReadThread.start();

        mBitmap_write = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.write);
        return true;
    }

    public static void init() {

        openGPIO();
        initGPIO();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub

                open();
                // 开启一票一控
                send(new byte[]{0x1b, 0x23, 0x23, 0x46, 0x54, 0x4B, 0x54,
                        0x31});
                // //设为中文
                send(new byte[]{0x1b, 0x23, 0x23, 0x53, 0x4c, 0x41, 0x4e,
                        0x0F});
                // 设为codepage
                send(new byte[]{0x1b, 0x23, 0x23, 0x43, 0x44, 0x54, 0x59,
                        0x03});

            }
        }, 2000);

        // 利用Handler更新UI
        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x123) {
                    if (msg.obj != null) {
                        String str = "" + msg.obj;
                        if (!str.trim().contains("##56")) {
                            if (!str.trim().contains("55DN")) {
                                if (!str.trim().contains("##55")) {
                                    if (!str.trim().equals("start")) {
                                        if (!str.trim().equals("BE ")) {
                                            if (!str.trim().equals("P")) {
                                                if (!str.trim().contains("褄鳰")) {
                                                    if (!str.trim().contains("褄")) {
                                                        if (!str.trim().contains(
                                                                "鳰")) {
                                                            if (!str.trim()
                                                                    .contains(
                                                                            "find bl end err")) {
                                                                if (!str.trim()
                                                                        .equals("BE")) {
                                                                    if (!str.trim()
                                                                            .equals("11 ")) {
                                                                        if (!str.trim()
                                                                                .equals("MM")) {

                                                                            str = str
                                                                                    .replace(
                                                                                            "�4",
                                                                                            "");
                                                                            str = str
                                                                                    .replace(
                                                                                            "�1",
                                                                                            "");
                                                                            // str=str.replace(" ",
                                                                            // "");
                                                                            str = str
                                                                                    .replace(
                                                                                            "�",
                                                                                            "");

                                                                            Intent intentBroadcast = new Intent();
                                                                            intentBroadcast
                                                                                    .setAction("com.qs.scancode");
                                                                            intentBroadcast
                                                                                    .putExtra(
                                                                                            "data",
                                                                                            str);

                                                                            context.sendBroadcast(intentBroadcast);

                                                                            // 清空数据
                                                                            sb1.setLength(0);

                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        };

    }

    static String str1, str2;
    static Message msg;

    static class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isOpen) {
                int size;

                try {

                    int count = 0;
                    while (count == 0) {
                        count = mInputStream.available();
                    }
                    byte[] buffer = new byte[count];
                    size = mInputStream.read(buffer);

                    if (size > 0) {
                        str2 = "-1";

                        recv = new byte[size];

                        System.arraycopy(buffer, 0, recv, 0, size);

                        buffer = recv;

//						Log.e("", "字符串数据:" + str2);

                        str1 = byteToString(buffer, buffer.length);

                        isCanprint = true;

                        msg = handler.obtainMessage(SHOW_RECV_DATA);
                        msg.obj = str1;
                        msg.sendToTarget();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("", "返回数据4");
                    return;
                }
            }

        }
    }

    StringBuffer sb = new StringBuffer();

    private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RECV_DATA:
                    String barCodeStr1 = (String) msg.obj;
                    Log.e("", "1read success:" + barCodeStr1);
                    if (barCodeStr1.trim() != "") {
                        if (isOpen) {
                            if (!barCodeStr1.trim().contains("##55")) {
                                if (!barCodeStr1.trim().equals("start")) {
                                    if (barCodeStr1.trim().length() != 0) {

                                        sb1.append(barCodeStr1);
                                        num = 1;
                                        mHanlder.removeCallbacks(run_getData);
                                        mHanlder.post(run_getData);

                                    }
                                }
                            }
                        }
                    }
                    break;
                case NOPAPER:

                    Intent mIntent = new Intent("com.qs.getmassage");
                    mIntent.putExtra("message", 1);
                    context.sendBroadcast(mIntent);

                    break;
                case SHOW_RECV_DATA2:

                    Log.e("str2", "str2====");

                    Intent mIntent2 = new Intent("com.qs.getmassage");
                    mIntent2.putExtra("message", 0);
                    context.sendBroadcast(mIntent2);

                    break;
            }
        }

        ;
    };

    static int num = 1;
    static String strRead;
    static Handler mHanlder = new Handler();
    static Runnable run_getData = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (num > 1) {
                num = 1;
                mHanlder.removeCallbacks(run_getData);
                Message m = new Message();
                m.what = 0x123;
                if (sb1.toString().contains("14 00 0C 0F")) {

                    isCanprint = false;
                    msg = handler.obtainMessage(NOPAPER);
                    msg.sendToTarget();
                    Log.e("str2", "str3=缺纸");
                } else if (sb1.toString().contains("11")) {
                } else if (sb1.toString().equals("00 ")) {
                } else {

                    isCanprint = true;
                    Log.e("str2", "str2=" + sb1.toString());
                    if (sb1.toString().contains("7A F8 ")) {
                        msg = handler.obtainMessage(SHOW_RECV_DATA2);
                        msg.sendToTarget();
                        Log.e("str2", "str2：打印成功");
                    } else {

                        if (sb1.toString().length() <= 0) {
                            return;
                        }

                        byte[] bt1 = hexStringToBytes(sb1.toString());

                        try {
                            // strRead = new String(bt1, decode);
                            strRead = new String(bt1, "GBK");
                            // 如果有乱码，则说明编码不对，切换成UTF-8
                            if (strRead.contains("�")) {
                                strRead = new String(bt1, "UTF-8");
                            }

                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        m.obj = strRead;
                    }
                    // m.obj = sb1.toString();
                    h.sendMessage(m);
                }
                sb1.setLength(0);
            } else {

                num++;
                mHanlder.postDelayed(run_getData, 200);

            }
        }
    };

    // 拉高扫描头电源
    public static void open() {

        PrintUtils.send(new byte[]{0x1B, 0x23, 0x23, 0x35, 0x36, 0x55, 0x50});

    }

    // 执行扫描
    public static void openScan() {

        try {
            //给扫描头上电
            mICommService.setGpioOut(Integer.parseInt("167"), 1);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    // 扫描指令
                    PrintUtils.send(new byte[]{0x1B, 0x23, 0x23, 0x35, 0x35,
                            0x44, 0x4E});

                    // 清空数据
                    sb1.setLength(0);
                }
            }, 100);

        } catch (Exception e) {

        }


    }

    public static void initGPIO() {
        // TODO Auto-generated method stub
    }

    public static void openGPIO() {
    }

    static StringBuffer buf;
    static byte high, low;
    static byte maskHigh = (byte) 0xf0;
    static byte maskLow = 0x0f;

    public static String byteToString(byte[] b, int size) {

        buf = new StringBuffer();

        for (int i = 0; i < size; i++) {
            high = (byte) ((b[i] & maskHigh) >> 4);
            low = (byte) (b[i] & maskLow);
            buf.append(findHex(high));
            buf.append(findHex(low));
            buf.append(" ");
        }
        return buf.toString();

    }

    private static char findHex(byte b) {
        int t = new Byte(b).intValue();
        t = t < 0 ? t + 16 : t;
        if ((0 <= t) && (t <= 9)) {
            return (char) (t + '0');
        }
        return (char) (t - 10 + 'A');
    }

    /**
     * 查看一个字符串是否可以转换为数字
     *
     * @param str 字符串
     * @return true 可以; false 不可以
     */
    public static boolean isStr2Num(String str) {
        Pattern pattern = Pattern.compile("^[0-9]*$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 每次发送的最大字节
     */
//	public static int arraysNum = 1000;

    public static int arraysNum = 64;

    /**
     * 发送数据
     */
    public static void send(byte[] data) {
        if (data == null)
            return;
        // 大于1000字节分流发送
//		if (data.length > 1000) {
        // 分流发送，每次发送arraysNum个字节
        shuntSend(data);
//		} else {
//			// 小于1000字节直接发送
//			send1(data);
//
//		}
    }

    /**
     * 发送数据
     */
    public static void send1(byte[] data) {
        if (data == null)
            return;
        try {
            mOutputStream.write(data);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static byte[] bt;

    /**
     * 字节分流发送
     *
     * @param bt1
     */
    private static void shuntSend(byte[] bt1) {

        bt = bt1;
        // 字节数组分割，每次发送的最大字节默认为1000
        for (int i = 0; i < (bt.length / arraysNum + 1); i++) {
            if (arraysNum * (i + 1) >= bt.length) {
                list.add(Arrays.copyOfRange(bt, arraysNum * i, bt.length));
            } else {
                list.add(Arrays.copyOfRange(bt, arraysNum * i, arraysNum
                        * (i + 1)));
            }
        }

        barNum = list.size();

        // 启动发送线程
        handler.postDelayed(mRun_start, 10);
    }

    /**
     * 小端模式 将int转为低字节在前，高字节在后的byte数组
     *
     * @param n int
     * @return byte[]
     */
    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    /**
     * 将int数值转换为占四个字节的byte数组
     *
     * @param value 要转换的int值
     * @return byte数组
     */
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    static Runnable mRun_start = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (sendNum >= barNum) {

                sendNum = 0;
                // 发送完成清除list
                list.clear();
                // 发送更新至打印机
                handler.removeCallbacks(mRun_start);

            } else {

                try {
                    // 获取打印机状态
                    // 1：高电平  //高代表打印机忙，app不允许发送数据
                    // 0：低电平  //低电平代表打印机空闲，可以接收数据
                    if (mICommService.getGpioPrinterState() == 1) {
                        //延迟10ms
                        handler.postDelayed(mRun_start, 10);

                    } else {
                        // 发送指令至打印机
                        send1(list.get(sendNum));
                        sendNum++;
                        //不延迟，继续发下一包
                        handler.postDelayed(mRun_start, 0);
                    }
                } catch (Exception e) {

                }


            }
        }

        ;
    };


    //关闭串口
    public static void closeCommonApi() {

        StopTestService();

        mSerialPort.closePort();
    }

    public static void printMuchText(Canvas canvas, TextPaint paint, String[] textList, int lineSpacing, int paperRealWidth, int paperRealHeight) {
        float textHeight = 0;
        ArrayList<String> texts = new ArrayList<>();
        float yPos = 0;
        for (int i = 0; i < textList.length; i++) {
            String text = textList[i];
            String str = "";
            float currentLineWidth = 0;
            for (int j = 0; j < text.length(); j++) {
                String character = String.valueOf(text.charAt(j));

                float wordWidth = paint.measureText(character);

                float newWidth = currentLineWidth + wordWidth;
                if (newWidth >= paperRealWidth) {
                    // 移至下一行
                    currentLineWidth = 0;
                    texts.add(str);
                    textHeight = textHeight + lineSpacing;
                    str = character;
                } else {
                    currentLineWidth = newWidth;
                    if (j == text.length() - 1) {
                        // 到最后也没超过长度
                        textHeight = textHeight + lineSpacing;
                        str = str + character;
                        texts.add(str);
                    } else {
                        str = str + character;
                    }
                }

            }
        }


        int h = (paperRealHeight - (int) Math.round(textHeight)) / (texts.size() + 1);

        int startX = 0;

        int startY = 0;

        for (int i = 0; i < texts.size(); i++) {
            if (startY == 0) {
                startY = lineSpacing + h;
            }
            String t = texts.get(i);
            canvas.drawText(t, startX, startY, paint);
            startY = startY + h + lineSpacing;
        }

    }


    public static Bitmap textAsBitmap(int size, int paperWidth, int paperHeight, final String qrCodeStr, String text, int lineSpacing) {

        String[] textList = text.split("\\r?\\n");

        double scale = 0.8 / 3.1;

        int paperRealWidth = (int) Math.round(paperWidth * 30 * scale);

        int paperRealHeight = (int) Math.round(paperHeight * 30 * scale);

        Log.d("paperRealWidth", "--------->>>>>  " + String.valueOf(paperRealWidth));
        Log.d("paperRealHeight", "--------->>>>>  " + String.valueOf(paperRealHeight));


        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(size);

        StaticLayout layout = new StaticLayout("", textPaint, paperRealWidth,
                Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);

        int space = 0;

        if (paperHeight == 50) {
            space = 10;
        } else if (paperHeight == 30) {
            space = 20;
        } else if (paperHeight == 70) {
            space = 10;
        }

        // 创建一个位图
        Bitmap bitmap = Bitmap.createBitmap(paperRealWidth, paperRealHeight - space, Bitmap.Config.ARGB_8888);
        // 创建一个画面
        Canvas canvas = new Canvas(bitmap);

        canvas.drawRGB(255, 255, 255);

        printMuchText(canvas, textPaint, textList, lineSpacing, paperRealWidth, paperRealHeight);

        int textWidth = paperRealWidth * 2 / 3;

        int leftWidth = paperRealWidth - textWidth;


        double codeScale = 0.35;

        if (paperHeight == 50) {
            codeScale = 0.5;
        } else if (paperHeight == 30) {
            codeScale = 0.79;
        } else if (paperHeight == 70) {
            codeScale = 0.41;
        }


        int codeWidth = (int) Math.round(paperRealHeight * codeScale);

        Bitmap bit = PrintUtils.createQRImage(qrCodeStr, codeWidth, codeWidth);
        // 画布画个图片
        float mapW = (float) (paperRealWidth - codeWidth + 10);
        float mapH = (float) (paperRealHeight - codeWidth + 10);

        canvas.drawBitmap(bit, mapW, mapH, textPaint);
        layout.draw(canvas);
        return bitmap;
    }

    public static Bitmap qrCordAsBitmap(int size, int paperWidth, int paperHeight, final String qrCodeStr, int lineSpacing) {

        int paperRealWidth = 385;

        int paperRealHeight = 320;

        double codeScale = 0.35;

        if (paperHeight == 50) {
            paperRealWidth = 385;
            paperRealHeight = 385;
            codeScale = 0.6;
        } else if (paperHeight == 30) {
            codeScale = 0.33;
        } else if (paperHeight == 70) {
            codeScale = 0.8;
        }


        int codeWidth = (int) Math.round((paperRealHeight >= paperRealWidth ? paperRealWidth : paperRealHeight) * codeScale);


        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(25);

        StaticLayout layout = new StaticLayout("", textPaint, paperRealWidth,
                Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);


        // 创建一个位图
        Bitmap bitmap = Bitmap.createBitmap(paperRealWidth, paperRealHeight, Bitmap.Config.ARGB_8888);
        // 创建一个画面
        Canvas canvas = new Canvas(bitmap);

        canvas.drawRGB(255, 255, 255);


        float currentLineWidth = 0;

        for (int j = 0; j < qrCodeStr.length(); j++) {
            String character = String.valueOf(qrCodeStr.charAt(j));

            float wordWidth = textPaint.measureText(character);

            currentLineWidth = currentLineWidth + wordWidth;
        }

        int startX = (int) Math.round((paperRealWidth - currentLineWidth) / 2.0);


        Bitmap bit = PrintUtils.createQRImage(qrCodeStr, codeWidth, codeWidth);


        // 画布画个图片
        float mapW = (float) ((paperRealWidth - codeWidth) / 2.0);


        float mapH = (float) ((paperRealHeight - codeWidth - lineSpacing) / 2.0);
        ;

        canvas.drawText(qrCodeStr, startX, mapH + codeWidth + 10, textPaint);

        canvas.drawBitmap(bit, mapW, mapH, textPaint);


        layout.draw(canvas);

        return bitmap;
    }


    public static void printTextAsBitmap(final int align, final Bitmap bitmap1,
                                         final boolean isLabel, final boolean tearPape) {

        send(new byte[]{0x1d, 0x61, 0x00});

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (isCanprint) {

                    //初始化打印机
                    PrintUtils.send(new byte[]{0x1B, 0x40});

                    switch (align) {
                        case 0:
                            send(new byte[]{0x1b, 0x61, 0x00});
                            break;
                        case 1:
                            send(new byte[]{0x1b, 0x61, 0x01});
                            break;
                        case 2:
                            send(new byte[]{0x1b, 0x61, 0x02});
                            break;
                        default:
                            break;
                    }


                    Bitmap bitmap = bitmap1;


                    byte[] printData1 = draw2PxPoint(bitmap);

                    // 一票一控起始指令
                    printData1 = concat(new byte[]{0x1D, 0x23, 0x53,
                            (byte) 0xD1, 0x7A, (byte) 0xF8, 0x4D}, printData1);

                    if (tearPape && !labelEnable) {
                        //非黑标状态打印后多加3个回车，把纸张推出来，方便撕纸
//                        printData1 = concat(
//                                printData1, new byte[]{0x20, 0x0a});
                    }

                    if (isLabel) {

                        printData1 = concat(printData1,
                                new byte[]{0x1D, 0x0c});

                    }

                    // 发送一票一控结束指令
                    printData1 = concat(printData1, new byte[]{0x1D, 0x23,
                            0x45});

                    send(printData1);

                }
            }
        }, 200);
    }


    /**
     * 打印文字
     *
     * @param size     文字大小，其中1为正常字体，2位双倍宽高字体，暂不支持其他字体大小
     * @param align    对齐方式，其中0为居左，1为居中，2为居右
     * @param text     打印的文字
     * @param tearPape 打印完是否要撕纸，设为true时候会打印完多走3空行，方便撕纸操作
     */
    public static void printText(final int paper_width, final int size, final int align,
                                 final String text, final boolean isLabel, final boolean tearPape) {

        // 查询是否缺纸
        send(new byte[]{0x1d, 0x61, 0x00});

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (isCanprint) {

                    String str_space;
                    int num;
                    if (paper_width == 52) {
                        str_space = "";
                        num = 24;
                    } else if (paper_width == 40) {
                        str_space = "        ";
                        num = 18;
                    } else {
                        str_space = "             ";
                        num = 12;
                    }
                    String[] lines = splitStringEveryNCharacters(text, str_space, num);
                    StringBuffer str_print = new StringBuffer();
                    for (String line : lines) {
                        str_print.append(line);
                    }
                    String str_print1 = str_space + str_print.toString().trim();

                    //初始化打印机
                    PrintUtils.send(new byte[]{0x1B, 0x40});

                    switch (align) {
                        case 0:
                            send(new byte[]{0x1b, 0x61, 0x00});
                            break;
                        case 1:
                            send(new byte[]{0x1b, 0x61, 0x01});
                            break;
                        case 2:
                            send(new byte[]{0x1b, 0x61, 0x02});
                            break;
                        default:
                            break;
                    }
                    switch (size) {
                        case 1:
                            send(new byte[]{0x1D, 0x21, 0x00});
                            break;
                        case 2:
                            send(new byte[]{0x1D, 0x21, 0x11});
                            break;
                        default:
                            break;
                    }
                    // 打印
                    try {

                        byte[] printData1 = (str_print1 + "\n").getBytes("GBK");

                        // 一票一控起始指令
                        printData1 = concat(new byte[]{0x1D, 0x23, 0x53, (byte) 0xD1, 0x7A, (byte) 0xF8, 0x4D}, printData1);

                        if (tearPape && !labelEnable) {
                            //非黑标状态打印后多加3个回车，把纸张推出来，方便撕纸
                            printData1 = concat(
                                    printData1, new byte[]{0x20, 0x0a, 0x0a, 0x0a});
                        }

                        if (isLabel) {

                            printData1 = concat(printData1, new byte[]{0x1D, 0x0c});

                        }

                        // 发送一票一控结束指令
                        printData1 = concat(printData1, new byte[]{0x1D, 0x23, 0x45});

                        send(printData1);

                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }, 200);

    }

    public static Bitmap textAsBitmapTest(float textSize) {

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout("", textPaint, 380,
                Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        // 创建一个位图
        Bitmap bitmap = Bitmap.createBitmap(380, 320, Bitmap.Config.ARGB_8888);
        // 创建一个画面
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRGB(255, 255, 255);
        canvas.drawText("料框号:", 0, 30, textPaint);
        canvas.drawText("123456", 80, 30, textPaint);
        canvas.drawText("物料号:", 0, 60, textPaint);
        canvas.drawText("_7", 120, 60, textPaint);
        canvas.drawText("物料号描述:", 0, 90, textPaint);
        canvas.drawText("27", 120, 90, textPaint);
        canvas.drawText("质量类型:", 0, 120, textPaint);
        canvas.drawText(" 6", 120, 120, textPaint);
        canvas.drawText("数量:", 0, 150, textPaint);
        canvas.drawText("_" + 7, 120, 150, textPaint);
        canvas.drawText("打印人:", 0, 180, textPaint);
        canvas.drawText("_27", 120, 180, textPaint);

        Bitmap bit = PrintUtils.createQRImage("12345678", 100, 100);
        // 画布画个图片
        canvas.drawBitmap(bit, 310, 180, textPaint);
        layout.draw(canvas);

        return bitmap;
    }


    /**
     * 打印图片
     *
     * @param paper_width 纸张宽度选择，目前只有30mm、40mm和52mm可选
     * @param align       对齐方式，其中0为居左，1为居中，2为居右
     * @param bitmap1     需要打印的图片
     * @param tearPape    打印完是否要撕纸，设为true时候会打印完多走3空行，方便撕纸操作
     */
    public static void printBitmap(final int paper_width, final int align, final Bitmap bitmap1,
                                   final boolean isLabel, final boolean tearPape) {

        send(new byte[]{0x1d, 0x61, 0x00});

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (isCanprint) {

                    //初始化打印机
                    PrintUtils.send(new byte[]{0x1B, 0x40});

                    switch (align) {
                        case 0:
                            send(new byte[]{0x1b, 0x61, 0x00});
                            break;
                        case 1:
                            send(new byte[]{0x1b, 0x61, 0x01});
                            break;
                        case 2:
                            send(new byte[]{0x1b, 0x61, 0x02});
                            break;
                        default:
                            break;
                    }

//					int num = 384;
//					if (Build.MODEL.contains("408")) {
//						num = 570;
//					}

                    Bitmap bitmap = null;
//					// 58纸张最大支持宽度是384，80纸张最大支持570
//					if (bitmap1.getWidth() > num) {
//
//						bitmap = zoomImg(bitmap1, num, bitmap1.getHeight());
//
//					} else {
//
                    bitmap = bitmap1;
//
//					}

                    if (paper_width == 52) {
                    } else if (paper_width == 40) {

//						bitmap=zoomImg(bitmap,150,bitmap.getHeight());

                        mBitmap_write = zoomImg(mBitmap_write, 100, bitmap.getHeight());
                        bitmap = twoBtmap2One1(mBitmap_write, bitmap);

                    } else {

//						bitmap=zoomImg(bitmap,200,bitmap.getHeight());
                        mBitmap_write = zoomImg(mBitmap_write, 180, bitmap.getHeight());
                        bitmap = twoBtmap2One1(mBitmap_write, bitmap);

                    }
                    byte[] printData1 = draw2PxPoint(bitmap);

                    // 一票一控起始指令
                    printData1 = concat(new byte[]{0x1D, 0x23, 0x53,
                            (byte) 0xD1, 0x7A, (byte) 0xF8, 0x4D}, printData1);

                    if (tearPape && !labelEnable) {
                        //非黑标状态打印后多加3个回车，把纸张推出来，方便撕纸
                        printData1 = concat(
                                printData1, new byte[]{0x20, 0x0a, 0x0a, 0x0a});
                    }

                    if (isLabel) {

                        printData1 = concat(printData1,
                                new byte[]{0x1D, 0x0c});

                    }

                    // 发送一票一控结束指令
                    printData1 = concat(printData1, new byte[]{0x1D, 0x23,
                            0x45});

                    send(printData1);

                }
            }
        }, 200);
    }

    /**
     * 打印一维码
     *
     * @param paper_width  纸张宽度选择，目前只有30mm、40mm和52mm可选
     * @param align        对齐方式，其中0为居左，1为居中，2为居右
     * @param width        打印一维码宽度，由于58纸张限制，最大打印宽度为384，超过则可能无法打印
     * @param height       打印一维码的高度
     * @param data         一维码的内容（不能是中文，否则会报错）
     * @param isShowBarStr 是否显示条码内容
     * @param size         条码内容文字大小，单位是sp
     * @param tearPape     打印完是否要撕纸，设为true时候会打印完多走3空行，方便撕纸操作
     */
    public static void printBarCode(final int paper_width, final int align, final int width,
                                    final int height, final String data, final boolean isShowBarStr,
                                    final int size, final boolean isLabel, final boolean tearPape) {

        send(new byte[]{0x1d, 0x61, 0x00});

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (isCanprint) {

                    //初始化打印机
                    PrintUtils.send(new byte[]{0x1B, 0x40});

                    switch (align) {
                        case 0:
                            send(new byte[]{0x1b, 0x61, 0x00});
                            break;
                        case 1:
                            send(new byte[]{0x1b, 0x61, 0x01});
                            break;
                        case 2:
                            send(new byte[]{0x1b, 0x61, 0x02});
                            break;

                        default:
                            break;
                    }

                    // 生成一维码图片
                    Bitmap mBitmap1 = BarcodeCreater.creatBarcode(context,
                            data, width, height, false, 1, size);

                    // 一维码图片去除白边
                    mBitmap1 = BitmapDeleteNoUseSpaceUtil
                            .deleteNoUseWhiteSpace(mBitmap1);

                    // 图片缩放
                    mBitmap1 = zoomImg(mBitmap1, width, height);


                    if (isShowBarStr) {

                        // 生成一维码内容文字图片
                        Bitmap textBitmap = textAsBitmap(data, mBitmap1.getWidth(), size);

                        mBitmap1 = twoBtmap2One(mBitmap1, textBitmap);

                    }

                    if (paper_width == 52) {
                    } else if (paper_width == 40) {
                        mBitmap_write = zoomImg(mBitmap_write, 100, mBitmap1.getHeight());
                        mBitmap1 = twoBtmap2One1(mBitmap_write, mBitmap1);
                    } else {
                        mBitmap_write = zoomImg(mBitmap_write, 180, mBitmap1.getHeight());
                        mBitmap1 = twoBtmap2One1(mBitmap_write, mBitmap1);
                    }

                    //一维码数据
                    byte[] printData1 = draw2PxPoint(mBitmap1);

                    // 一票一控起始指令
                    printData1 = concat(new byte[]{0x1D, 0x23, 0x53,
                            (byte) 0xD1, 0x7A, (byte) 0xF8, 0x4D}, printData1);

                    if (tearPape && !labelEnable) {
                        //非黑标状态打印后多加3个回车，把纸张推出来，方便撕纸
                        printData1 = concat(
                                printData1, new byte[]{0x20, 0x0a, 0x0a, 0x0a});
                    }

                    if (isLabel) {

                        //是否黑标走纸
                        printData1 = concat(printData1,
                                new byte[]{0x1D, 0x0c});

                    }

                    // 发送一票一控结束指令
                    printData1 = concat(printData1, new byte[]{0x1D, 0x23,
                            0x45});


                    send(printData1);

                }
            }
        }, 200);
    }


    /**
     * 打印二维码
     *
     * @param paper_width 纸张宽度选择，目前只有30mm、40mm和52mm可选
     * @param align       对齐方式，0 左对齐，1 居中，2右对齐
     * @param width       二维码宽度
     * @param height      二维码高度
     * @param data        二维码内容
     * @param size        是否显示二维码
     * @param size        二维码内容大小
     * @param tearPape    打印完是否要撕纸，设为true时候会打印完多走3空行，方便撕纸操作
     */
    public static void printQRCode(final int paper_width, final int align, final int width,
                                   final int height, final String data, final boolean isShowQRStr,
                                   final int size, final boolean isLabel, final boolean tearPape) {

        send(new byte[]{0x1d, 0x61, 0x00});

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (isCanprint) {

                    //初始化打印机
                    PrintUtils.send(new byte[]{0x1B, 0x40});

                    switch (align) {
                        case 0:
                            send(new byte[]{0x1b, 0x61, 0x00});
                            break;
                        case 1:
                            send(new byte[]{0x1b, 0x61, 0x01});
                            break;
                        case 2:
                            send(new byte[]{0x1b, 0x61, 0x02});
                            break;

                        default:
                            break;
                    }

                    Bitmap mBitmap = createQRImage(data, width, height);


                    if (mBitmap == null) {

                        mBitmap = BarcodeCreater.encode2dAsBitmap(data, width,
                                height, 2);

                        // mBitmap = BitmapDeleteNoUseSpaceUtil
                        // .deleteNoUseWhiteSpace(mBitmap);

                        // mBitmap = zoomImg(mBitmap, width, height);

                    }

                    if (paper_width == 52) {
                    } else if (paper_width == 40) {
                        mBitmap_write = zoomImg(mBitmap_write, 100, mBitmap.getHeight());
                        mBitmap = twoBtmap2One1(mBitmap_write, mBitmap);
                    } else {
                        mBitmap_write = zoomImg(mBitmap_write, 180, mBitmap.getHeight());
                        mBitmap = twoBtmap2One1(mBitmap_write, mBitmap);
                    }

                    Bitmap textBitmap = textAsBitmap(data, width, size);
                    //
                    byte[] printData1 = draw2PxPoint(mBitmap);

                    // 一票一控起始指令
                    printData1 = concat(new byte[]{0x1D, 0x23, 0x53,
                            (byte) 0xD1, 0x7A, (byte) 0xF8, 0x4D}, printData1);

                    if (isShowQRStr) {

                        byte[] printData2 = draw2PxPoint(textBitmap);

                        printData1 = concat(printData1, printData2);

                    }

                    if (tearPape && !labelEnable) {
                        //非黑标状态打印后多加3个回车，把纸张推出来，方便撕纸
                        printData1 = concat(
                                printData1, new byte[]{0x20, 0x0a, 0x0a, 0x0a});
                    }

                    if (isLabel) {

                        printData1 = concat(printData1,
                                new byte[]{0x1D, 0x0c});

                    }
                    // 发送一票一控结束指令
                    printData1 = concat(printData1, new byte[]{0x1D, 0x23,
                            0x45});

                    send(printData1);

                }
            }
        }, 200);
    }

    public static boolean labelEnable = false;

    /**
     * 是否开启黑标功能
     *
     * @param enable
     */
    public static void labelEnable(boolean enable) {

        labelEnable = enable;
        if (enable) {
            // 打开标签检测指令，即开启黑标功能
            PrintUtils.send(new byte[]{0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04,
                    0x05, 0x06, 0x44});
        } else {
            // 关闭标签检测指令，即关闭黑标功能
            PrintUtils.send(new byte[]{0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04,
                    0x05, 0x06, 0x66});
        }

    }

    /**
     * 走纸到下一页，只有开启黑标功能才生效
     */
    public static void toNextPaper() {

        PrintUtils.send(new byte[]{0x1d, 0x0c});

    }

    /**
     * 打印测试
     */
    public static void printTest() {

        PrintUtils.    // 自检
                send(new byte[]{0x1b, 0x23, 0x23, 0x53, 0x45,
                0x4c, 0x46});

    }

    /**
     * 设定黑标起始位
     *
     * @param startNum
     */
    public static void setStarNum(int startNum) {

        // 黑标起始位设置，倒数第二个字节startNum为标签起始位
        PrintUtils.send(new byte[]{0x1D, 0x28, 0x46, 0x04, 0x00, 0x01, 0x00,
                (byte) startNum, 0x00});

    }

    public static StringBuffer bytesToString(byte[] bytes) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String s = Integer.toHexString(bytes[i] & 0xff);
            if (s.length() < 2)
                sBuffer.append('0');
            sBuffer.append(s + " ");
        }
        return sBuffer;
    }

    public static String[] splitStringEveryNCharacters(String input, String str_space, int n) {
        if (input == null || input.isEmpty() || n <= 0) {
            return new String[0];
        }

        int length = input.length();
        int count = (length + n - 1) / n; // 向上取整
        String[] result = new String[count];

        for (int i = 0, start = 0; i < count; start += n, i++) {
            int end = Math.min(start + n, length);
            result[i] = str_space + input.substring(start, end) + "\n";
        }

        return result;
    }

    /**
     * 文字转图片
     *
     * @param str
     * @return
     */
    public static Bitmap word2bitmap(String str, int width, int height, int size) {

        Bitmap bMap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bMap);
        canvas.drawColor(Color.WHITE);
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(size);
        // textPaint.
        StaticLayout layout = new StaticLayout(str, textPaint, bMap.getWidth(),
                Alignment.ALIGN_NORMAL, (float) 1.0, (float) 0.0, true);
        layout.draw(canvas);

        return bMap;

    }

    /**
     * 文字转图片
     *
     * @param text     将要生成图片的内容
     * @param textSize 文字大小
     * @return
     */
    public static Bitmap textAsBitmap(String text, int wigth, float textSize) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);

        textPaint.setFakeBoldText(true);

        StaticLayout layout = new StaticLayout(text, textPaint, wigth,
                Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight() + 20, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);

        return bitmap;
    }

    /**
     * 文字转图片
     *
     * @param text     将要生成图片的内容
     * @param textSize 文字大小
     * @return
     */
    public static Bitmap textAsBitmap1(String text, float textSize) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);

        StaticLayout layout = new StaticLayout(text, textPaint, 384,
                Alignment.ALIGN_CENTER, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth(),
                layout.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);

        return bitmap;

    }

    /**
     * 两张图片上下合并成一张
     *
     * @param bitmap1
     * @param bitmap2
     * @return
     */
    public static Bitmap twoBtmap2One(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth(),
                bitmap1.getHeight() + bitmap2.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
        return bitmap3;
    }

    /**
     * 两张图片左右合并成一张
     *
     * @param bitmap1
     * @param bitmap2
     * @return
     */
    public static Bitmap twoBtmap2One1(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(
                bitmap1.getWidth() + bitmap2.getWidth(), bitmap1.getHeight(),
                bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null);
        return bitmap3;
    }

    /**
     * 图片旋转
     *
     * @param bm                将要旋转的图片
     * @param orientationDegree 旋转角度
     * @return
     */
    public static Bitmap adjustPhotoRotation(Bitmap bm,
                                             final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2,
                (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(),
                Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;

    }

    // 缩放图片
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;
    }

    /**
     * 生成二维码 要转换的地址或字符串,可以是中文
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createQRImage(String url, int width, int height) {
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "GBK");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, width, height, hints);
            bitMatrix = deleteWhite(bitMatrix);// 删除白边
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片， 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap
                    .createBitmap(width, height, Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {

        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    /**
     * hex String to byte array
     */
    public static byte[] hexStringToBytes(String hexString) {


        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

    public static void StartTestService(Context context1) {

        context = context1;

        if (null == mICommService) {
            try {
                Intent intent = new Intent();
                intent.setAction("comm_com_service");
                intent.setPackage("com.android.settings");
                boolean success = context.bindService(intent, SERVICECONNECTION, Context.BIND_AUTO_CREATE);
                Log.d("GPIO", "--------->>>>>   StartTestService() success = " + success);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public static void StopTestService() {
        if (null != mICommService) {
            try {
                //给模块断电
                mICommService.setGpioOut(Integer.parseInt("72"), 0);
                mICommService.setGpioOut(Integer.parseInt("150"), 0);
                mICommService.setGpioOut(Integer.parseInt("151"), 0);
                mICommService.setGpioOut(Integer.parseInt("158"), 0);
            } catch (Exception e) {

            }
            Log.d("GPIO", "--------->>>>>   StopTestService()");
            mICommService = null;
            context.unbindService(SERVICECONNECTION);
        }
    }

    public static ICommService mICommService = null;
    public static ServiceConnection SERVICECONNECTION = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mICommService = ICommService.Stub.asInterface(iBinder);
            if (null != mICommService) {
                Log.d("GPIO", "--------->>>>>   onServiceConnected()");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mICommService.setGpioOut(Integer.parseInt("72"), 1);
                            mICommService.setGpioOut(Integer.parseInt("150"), 1);
                            mICommService.setGpioOut(Integer.parseInt("151"), 1);
                            mICommService.setGpioOut(Integer.parseInt("158"), 1);
                        } catch (Exception e) {

                        }
                    }
                }, 500);

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("GPIO", "--------->>>>>   onServiceDisconnected()");
        }
    };

    /*************************************************************************
     * 假设一个240*240的图片，分辨率设为24, 共分10行打印 每一行,是一个 240*24 的点阵, 每一列有24个点,存储在3个byte里面。
     * 每个byte存储8个像素点信息。因为只有黑白两色，所以对应为1的位是黑色，对应为0的位是白色
     **************************************************************************/
    /**
     * 把一张Bitmap图片转化为打印机可以打印的字节流
     *
     * @param bmp
     * @return
     */
    public static byte[] draw2PxPoint(Bitmap bmp) {
        // 用来存储转换后的 bitmap 数据。为什么要再加1000，这是为了应对当图片高度无法
        // 整除24时的情况。比如bitmap 分辨率为 240 * 250，占用 7500 byte，
        // 但是实际上要存储11行数据，每一行需要 24 * 240 / 8 =720byte 的空间。再加上一些指令存储的开销，
        // 所以多申请 1000byte 的空间是稳妥的，不然运行时会抛出数组访问越界的异常。
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1200;
        byte[] data = new byte[size];
        int k = 0;
        // 设置行距为0的指令
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // 逐行打印
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            // 打印图片的指令
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); // nL
            data[k++] = (byte) (bmp.getWidth() / 256); // nH
            // 对于每一行，逐列打印
            for (int i = 0; i < bmp.getWidth(); i++) {
                // 每一列24个像素点，分为3个字节存储
                for (int m = 0; m < 3; m++) {
                    // 每个字节表示8个像素点，0表示白色，1表示黑色
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        if (k < size) {
                            data[k] += data[k] + b;
                        }
                        // data[k] = (byte) (data[k]+ data[k] + b);
                    }
                    k++;
                }
            }
            if (k < size) {
                data[k++] = 10;// 换行
            }
        }
        return data;
    }

    /**
     * 灰度图片黑白化，黑色是1，白色是0
     *
     * @param x   横坐标
     * @param y   纵坐标
     * @param bit 位图
     * @return
     */
    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // 取高两位
            int green = (pixel & 0x0000ff00) >> 8; // 取中两位
            int blue = pixel & 0x000000ff; // 取低两位
            int gray = RGB2Gray(red, green, blue);
            // if (gray < 128) {
            if (gray < 200) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    /**
     * 图片灰度的转化
     */
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b); // 灰度转化公式
        return gray;
    }

}
