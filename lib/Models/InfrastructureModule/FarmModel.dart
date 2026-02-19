class FarmModel {
  final int Id;
  final String FarmName;
  final String? NationalRegistryNumber;
  final bool IsOfficiallyDeclared;
  final double LatitudeCoordinate;
  final double LongitudeCoordinate;
  final String CommuneName;

  FarmModel({
    required this.Id,
    required this.FarmName,
    this.NationalRegistryNumber,
    required this.IsOfficiallyDeclared,
    required this.LatitudeCoordinate,
    required this.LongitudeCoordinate,
    required this.CommuneName,
  });

  factory FarmModel.fromJson(Map<String, dynamic> json) {
    return FarmModel(
      Id: json['Id'],
      FarmName: json['FarmName'],
      NationalRegistryNumber: json['NationalRegistryNumber'],
      IsOfficiallyDeclared: json['IsOfficiallyDeclared'],
      LatitudeCoordinate: double.parse(json['LatitudeCoordinate'].toString()),
      LongitudeCoordinate: double.parse(json['LongitudeCoordinate'].toString()),
      CommuneName: json['Commune']['CommuneName'],
    );
  }
}