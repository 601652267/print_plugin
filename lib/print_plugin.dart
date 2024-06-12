
import 'print_plugin_platform_interface.dart';

class PrintPlugin {
  Future<String?> getPlatformVersion() {
    return PrintPluginPlatform.instance.getPlatformVersion();
  }

  Future<String?> getPlatformVersionTest() async {
    return PrintPluginPlatform.instance.getPlatformVersionTest();
  }

}
