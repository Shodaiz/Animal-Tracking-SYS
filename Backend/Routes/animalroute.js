const express = require('express');
const router = express.Router();
const animalController = require('../Controllers/animalcontroller');
const auth = require('../middleware/auth');


router.get('/api/scan/:rfid', auth, animalController.getanimalByScan);
/**
 * @swagger
 * /api/animals/verify/{rfid}:
 * get:
 * summary: Vérifier un animal via son code RFID (Public)
 * tags: [Animals]
 * parameters:
 * - in: path
 * name: rfid
 * required: true
 * schema:
 * type: string
 * description: Le code RFID unique de l'animal
 * responses:
 * 200:
 * description: Informations sur l'animal et son état de santé
 * 404:
 * description: Animal non trouvé ou non enregistré
 */
router.post('/api/animals/register', auth, animalController.registerNewanimal);
router.patch('/api/animals/:id/verify', auth, animalController.verifyAnimal);
router.get('/api/animals/:id/vaccinations', auth, async (req, res) => {
    const [rows] = await db.execute('SELECT * FROM Vaccinations WHERE AnimalId = ?', [req.params.id]);
    res.json({ success: true, data: rows });
});

router.delete('/api/animals/:id', auth, animalController.deleteAnimal);
router.get('/api/animals', async (req, res) => {
    // Route pour la liste filtrée par proprio
    const Animal = require('./model/animal');
    try {
        const animals = await Animal.getAll(req.user.id, req.query.species);
        res.json({ success: true, data: animals });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});
router.patch('/api/animals/:id/move', auth, animalController.moveanimal);
router.get('/api/markets/verified', auth, animalController.getVerifiedMarkets);

// ROUTES SANTÉ
router.get('/api/animals/:id/health', auth, animalController.getHealthHistory);
router.post('/api/animals/health', auth, animalController.addHealthRecord);
router.put('/api/health-records/:recordId', auth, animalController.updateHealthRecord);


module.exports = router;