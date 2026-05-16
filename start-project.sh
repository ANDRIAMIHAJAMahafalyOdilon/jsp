#!/bin/bash

echo "========================================"
echo "  Système de Réservation Coopérative"
echo "========================================"
echo

echo "1. Vérification de Java..."
java -version
if [ $? -ne 0 ]; then
    echo "ERREUR: Java n'est pas installé ou pas dans le PATH"
    exit 1
fi

echo
echo "2. Vérification de MySQL..."
mysql --version
if [ $? -ne 0 ]; then
    echo "ATTENTION: MySQL n'est pas dans le PATH"
    echo "Assurez-vous que MySQL est installé et démarré"
fi

echo
echo "3. Instructions de démarrage:"
echo "   - Assurez-vous que MySQL est démarré"
echo "   - Exécutez: mysql -u root -p < db/schema.sql"
echo "   - (Optionnel) Exécutez: mysql -u root -p < db/test-data.sql"
echo "   - Déployez le projet dans Tomcat"
echo "   - Accédez à: http://localhost:8080/jsp-reservation-cooperative/"
echo
echo "4. Compte par défaut:"
echo "   Utilisateur: admin"
echo "   Mot de passe: admin123"
echo

read -p "Appuyez sur Entrée pour continuer..."