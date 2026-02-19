import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'Core/Themes/AppTheme.dart';
import 'Providers/MapProvider.dart';
import 'Screens/Map/NationalMapScreen.dart';

void main() {
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => MapProvider()),
      ],
      child: MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'National Animal Tracking',
      theme: AppTheme.LightTheme,
      debugShowCheckedModeBanner: false,
      home: NationalMapScreen(), // Lancement direct sur la Carte
    );
  }
}