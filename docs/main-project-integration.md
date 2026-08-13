# 主项目接入 print_plugin

本文档说明主 Flutter 项目如何通过 Git 引入 `print_plugin`，并按 Android flavor 选择不同厂商实现。

## 1. 引入插件

在主项目 `pubspec.yaml` 中添加：

```yaml
dependencies:
  print_plugin:
    git:
      url: https://github.com/601652267/print_plugin
      ref: main
```

首次接入执行：

```bash
flutter pub get
```

后续插件代码更新后，主项目建议执行：

```bash
flutter pub upgrade print_plugin
```

如果仍然没有更新到 GitHub 最新 commit，可以检查 `pubspec.lock` 中 `print_plugin` 记录的 commit 是否还是旧值。

## 2. 插件内置厂商 flavor

插件 Android 侧定义了一个 `printer` 维度：

```text
normalPrinter  -> QS601 老厂家打印 SDK
baoanPrinter   -> 宝安 PDA ReaderManager.jar + 广播扫码实现
```

当前广播扫码信息：

```text
Action: com.scanner.broadcast
Extra: data
```

`baoanPrinter` 监听该广播，并额外兼容厂家文档里的 `com.android.server.scannerservice.broadcast` / `scannerdata`。`normalPrinter` 不再监听这套宝安广播。

## 3. 主项目 Android 配置

主项目一般已经有自己的客户 flavor，例如 `dev`、`kc`、`mda`、`baoan` 等。需要在主项目 `android/app/build.gradle` 中把每个客户 flavor 映射到插件的 `printer` flavor。

推荐在 `defaultConfig` 中设置默认打印实现：

```gradle
android {
    defaultConfig {
        // 没有单独声明的客户 flavor，默认使用 QS601 老厂家实现
        missingDimensionStrategy "printer", "normalPrinter"
    }

    flavorDimensions "client"

    productFlavors {
        kc {
            dimension "client"
            // 不写 missingDimensionStrategy 时，会走 defaultConfig 的 normalPrinter
        }

        mda {
            dimension "client"
            // 不写 missingDimensionStrategy 时，会走 defaultConfig 的 normalPrinter
        }

        baoan {
            dimension "client"
            // 特定客户使用宝安 PDA 实现
            missingDimensionStrategy "printer", "baoanPrinter"
        }
    }
}
```

如果主项目使用 `flutter_flavorizr`，也需要保证生成后的 Android flavor 名称和上面一致。`flavorizr` 中可以继续定义业务 flavor，例如：

```yaml
flavorizr:
  flavors:
    kc:
      app:
        name: "急救车KC"
      android:
        applicationId: "com.example.project_ambulance_kc"

    baoan:
      app:
        name: "急救车宝安"
      android:
        applicationId: "com.example.project_ambulance_baoan"
```

然后在生成后的 `android/app/build.gradle` 里补充或保留对应的 `missingDimensionStrategy`。

## 4. 哪些包会打进 APK

QS601 的 jar 只配置在插件的 `normalPrinter` flavor 中：

```gradle
normalPrinterImplementation files('src/normalPrinter/libs/qs601sdk.jar')
```

所以：

```gradle
missingDimensionStrategy "printer", "normalPrinter"
```

会把 QS601 SDK 打进 APK。

```gradle
missingDimensionStrategy "printer", "baoanPrinter"
```

不会把 QS601 SDK 打进 APK，会打进宝安扫描 SDK：

```gradle
baoanPrinterImplementation files('src/baoanPrinter/libs/ReaderManager.jar')
```

如果某个主项目 flavor 例如 `kc` 什么都没有配置，但 `defaultConfig` 写了：

```gradle
missingDimensionStrategy "printer", "normalPrinter"
```

那么 `kc` 会默认使用 `normalPrinter`，也会把 QS601 SDK 打进 APK。

如果 `defaultConfig` 和 `kc` 都没有配置 `missingDimensionStrategy`，Gradle 会不知道插件应该选择 `normalPrinter` 还是 `baoanPrinter`，通常会编译失败。

## 5. 打包命令

主项目使用自己的客户 flavor 打包。

例如 `kc`：

```bash
flutter build apk --flavor kc --release -t lib/main_kc.dart
```

例如 `baoan`：

```bash
flutter build apk --flavor baoan --release -t lib/main_baoan.dart
```

如果使用 FVM：

```bash
fvm flutter build apk --flavor kc --release -t lib/main_kc.dart
fvm flutter build apk --flavor baoan --release -t lib/main_baoan.dart
```

`--flavor` 使用的是主项目的客户 flavor 名，不是插件的 `normalPrinter` 或 `baoanPrinter`。插件厂商实现由 `missingDimensionStrategy` 选择。

## 6. Dart 初始化与扫码回调

主项目中初始化插件：

```dart
final printPlugin = PrintPlugin();

Future<void> initPrintPlugin() async {
  final couldUse = await printPlugin.initPrint();

  printPlugin.setUpMethodCallHandler((res) {
    final message = res['message'];
    // message 即扫码结果
  });
}
```

`printPlugin.couldUseScan` 表示当前 flavor 是否可用激光扫描：

```dart
final status = await printPlugin.getScannerStatus();
final couldUseScan = printPlugin.couldUseScan;
```

返回含义：

```text
true   当前 flavor 可用激光扫描。normalPrinter 打印初始化成功时也返回 true
false  当前 flavor 暂不可用激光扫描，例如 baoanPrinter 的厂家 Binder 服务不可用
null   当前平台或 flavor 没有提供扫描状态
```

收到 PDA 广播后，插件会回调：

```dart
{'message': 'D2393235398C'}
```

如果只需要触发老设备扫描按键，可以继续调用：

```dart
await printPlugin.openScan({});
```

对于 `baoanPrinter`，可以动态配置广播参数：

```dart
await printPlugin.configureScanner(
  scanAction: 'com.scanner.broadcast',
  scanDataKey: 'data',
);
```

也可以控制扫描头：

```dart
await printPlugin.setScannerActive(true);
await printPlugin.setScannerKeyEnabled(true);
await printPlugin.restoreScanner();
await printPlugin.startScan();
await printPlugin.stopScan();
```

从相机扫码页面退出后，可以调用：

```dart
await printPlugin.restoreScanner();
```

用于恢复扫描头开启、物理扫描键启用、API 输出模式和无结束符配置。

## 7. 打印二维码和文字

当前常用的二维码加文字打印接口：

```dart
await printPlugin.textAsBitmap({
  'paperWidth': 52,
  'paperHeight': 30,
  'lineSpacing': 15,
  'size': 18,
  'width': 20,
  'text': '药品:阿莫西林胶囊阿莫西林胶囊\n车码:87hfsxzg92100\n规格:10mg',
  'qrCodeStr': '二维码内容',
});
```

字段说明：

```text
paperWidth   纸张宽度，单位 mm
paperHeight  纸张高度，单位 mm
lineSpacing  文字行间距
size         文字大小
width        二维码宽度，单位 mm，默认 20mm
text         标签上显示的文字，过长会自动换行
qrCodeStr    二维码内容
```

## 8. 常见问题

### 更新 Git 依赖后没有变化

优先执行：

```bash
flutter pub upgrade print_plugin
```

如果还没有变化，检查 `pubspec.lock` 是否锁在旧 commit。

### `flutter pub upgrade print_plugin` 卡在 Resolving dependencies

常见原因是网络访问 GitHub 慢、Git 缓存锁住、或者主项目依赖冲突。可以先确认网络和 GitHub 访问，再重试。

### 编译时提示无法选择插件 variant

检查主项目 `android/app/build.gradle` 是否配置了：

```gradle
missingDimensionStrategy "printer", "normalPrinter"
```

或者在特定客户 flavor 中配置：

```gradle
missingDimensionStrategy "printer", "baoanPrinter"
```

### 扫码没有回调

检查：

```text
1. App 是否调用了 initPrint()
2. PDA 系统扫码服务是否开启广播输出
3. 厂家给的 Action 是否为 com.scanner.broadcast
4. 数据字段是否为 data
```

可以用 adb 模拟：

```bash
adb shell am broadcast -a com.scanner.broadcast --es data TEST123
```

查看日志：

```bash
adb logcat -s PrintUtilsNormalPrinter PrintUtilsBaoanPrinter
```
