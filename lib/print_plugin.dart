import 'print_plugin_platform_interface.dart';
import 'dart:io' show Platform;

class PrintPlugin {
  // 是否能使用打印功能
  bool couldUsePrint = false;
  // 是否能使用激光扫描。
  bool? couldUseScan;

  Future<String?> getPlatformVersion() {
    return PrintPluginPlatform.instance.getPlatformVersion();
  }

  Future<String?> getPlatformVersionTest() async {
    return PrintPluginPlatform.instance.getPlatformVersionTest();
  }

  Future<String?> printTest() async {
    return PrintPluginPlatform.instance.printTest();
  }

  Future<String?> printText(Map config) async {
    return PrintPluginPlatform.instance.printText(config);
  }

  Future<void> labelEnable(Map config) async {
    PrintPluginPlatform.instance.labelEnable(config);
  }

  Future<void> printQRCode(Map config) async {
    PrintPluginPlatform.instance.printQRCode(config);
  }

  Future<void> textAsBitmap(Map config) async {
    PrintPluginPlatform.instance.textAsBitmap(config);
  }

  Future<void> openScan(Map config) async {
    PrintPluginPlatform.instance.openScan(config);
  }

  Future<void> configureScanner({
    String? scanAction,
    String? scanDataKey,
  }) async {
    await PrintPluginPlatform.instance.configureScanner({
      'scanAction': scanAction,
      'scanDataKey': scanDataKey,
    });
  }

  Future<Map?> getScannerStatus() async {
    final status = await PrintPluginPlatform.instance.getScannerStatus();
    couldUseScan = status?['couldUseScan'] as bool?;
    return status;
  }

  Future<bool?> setScannerActive(bool active) async {
    return PrintPluginPlatform.instance.setScannerActive({'active': active});
  }

  Future<bool?> setScannerKeyEnabled(bool enabled) async {
    return PrintPluginPlatform.instance
        .setScannerKeyEnabled({'enabled': enabled});
  }

  Future<bool?> restoreScanner() async {
    final result = await PrintPluginPlatform.instance.restoreScanner();
    if (result != null) {
      couldUseScan = result;
    }
    return result;
  }

  Future<bool?> startScan() async {
    return PrintPluginPlatform.instance.startScan();
  }

  Future<bool?> stopScan() async {
    return PrintPluginPlatform.instance.stopScan();
  }

  Future<void> intentTest(Map config) async {
    PrintPluginPlatform.instance.intentTest(config);
  }

  Future<bool> initPrint() async {
    if (Platform.isIOS) {
      couldUsePrint = false;
      return false;
    }
    String? resultStr = await PrintPluginPlatform.instance.initPrint();
    if (resultStr == null || resultStr == 'false') {
      couldUsePrint = false;
      await getScannerStatus();
      return false;
    }
    couldUsePrint = true;
    await getScannerStatus();
    return true;
  }

  void setUpMethodCallHandler(Function resolve) {
    PrintPluginPlatform.instance.setUpMethodCallHandler(resolve);
  }
}
