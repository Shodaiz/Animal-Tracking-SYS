class AnimalModel {
  final int Id;
  final String? UniqueRfidCode;
  final String SpeciesName;
  final String LifeStatus;
  final bool HasMovementPermit;
  final String HealthStatus;
  final String CurrentFarmName;

  AnimalModel({
    required this.Id,
    this.UniqueRfidCode,
    required this.SpeciesName,
    required this.LifeStatus,
    required this.HasMovementPermit,
    required this.HealthStatus,
    required this.CurrentFarmName,
  });

  factory AnimalModel.fromJson(Map<String, dynamic> json) {
    return AnimalModel(
      Id: json['Id'],
      UniqueRfidCode: json['RfidTag']?['UniqueRfidCode'],
      SpeciesName: json['SpeciesName'],
      LifeStatus: json['LifeStatus'],
      HasMovementPermit: json['HasMovementPermit'],
      HealthStatus: json['HealthStatus'],
      CurrentFarmName: json['Farm']['FarmName'],
    );
  }
}