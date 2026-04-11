const Animal = require('../model/animal');

/**
 * Gère le scan d'un tag RFID depuis le mobile
 * Route: GET /api/animals/scan/:rfid
 */
exports.getanimalByScan = async (req, res) => {
    try {
        const rfidCode = req.params.rfid; // Récupère le code scanné
        const animal = await Animal.findByRfid(rfidCode);

        if (!animal) {
            return res.status(404).json({ 
                success: false, 
                message: "Aucun animal trouvé pour ce tag RFID." 
            });
        }

        // Renvoie toutes les infos nécessaires à l'affichage mobile 
        res.status(200).json({
            success: true,
            data: animal
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

/**
 * Enregistre un nouvel animal (Cycle de vie: Birth/Acquisition)
 * Route: POST /api/animals/register
 */
exports.registerNewanimal = async (req, res) => {
    try {
        // Les données envoyées par l'app Flutter via le body
        const animalId = await Animal.create(req.body);

        res.status(201).json({
            success: true,
            message: "animal enregistré avec succès et tag assigné.",
            animalId: animalId
        });
    } catch (error) {
        // Si le trigger SQL bloque (ex: tag défectueux), l'erreur sera captée ici
        res.status(400).json({ 
            success: false, 
            message: "Erreur d'enregistrement : " + error.message 
        });
    }
};


/**
 * Met à jour la ferme de l'animal (Mouvement)
 * Route: PATCH /api/animals/:id/move
 */
exports.moveanimal = async (req, res) => {
    try {
        const { id } = req.params;
        const { newFarmId } = req.body;

        await animal.updateFarm(id, newFarmId);

        // Note: Le trigger SQL "BeforeFarmChange" a déjà créé le log de mouvement en BDD
        res.status(200).json({
            success: true,
            message: "Mouvement enregistré et ferme mise à jour."
        });
    } catch (error) {
        res.status(400).json({ success: false, error: error.message });
    }
};

/**
 * Récupère l'historique de santé
 * Route: GET /api/animals/:id/health
 */
exports.getHealthHistory = async (req, res) => {
    try {
        const { id } = req.params;
        const history = await Animal.getHealthHistory(id);

        res.status(200).json({
            success: true,
            data: history // Liste des records types: Vaccination, Checkup, etc. [cite: 96]
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }

};
exports.getAnimalDetails = async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await db.execute(`
            SELECT a.*, r.UniqueRfidCode, f.FarmName, o.FullOwnerName 
            FROM Animals a
            JOIN RfidTags r ON a.RfidTagId = r.Id
            JOIN Farms f ON a.CurrentFarmId = f.Id
            JOIN Owners o ON a.OwnerId = o.Id
            WHERE a.Id = ?`, [id]);

        if (rows.length === 0) return res.status(404).json({ message: "Animal non trouvé" });
        
        res.json({ success: true, data: rows[0] });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};
exports.addHealthRecord = async (req, res) => {
    // Sécurité : On vérifie le rôle provenant du Token JWT
    if (req.user.role !== 'veterinaire') {
        return res.status(403).json({ message: "Seul un vétérinaire peut modifier le dossier médical." });
    }

    try {
        const { animalId, status, notes } = req.body;
        // 1. Mettre à jour l'état de l'animal
        await db.execute('UPDATE Animals SET HealthStatus = ? WHERE Id = ?', [status, animalId]);
        // 2. Créer la ligne d'historique
        await db.execute('INSERT INTO HealthRecords (AnimalId, HealthStatus, DiagnosisNotes, CheckupDate) VALUES (?, ?, ?, NOW())', 
        [animalId, status, notes]);

        res.json({ success: true, message: "Dossier médical mis à jour." });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }

};
exports.updateHealthRecord = async (req, res) => {
    // Seul le vétérinaire peut modifier
    if (req.user.role !== 'veterinaire') {
        return res.status(403).json({ message: "Accès réservé aux vétérinaires." });
    }

    try {
        const { recordId } = req.params; // L'ID de la ligne dans HealthRecords
        const { status, notes } = req.body;

        const query = `UPDATE HealthRecords SET HealthStatus = ?, DiagnosisNotes = ? WHERE Id = ?`;
        await db.execute(query, [status, notes, recordId]);

        res.json({ success: true, message: "Fiche médicale mise à jour." });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

// Pour eid_home_screen.dart (Marchés vérifiés)
exports.getVerifiedMarkets = async (req, res) => {
    try {
        const query = `SELECT Id, Name, Location, Latitude, Longitude FROM VerifiedMarkets WHERE IsActive = 1`;
        const [markets] = await db.execute(query);
        res.json({ success: true, data: markets });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

// Pour map_screen.dart (Points d'incidents/fraudes)
exports.getMapIncidents = async (req, res) => {
    try {
        // Récupère les signalements récents pour les afficher sur la carte
        const query = `
            SELECT Id, Type, Latitude, Longitude, Description 
            FROM Reports 
            WHERE CreatedAt > DATE_SUB(NOW(), INTERVAL 30 DAY)`;
        const [incidents] = await db.execute(query);
        res.json({ success: true, data: incidents });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};
exports.verifyAnimal = async (req, res) => {
    // Seul un admin ou vétérinaire peut valider
    if (req.user.role === 'farmer') return res.status(403).send("Non autorisé");
    
    await db.execute('UPDATE Animals SET isVerified = 1 WHERE Id = ?', [req.params.id]);
    res.json({ success: true, message: "Animal certifié" });
};

exports.deleteAnimal = async (req, res) => {
    try {
        const animalId = req.params.id;
        const ownerId = req.user.id; // Récupéré via le token JWT (auth middleware)

        const result = await Animal.delete(animalId, ownerId);

        if (result.affectedRows === 0) {
            return res.status(404).json({ 
                success: false, 
                message: "Animal non trouvé ou vous n'avez pas l'autorisation." 
            });
        }

        res.json({ 
            success: true, 
            message: "L'animal a été supprimé avec succès." 
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};
// Vérification publique pour le citoyen
exports.verifyPublicAnimal = async (req, res) => {
    const { rfid } = req.params;
    try {
        const query = `
            SELECT a.Id, a.Breed, a.Status, f.Name as FarmName, 
            (SELECT COUNT(*) FROM Vaccinations v WHERE v.AnimalId = a.Id) as VaccinCount
            FROM Animals a
            JOIN Farms f ON a.FarmId = f.Id
            WHERE a.RFID = ?`;
        const [rows] = await db.execute(query, [rfid]);
        
        if (rows.length === 0) {
            return res.status(404).json({ verified: false, message: "Animal inconnu du système national" });
        }
        res.json({ verified: true, data: rows[0] });
    } catch (e) { res.status(500).send(e.message); }
};