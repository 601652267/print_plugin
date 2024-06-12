
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
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  Future<String?> getPlatformVersionTest() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersionTest');
    return version;
  }

  Future<String?> initPrint() async {
    final version = await methodChannel.invokeMethod<String>('initPrint');
    return version;
  }

  Future<String?> printTest() async {
    return await methodChannel.invokeMethod<String>('printTest');
  }

  Future<String?> printText(Map config) async {
    return await methodChannel.invokeMethod<String>('printText', config);
  }

  Future<void> labelEnable(Map config) async {
     await methodChannel.invokeMethod<String>('labelEnable', config);
  }

  Future<void> printQRCode(Map config) async {
    await methodChannel.invokeMethod<String>('printQRCode', config);
  }






}
