// server.js
require('dotenv').config({ path: './access.env' }); 
const express = require('express');
const cors = require('cors');
const swaggerJSDoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
//const auth = require('./middleware/auth');
const options = {
        definition: {
        openapi: '3.0.0',
        info : {
            title: 'API Animal tracking system',
            version: '1.0.0',
            description: 'Système de suivi animalier et détection de fraude',
        },
        servers: [ {
            url: 'http://localhost:3000',
        },],
        components: {
            securitySchemes: {
              bearerAuth: {
                type: 'http',
                scheme: 'bearer',
                bearerFormat: 'JWT',
              },
            },
          
        },
    },
    
    apis:['./Routes/*.js'],
}

const swaggerSpec = swaggerJSDoc(options);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));
// Import des Routes
const animalRoute = require('./Routes/animalroute');
const authRoutes = require('./Routes/authroutes');
const StatsRoute = require('./Routes/statsroute');
const inspectionRoute = require('./Routes/inspectionroutes');


const app = express();
app.use(cors());
app.use(express.json());

app.use('/api/auth', authRoutes);
app.use('/api/animals', animalRoute);
app.use('/api/inspections', inspectionRoute);
app.use('/api/stats', StatsRoute);


// aid l'adha option
//app.get('/api/special/aid-adha', auth, animalController.getAidAnimals);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Serveur lancé sur le port ${PORT}`);
});