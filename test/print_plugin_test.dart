import 'package:flutter_test/flutter_test.dart';
import 'package:print_plugin/print_plugin.dart';
import 'package:print_plugin/print_plugin_platform_interface.dart';
import 'package:print_plugin/print_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPrintPluginPlatform
    with MockPlatformInterfaceMixin
    implements PrintPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<String?> getPlatformVersionTest() => Future.value('42');

}

void main() {
  final PrintPluginPlatform initialPlatform = PrintPluginPlatform.instance;

  test('$MethodChannelPrintPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPrintPlugin>());
  });

  test('getPlatformVersion', () async {
    PrintPlugin printPlugin = PrintPlugin();
    MockPrintPluginPlatform fakePlatform = MockPrintPluginPlatform();
    PrintPluginPlatform.instance = fakePlatform;

    expect(await printPlugin.getPlatformVersion(), '42');
  });
}
