@echo off
echo ========================================
echo   Systeme de Reservation Cooperative
echo ========================================
echo.

echo 1. Verification de Java...
java -version
if %errorlevel% neq 0 (
    echo ERREUR: Java n'est pas installe ou pas dans le PATH
    pause
    exit /b 1
)

echo.
echo 2. Verification de MySQL...
mysql --version
if %errorlevel% neq 0 (
    echo ATTENTION: MySQL n'est pas dans le PATH
    echo Assurez-vous que MySQL est installe et demarre
)

echo.tu
echo 3. Instructions de demarrage:
echo    - Assurez-vous que MySQL est demarre
echo    - Executez: mysql -u root -p ^< db/schema.sql
echo    - (Optionnel) Executez: mysql -u root -p ^< db/test-data.sql
echo    - Deployez le projet dans Tomcat
echo    - Accedez a: http://localhost:8080/jsp-reservation-cooperative/
echo.
echo 4. Compte par defaut:
echo    Utilisateur: admin
echo    Mot de passe: admin123
echo.

pause