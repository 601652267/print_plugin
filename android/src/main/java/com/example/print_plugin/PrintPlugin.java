package com.example.print_plugin;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.example.print_plugin.PrintUtils;

import android.os.Handler;

import io.flutter.embedding.android.FlutterActivity;

import android.content.Context;


/**
 * PrintPlugin
 */
public class PrintPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context;

    boolean isInit = false;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "print_plugin");
        channel.setMethodCallHandler(this);
    }

    public boolean initPrint() {
        isInit = true;
        //初始化打印工具
        return PrintUtils.initPrintUtils(context);
    }


    /**
     * 打印自检
     */
    public void printTest() {
        PrintUtils.printTest();
    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE + " 999");
        } else if (call.method.equals("getPlatformVersionTest")) {
            result.success("Android 123 ----- 456");
        } else if (call.method.equals("initPrint")) {
            boolean res = this.initPrint();
            result.success(res ? "true" : "false");
        } else if (call.method.equals("printText")) {
            Integer paperWidthNumber = call.argument("paperWidth");
            int paperWidth = 52; // 整数类型的默认值
            if (paperWidthNumber != null) {
                paperWidth = paperWidthNumber;
            }

            // 文字大小，其中1为正常字体，2为双倍宽高字体，暂不支持其他字体大小
            Integer sizeNumber = call.argument("size");
            int size = 1; // 整数类型的默认值
            if (sizeNumber != null) {
                size = sizeNumber;
            }

            // 对齐方式，其中0为居左，1为居中，2为居右
            Integer alignNumber = call.argument("align");
            int align = 1; // 整数类型的默认值
            if (alignNumber != null) {
                align = alignNumber;
            }

            // 要打印的文字
            String text = call.argument("text");
            if (text == null) {
                text = ""; // 设置默认消息
            }
            PrintUtils.printText(paperWidth, size, align, text, true, true);

        } else if (call.method.equals("printTest")) {
            this.printTest();
            result.success("true");
        } else if (call.method.equals("labelEnable")) {
            /**
             * 纸张类型选择 enable true 普通热敏纸 false 间隙标签纸
             */
            boolean enable = call.argument("enable");
            PrintUtils.labelEnable(enable);
            result.success("true");
        } else if (call.method.equals("printQRCode")) {
            /**
             * 打印二维码
             * @param align 对齐方式，0 左对齐，1 居中，2右对齐
             * @param width 二维码宽度
             * @param height 二维码高度
             * @param data 二维码内容
             * @param isShowQRStr 是否显示二维码内容
             * @param size 二维码内容大小
             * @param isLabel 是否进行标签走纸，设为true时候打印完自动走纸到下一页，只有开启标签/黑标功能时有效
             * @param tearPape  打印完是否要撕纸，设为true时候会打印完多走3空行，把纸张推出来，方便撕纸操作,只有关闭标签/黑标功能时有效
             */

            Integer paperWidthNumber = call.argument("paperWidth");
            int paperWidth = 52; // 整数类型的默认值
            if (paperWidthNumber != null) {
                paperWidth = paperWidthNumber;
            }

            Integer paperHeightNumber = call.argument("paperHeight");
            int paperHeight = 30; // 整数类型的默认值
            if (paperHeightNumber != null) {
                paperHeight = paperHeightNumber;
            }

            Integer codeSizeNumber = call.argument("codeSize");
            int codeSize = 160; // 整数类型的默认值
            if (codeSizeNumber != null) {
                codeSize = codeSizeNumber;
            }

            Integer sizeNumber = call.argument("size");
            int size = 30; // 整数类型的默认值
            if (sizeNumber != null) {
                size = sizeNumber;
            }

            Integer alignNumber = call.argument("align");
            int align = 1; // 整数类型的默认值
            if (alignNumber != null) {
                align = alignNumber;
            }

            String data = call.argument("data");
            if (data == null) {
                data = ""; // 设置默认消息
            }

            PrintUtils.printQRCode(paperWidth,align, codeSize, codeSize, data, false, size, true,true);
            result.success("true");
        }  else {
            result.notImplemented();
        }



    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
