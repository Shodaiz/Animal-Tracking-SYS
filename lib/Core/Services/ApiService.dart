import 'package:dio/dio.dart';
import '../Core/Constants/ApiConstants.dart';

class ApiService {
  final Dio _dio = Dio(BaseOptions(baseUrl: ApiConstants.BaseUrl));

  // Récupérer toutes les fermes pour la carte (Anti-Fraude)
  Future<List<dynamic>> getAllNationalFarms() async {
    try {
      final response = await _dio.get(ApiConstants.GetNationalFarms);
      return response.data;
    } catch (e) {
      throw Exception("Erreur lors de la récupération des fermes nationales");
    }
  }

  // Scanner un animal par RFID
  Future<Map<String, dynamic>> scanAnimal(String rfidCode) async {
    try {
      final response = await _dio.get("${ApiConstants.ScanAnimalByRfid}$rfidCode");
      return response.data;
    } catch (e) {
      throw Exception("Animal non répertorié - Fraude suspectée");
    }
  }
}