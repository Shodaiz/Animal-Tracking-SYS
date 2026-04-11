const express = require('express');
const router = express.Router();
const MetadataController = require('../Controllers/metadatacontroller');
const auth = require('../middleware/auth'); 

/**
 * @swagger
 * tags:
 * name: Metadata
 * description: Statistiques globales et données utilitaires
 */

/**
 * @swagger
 * /api/metadata/dashboard-stats:
 * get:
 * summary: Récupère les statistiques pour le tableau de bord (Admin/Inspecteur)
 * description: Retourne le nombre total d'animaux, de fermes et d'inspections réalisées.
 * tags: [Metadata]
 * security:
 * - bearerAuth: []
 * responses:
 * 200:
 * description: Statistiques récupérées avec succès
 * content:
 * application/json:
 * schema:
 * type: object
 * properties:
 * success: { type: boolean }
 * data:
 * type: object
 * properties:
 * animals: { type: integer, example: 1250 }
 * farms: { type: integer, example: 45 }
 * inspections: { type: integer, example: 320 }
 * 401:
 * description: Token invalide ou manquant
 */
// ROUTES DASHBOARD
router.get('/api/dashboard/stats', auth, MetadataController.getDashboardStats);
router.get('/api/metadata/options', auth, MetadataController.getFormOptions);

// ROUTE RAPPORTS
router.get('/api/reports/activity', auth, async (req, res) => {
    const Metadata = require('../model/metadata');
    try {
        const report = await Metadata.getActivityReport(req.user.id);
        res.json({ success: true, data: report });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});


module.exports = router;