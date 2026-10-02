@echo off
title MediSphere AI
echo Starting MediSphere AI... wait about 30 seconds, then open http://localhost:8080
echo (Close this window to stop the app.)
start "" /min cmd /c "timeout /t 30 >nul & start http://localhost:8080"
java -jar "%~dp0medisphere-ai.jar" --spring.profiles.active=h2
pause
