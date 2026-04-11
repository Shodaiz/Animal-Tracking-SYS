const jwt = require('jsonwebtoken');

module.exports = (req, res, next) => {
    // 1. Récupérer le token dans le header de la requête
    const token = req.header('Authorization')?.replace('Bearer ', '');

    if (!token) {
        return res.status(401).json({ message: "Accès refusé. Aucun token fourni." });
    }

    try {
        // 2. Vérifier si le token est valide avec ta clé secrète
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        
        // 3. Ajouter les infos de l'utilisateur à la requête pour que les contrôleurs sachent qui parle
        req.user = decoded; 
        
        next(); // Passer à l'étape suivante (le contrôleur)
    } catch (error) {
        res.status(400).json({ message: "Token invalide." });
    }
};