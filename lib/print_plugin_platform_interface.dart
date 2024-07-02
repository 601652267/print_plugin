import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'print_plugin_method_channel.dart';

abstract class PrintPluginPlatform extends PlatformInterface {
  /// Constructs a PrintPluginPlatform.
  PrintPluginPlatform() : super(token: _token);

  static final Object _token = Object();

  static PrintPluginPlatform _instance = MethodChannelPrintPlugin();

  /// The default instance of [PrintPluginPlatform] to use.
  ///
  /// Defaults to [MethodChannelPrintPlugin].
  static PrintPluginPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [PrintPluginPlatform] when
  /// they register themselves.
  static set instance(PrintPluginPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> getPlatformVersionTest() {
    throw UnimplementedError(
        'getPlatformVersionTest() has not been implemented.');
  }

  Future<String?> initPrint() async {
    throw UnimplementedError('initPrint() has not been implemented.');
  }

  Future<String?> printTest() async {
    throw UnimplementedError('printTest() has not been implemented.');
  }

  Future<String?> printText(Map config) async {
    throw UnimplementedError('printText() has not been implemented.');
  }

  Future<void> labelEnable(Map config) async {
    throw UnimplementedError('labelEnable() has not been implemented.');
  }

  Future<void> printQRCode(Map config) async {
    throw UnimplementedError('printQRCode() has not been implemented.');
  }

  Future<void> textAsBitmap(Map config) async {
    throw UnimplementedError('textAsBitmap() has not been implemented.');
  }

  Future<void> openScan(Map config) async {
    throw UnimplementedError('openScan() has not been implemented.');
  }

  void setUpMethodCallHandler(Function resolve) {
    _instance.setUpMethodCallHandler(resolve);
  }
}
