import 'dart:developer';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:print_plugin/print_plugin.dart';

void main() {
  log('message');
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  final _printPlugin = PrintPlugin();

  bool couldUse = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    couldUse = await _printPlugin.initPrint();

    _printPlugin.setUpMethodCallHandler((res) {
      log(res.toString());
    });
  }

  Future<void> printTest() async {
    await _printPlugin.printTest();
  }

  Future<void> printText() async {
    await _printPlugin.printText({
      'paperWidth': 52,
      'text':
          '药品:阿莫西林胶囊\n有效期:2024-12-12\n规格:10mg\n车码:87hfsxzg92100\n位置:第一层\n批号:AM001\n数量:2'
    });
  }

  Future<void> printQRCode() async {
    await _printPlugin.printQRCode({
      'paperWidth': 52,
      'paperHeight': 70,
      'size': 0,
      'codeSize': 210,
      'data': 'hello world hello'
    });
  }

  Future<void> openScan() async {
    await _printPlugin.openScan({});
  }

  // 50 - lineSpacing 30 - size 25 // 30 lineSpacing 15 - size 21 // 70 lineSpacing 40 - size 30
  Future<void> textAsBitmap() async {
    await _printPlugin.textAsBitmap({
      'paperWidth': 52,
      'paperHeight': 30,
      'lineSpacing': 15,
      'size': 21,
      'text':
          '药品:阿莫西林胶囊\n车码:87hfsxzg92100\n有效期:2024-12-12\n规格:10mg\n批号:AM001\n数量:1\n药品类型:普通',
      'qrCodeStr':
          '药品:阿莫西林胶囊\n有效期:2024-12-12\n规格:10mg\n车码:87hfsxzg92100\n批号:AM001\n数量:1\n药品类型:普通'
    });
  }

  Future<void> labelEnable(bool enable) async {
    await _printPlugin.labelEnable({
      'enable': enable,
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('打印'),
        ),
        body: Container(
          child: Column(
            children: [
              GestureDetector(
                onTap: () {
                  log(couldUse.toString());
                  if (couldUse == false) {
                    return;
                  }
                  this.printTest();
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '打印自检',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                onTap: () {
                  log(couldUse.toString());
                  if (couldUse == false) {
                    return;
                  }
                  this.printText();
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '打印文字',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                onTap: () {
                  if (couldUse == false) {
                    return;
                  }
                  this.labelEnable(true);
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '关闭黑标',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                onTap: () {
                  log(couldUse.toString());
                  if (couldUse == false) {
                    return;
                  }
                  this.labelEnable(false);
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '开启黑标',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                onTap: () {
                  log(couldUse.toString());
                  if (couldUse == false) {
                    return;
                  }
                  this.printQRCode();
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '打印二维码',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                onTap: () {
                  if (couldUse == false) {
                    return;
                  }
                  this.textAsBitmap();
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '打印测试',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
              GestureDetector(
                onTap: () {
                  // if (couldUse == false) {
                  //   return;
                  // }
                  this.openScan();
                },
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  margin: EdgeInsets.only(left: 12, right: 12, bottom: 10),
                  padding:
                      EdgeInsets.only(left: 20, right: 20, top: 10, bottom: 10),
                  decoration: BoxDecoration(
                    color: Colors.blue,
                    borderRadius: BorderRadius.circular(5.0),
                  ),
                  child: Center(
                    child: Text(
                      '扫描二维码',
                      style: TextStyle(color: Colors.white),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
