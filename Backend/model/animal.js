const db = require('../config/db');

class Animal {
    /**
     * Récupérer un animal par son code RFID unique
     * pour la fonction "Scan" de Flutter
     */
    static async findByRfid(rfidCode) {
        const query = `
            SELECT a.*, r.UniqueRfidCode, f.FarmName, o.FullOwnerName 
            FROM Animals a
            JOIN RfidTags r ON a.RfidTagId = r.Id
            JOIN Farms f ON a.CurrentFarmId = f.Id
            JOIN Owners o ON a.OwnerId = o.Id
            WHERE r.UniqueRfidCode = ?`; // Utilise les noms de colonnes du script SQL
        
        const [rows] = await db.execute(query, [rfidCode]);
        return rows[0];
    }

    /**
     * Enregistrer un nouvel animal (Naissance ou Acquisition)
     */
    static async create(data) {
        const query = `
            INSERT INTO Animals (
                RfidTagId, SpeciesName, BreedName, AnimalGender, 
                BirthDate, CurrentWeightKilograms, OriginType, 
                OwnerId, CurrentFarmId
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`;
        
        const params = [
            data.rfid_tag_id, data.species, data.breed, data.gender,
            data.birth_date, data.weight, data.origin_type,
            data.owner_id, data.farm_id
        ];

        const [result] = await db.execute(query, params);
        return result.insertId;
    }

    /**
     * Mettre à jour la ferme (Déclenche automatiquement le trigger de mouvement)
     */
    static async updateFarm(animalId, newFarmId) {
        const query = `UPDATE Animals SET CurrentFarmId = ? WHERE Id = ?`;
        const [result] = await db.execute(query, [newFarmId, animalId]);
        return result;
    }

    /** Récupérer l'historique de santé d'un animal
     * 
    */
    static async getHealthHistory(animalId) {
        const query = `
            SELECT h.*, v.VaccineName 
            FROM HealthRecords h
            LEFT JOIN Vaccinations v ON v.HealthRecordId = h.Id
            WHERE h.AnimalId = ?
            ORDER BY h.VisitTimestamp DESC`;
        
        const [rows] = await db.execute(query, [animalId]);
        return rows;
    }

static async getAll(ownerId, species = null) {
    let query = `
        SELECT a.Id, r.UniqueRfidCode, a.SpeciesName, a.BreedName, a.HealthStatus, f.FarmName 
        FROM Animals a
        JOIN RfidTags r ON a.RfidTagId = r.Id
        JOIN Farms f ON a.CurrentFarmId = f.Id
        WHERE a.LifeStatus = 'Alive' 
        AND a.OwnerId = ?`;
    
    let params = [ownerId];

    if (species) {
        query += " AND a.SpeciesName = ?";
        params.push(species);
    }

    const [rows] = await db.execute(query, params);
    return rows;
}
static async delete(id, ownerId) {
    // On vérifie que l'animal appartient bien à l'utilisateur avant de supprimer
    const query = `DELETE FROM Animals WHERE Id = ? AND OwnerId = ?`;
    const [result] = await db.execute(query, [id, ownerId]);
    return result.affectedRows > 0;
}

static async getAidSelection(ownerId) {
    const query = `
        SELECT a.Id, r.UniqueRfidCode, a.SpeciesName, a.BreedName, a.CurrentWeightKilograms, a.HealthStatus
        FROM Animals a
        JOIN RfidTags r ON a.RfidTagId = r.Id
        WHERE a.OwnerId = ? 
        AND a.LifeStatus = 'Alive'
        AND a.HealthStatus = 'Healthy'
        AND a.SpeciesName IN ('Mouton', 'Boeuf', 'Chèvre')
        AND DATEDIFF(NOW(), a.BirthDate) > 365`; // Plus d'un an
    
    const [rows] = await db.execute(query, [ownerId]);
    return rows;
}

static async delete(animalId, ownerId) {
    // On vérifie aussi l'ownerId pour être sûr qu'un éleveur ne supprime pas l'animal d'un autre
    const query = `DELETE FROM Animals WHERE Id = ? AND OwnerId = ?`;
    const [result] = await db.execute(query, [animalId, ownerId]);
    return result;
}
}

module.exports = Animal;