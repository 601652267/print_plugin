import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'print_plugin_platform_interface.dart';

/// An implementation of [PrintPluginPlatform] that uses method channels.
class MethodChannelPrintPlugin extends PrintPluginPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('print_plugin');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> getPlatformVersionTest() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersionTest');
    return version;
  }

  @override
  Future<String?> initPrint() async {
    final version = await methodChannel.invokeMethod<String>('initPrint');
    return version;
  }

  @override
  Future<String?> printTest() async {
    return await methodChannel.invokeMethod<String>('printTest');
  }

  @override
  Future<String?> printText(Map config) async {
    return await methodChannel.invokeMethod<String>('printText', config);
  }

  @override
  Future<void> labelEnable(Map config) async {
    await methodChannel.invokeMethod<String>('labelEnable', config);
  }

  @override
  Future<void> printQRCode(Map config) async {
    await methodChannel.invokeMethod<String>('printQRCode', config);
  }

  @override
  Future<void> textAsBitmap(Map config) async {
    await methodChannel.invokeMethod<String>('textAsBitmap', config);
  }

  @override
  Future<void> openScan(Map config) async {
    await methodChannel.invokeMethod<String>('openScan', config);
  }

  @override
  Future<void> configureScanner(Map config) async {
    await methodChannel.invokeMethod<String>('configureScanner', config);
  }

  @override
  Future<Map?> getScannerStatus() async {
    return await methodChannel.invokeMapMethod('getScannerStatus');
  }

  @override
  Future<bool?> setScannerActive(Map config) async {
    return await methodChannel.invokeMethod<bool>('setScannerActive', config);
  }

  @override
  Future<bool?> setScannerKeyEnabled(Map config) async {
    return await methodChannel.invokeMethod<bool>(
        'setScannerKeyEnabled', config);
  }

  @override
  Future<bool?> restoreScanner() async {
    return await methodChannel.invokeMethod<bool>('restoreScanner');
  }

  @override
  Future<bool?> startScan() async {
    return await methodChannel.invokeMethod<bool>('startScan');
  }

  @override
  Future<bool?> stopScan() async {
    return await methodChannel.invokeMethod<bool>('stopScan');
  }

  @override
  Future<void> intentTest(Map config) async {
    await methodChannel.invokeMethod<String>('intentTest', config);
  }

  @override
  void setUpMethodCallHandler(Function resolve) {
    methodChannel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onBroadcastReceived') {
        final arguments = call.arguments;
        if (arguments is Map) {
          log('message = ${arguments['message']}');
          resolve(arguments);
          return;
        }

        final message = arguments?.toString() ?? '';
        log('message = $message');
        resolve({'message': message});
        // 处理接收到的广播消息
      }
    });
  }
}
