const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const User = require('../model/user');
const db = require('../config/db'); // Import nécessaire pour updateProfile

exports.login = async (req, res) => {
    const { email, password } = req.body;
    try {
        const user = await User.findByEmail(email);
        if (!user) return res.status(404).json({ message: "Utilisateur non trouvé" });

        // ATTENTION : Vérifie si c'est user.Password ou user.PasswordHash dans ta DB
        const isMatch = await User.comparePassword(password, user.PasswordHash || user.Password);
        if (!isMatch) return res.status(401).json({ message: "Mot de passe incorrect" });

        const token = jwt.sign(
            { id: user.Id, role: user.UserRole },
            process.env.JWT_SECRET,
            { expiresIn: '24h' }
        );

        res.json({ success: true, token, user: { name: user.FullOwnerName, role: user.UserRole } });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.register = async (req, res) => {
    try {
        const userId = await User.create(req.body);
        res.status(201).json({ success: true, userId });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

exports.updateProfile = async (req, res) => {
    try {
        const { name, email, phone } = req.body;
        const userId = req.user.id;
        const query = `UPDATE Users SET FullName = ?, Email = ?, PhoneNumber = ? WHERE Id = ?`;
        await db.execute(query, [name, email, phone, userId]);
        res.json({ success: true, message: "Profil mis à jour" });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};

exports.changePassword = async (req, res) => {
    try {
        const { oldPassword, newPassword } = req.body;
        const userId = req.user.id;
        const [users] = await db.execute('SELECT PasswordHash FROM Users WHERE Id = ?', [userId]);
        const isMatch = await bcrypt.compare(oldPassword, users[0].PasswordHash);
        if (!isMatch) return res.status(400).json({ message: "Ancien mot de passe incorrect" });

        const salt = await bcrypt.genSalt(10);
        const newHash = await bcrypt.hash(newPassword, salt);
        await db.execute('UPDATE Users SET PasswordHash = ? WHERE Id = ?', [newHash, userId]);
        res.json({ success: true, message: "Mot de passe modifié" });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
};