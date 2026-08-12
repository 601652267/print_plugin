# print_plugin

A new Flutter project.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter development, view the
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

## Android vendor flavors

The Android plugin uses a `printer` flavor dimension to isolate vendor SDKs:

- `normalPrinter`: current QS601 SDK implementation
- `baoanPrinter`: placeholder implementation for the Baoan printer SDK

Apps that use their own customer flavor dimension can map each customer to one
printer vendor with `missingDimensionStrategy`:

```gradle
android {
    flavorDimensions "client"

    productFlavors {
        normal {
            dimension "client"
            missingDimensionStrategy "printer", "normalPrinter"
        }

        baoanPrinter {
            dimension "client"
            missingDimensionStrategy "printer", "baoanPrinter"
        }
    }
}
```

Build examples:

```bash
flutter build apk --flavor normal --release -t lib/main_normal.dart
flutter build apk --flavor baoanPrinter --release -t lib/main_baoan_printer.dart
```
