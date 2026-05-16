# Guide d'Installation - Système de Réservation Coopérative

## 🚀 Installation Rapide

### 1. Prérequis
- **Java JDK 11+** installé
- **Apache Tomcat 9+** installé
- **MySQL 8.0+** installé et démarré
- **Maven** (optionnel, pour compilation)

### 2. Configuration MySQL

#### Étape 1 : Créer la base de données
```bash
# Connectez-vous à MySQL
mysql -u root -p

# Exécutez le script de création
source db/schema.sql

# (Optionnel) Insérez des données de test
source db/test-data.sql
```

#### Étape 2 : Vérifier la configuration
Modifiez `src/main/java/com/cooperative/util/DBConnection.java` si nécessaire :
```java
private static final String URL = "jdbc:mysql://localhost:3306/cooperative_reservation?serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "votre_mot_de_passe_mysql";
```

### 3. Déploiement sur Tomcat

#### Option A : Déploiement direct (Développement)
1. Copiez le dossier du projet dans `webapps/` de Tomcat
2. Renommez-le en `jsp-reservation-cooperative`
3. Démarrez Tomcat

#### Option B : Compilation WAR (Production)
```bash
# Compilation avec Maven
mvn clean package

# Copiez le WAR généré
cp target/jsp-reservation-cooperative.war $TOMCAT_HOME/webapps/
```

### 4. Premier Démarrage

1. **Démarrez Tomcat**
   ```bash
   $TOMCAT_HOME/bin/startup.sh  # Linux/Mac
   $TOMCAT_HOME/bin/startup.bat # Windows
   ```

2. **Accédez à l'application**
   ```
   http://localhost:8080/jsp-reservation-cooperative/
   ```

3. **Connectez-vous avec le compte admin**
   - Utilisateur : `admin`
   - Mot de passe : `admin123`

## 🔧 Résolution des Problèmes

### Erreur de connexion MySQL
- Vérifiez que MySQL est démarré
- Vérifiez les paramètres de connexion dans `DBConnection.java`
- Assurez-vous que la base `cooperative_reservation` existe

### Erreur 404 sur l'application
- Vérifiez que Tomcat est démarré
- Vérifiez que l'application est déployée dans `webapps/`
- Consultez les logs Tomcat : `$TOMCAT_HOME/logs/catalina.out`

### Problème de compilation
- Vérifiez la version Java (JDK 11+)
- Vérifiez que Maven est installé
- Nettoyez et recompilez : `mvn clean compile`

## 📁 Structure des Fichiers Importants

```
jsp-reservation-cooperative/
├── db/
│   ├── schema.sql          # Schéma de base de données
│   └── test-data.sql       # Données de test
├── src/main/
│   ├── java/com/cooperative/
│   │   ├── dao/            # Accès aux données
│   │   ├── model/          # Modèles
│   │   ├── servlet/        # Contrôleurs
│   │   └── util/
│   │       └── DBConnection.java  # Configuration DB
│   └── webapp/
│       ├── *.jsp           # Pages web
│       └── css/            # Styles
├── pom.xml                 # Configuration Maven
└── README.md               # Documentation
```

## 🎯 Prochaines Étapes

1. **Testez l'authentification** avec le compte admin
2. **Créez des voitures** dans la section gestion
3. **Ajoutez des clients** 
4. **Effectuez des réservations**
5. **Générez des rapports PDF**

## 📞 Support

En cas de problème, vérifiez :
1. Les logs Tomcat
2. Les logs MySQL
3. La configuration de la base de données
4. Les permissions des fichiers