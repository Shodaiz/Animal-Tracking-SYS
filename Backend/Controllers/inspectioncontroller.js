// Controllers/inspectionController.js
const db = require('../config/db');

exports.createInspection = async (req, res) => {
    try {
        const { 
            animalId, farmId, latitude, longitude, 
            locationDescription, result, fraudType, notes 
        } = req.body;
        
        const inspectorId = req.user.id; // L'ID de l'inspecteur connecté

        const query = `
            INSERT INTO Inspections 
            (InspectorId, AnimalId, FarmId, InspectionDate, Latitude, Longitude, 
             LocationDescription, Result, FraudType, Status, Notes) 
            VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, 'Pending', ?)`;
        
        const [resultRow] = await db.execute(query, [
            inspectorId, animalId || null, farmId || null, 
            latitude, longitude, locationDescription, 
            result, fraudType || 'None', notes
        ]);

        res.status(201).json({ 
            success: true, 
            message: "Inspection enregistrée", 
            inspectionId: resultRow.insertId 
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};