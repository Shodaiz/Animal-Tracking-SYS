
const Metadata = require('../model/metadata');

exports.getFormOptions = async (req, res) => {
    try {
        const [farms, owners, tags] = await Promise.all([
            Metadata.getFarms(),
            Metadata.getOwners(),
            Metadata.getAvailableTags()
        ]);

        res.json({
            success: true,
            data: { farms, owners, tags }
        });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
    // Controllers/metadatacontroller.js
}
exports.getDashboardStats = async (req, res) => {
    try {
        const stats = await Metadata.getDashboardStats();
        res.json({ success: true, data: stats });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    };
};
