// model/Metadata.js
const db = require('../config/db');

class Metadata {
    // Liste des fermes pour le menu déroulant
    static async getFarms() {
        const [rows] = await db.execute('SELECT Id, FarmName FROM Farms');
        return rows;
    }

    // Liste des propriétaires
    static async getOwners() {
        const [rows] = await db.execute('SELECT Id, FullOwnerName FROM Owners');
        return rows;
    }

    // Liste des tags RFID DISPONIBLES (ceux qui ne sont pas encore sur un animal)
    static async getAvailableTags() {
        const query = `
            SELECT Id, UniqueRfidCode 
            FROM RfidTags 
            WHERE TagStatus = 'InStock'`;
        const [rows] = await db.execute(query);
        return rows;
    }

static async getDashboardStats() {
    // Compte le total d'animaux vivants
    const [total] = await db.execute('SELECT COUNT(*) as count FROM Animals WHERE LifeStatus = "Alive"');
    // Compte les alertes de santé (ex: statut 'Sick')
    const [alerts] = await db.execute('SELECT COUNT(*) as count FROM Animals WHERE HealthStatus = "Sick"');
    // Compte les mouvements récents (ex: les 7 derniers jours)
    const [moves] = await db.execute('SELECT COUNT(*) as count FROM Movements WHERE MovementDate > NOW() - INTERVAL 7 DAY');

    return {
        totalAnimals: total[0].count,
        healthAlerts: alerts[0].count,
        recentMovements: moves[0].count
    };
}
static async getUserFarms(ownerId) {
    const query = `
        SELECT Id as id, FarmName as name, Location as location, 
               Latitude as latitude, Longitude as longitude, 
               Capacity as capacity, Status as status, IsVerified as is_verified
        FROM Farms WHERE OwnerId = ?`;
    const [rows] = await db.execute(query, [ownerId]);
    return rows;
}

static async getActivityReport(ownerId) {
    const query = `
        (SELECT 'Mouvement' as type, m.MovementDate as date, a.SpeciesName as detail
         FROM Movements m
         JOIN Animals a ON m.AnimalId = a.Id
         WHERE a.OwnerId = ?)
        UNION ALL
        (SELECT 'Santé' as type, h.CheckupDate as date, h.HealthStatus as detail
         FROM HealthRecords h
         JOIN Animals a ON h.AnimalId = a.Id
         WHERE a.OwnerId = ?)
        ORDER BY date DESC LIMIT 20`;
    
    const [rows] = await db.execute(query, [ownerId, ownerId]);
    return rows;
}
}

module.exports = Metadata;