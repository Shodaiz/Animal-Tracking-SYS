const express = require('express');
const router = express.Router();
const authController = require('../Controllers/authcontroller');
const auth = require('../middleware/auth');

/**
 * @swagger
 * /api/auth/login:
 * post:
 * summary: Connexion utilisateur
 * tags: [Auth]
 */
router.post('/login', auth, authController.login);

router.post('/register', auth, authController.register);

router.patch('/profile', auth, authController.updateProfile);

router.patch('/change-password', auth, authController.changePassword);

module.exports = router;