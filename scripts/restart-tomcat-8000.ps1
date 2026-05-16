$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path $projectRoot)) { $projectRoot = "c:\Users\ASUS\Videos\JSP" }

$javaHome = Join-Path $projectRoot ".tools\jdk-17"
$mvn = Join-Path $projectRoot ".tools\apache-maven-3.9.9\bin\mvn.cmd"

$conn = Get-NetTCPConnection -LocalPort 8000 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn) {
    Stop-Process -Id $conn.OwningProcess -Force -ErrorAction Stop
    Start-Sleep -Seconds 2
}

$env:JAVA_HOME = $javaHome
Set-Location $projectRoot
& $mvn package -DskipTests -q
& $mvn org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run-war-only "-Dmaven.tomcat.port=8000"
