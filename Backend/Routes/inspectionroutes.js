const express = require('express');
const router = express.Router();
const inspectionController = require('../Controllers/inspectioncontroller');    
const auth = require('../middleware/auth');


/**
* @swagger
* tags:
* name: inspections
* description: Opérations de contrôle
*/

/**
* @swagger
* /api/inspections:
* post:
* summary: Créer un rapport d'inspection
* tags: [Inspections]
* security:
* - bearerAuth: []
* responses:
* 201:
* description: Succès
*/
router.post('/api/inspections', auth, inspectionController.createInspection);

module.exports = router;