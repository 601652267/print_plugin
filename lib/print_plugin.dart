import 'print_plugin_platform_interface.dart';
import 'dart:io' show Platform;

class PrintPlugin {
  // 是否能使用打印功能
  bool couldUsePrint = false;

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


  Future<bool> initPrint() async {
    if (Platform.isIOS) {
      couldUsePrint = false;
      return false;
    }
    String? resultStr = await PrintPluginPlatform.instance.initPrint();
    if (resultStr == null || resultStr! == 'false') {
      couldUsePrint = false;
      return false;
    }
    return true;
  }
}
