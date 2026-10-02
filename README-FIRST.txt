MEDISPHERE AI - quick start
===========================
You need ONE thing installed: Java 21 (or newer).
  Get it free: https://adoptium.net  (choose "Temurin 21", run the installer)
  Check it:    open a terminal / Command Prompt and type:  java -version   (must say 21 or higher)

RUN
  Windows:    double-click RUN-WINDOWS.bat
  Mac/Linux:  in a terminal:  chmod +x run-mac-linux.sh && ./run-mac-linux.sh

Wait ~30 seconds until you see "Started MedisphereApplication", then open:
  http://localhost:8080

LOGIN (all demo accounts)
  Admin          admin@medisphere.ai            Admin123!
  Doctor         dr.arun@medisphere.ai          Doctor123!   (dr.mehta@..., dr.patel@... same password)
  Nurse          nurse.taylor@medisphere.ai     Nurse123!
  Receptionist   reception.jones@medisphere.ai  Reception123!
  Pharmacist     pharmacist.lee@medisphere.ai   Pharmacy123!
  Lab Technician labtech.brown@medisphere.ai    LabTech123!
  Patient        john.doe@example.com           Patient123!  (mary.jane@example.com same)

NOTES
  - Data is stored in memory: it resets to fresh demo data every time you restart.
  - Port 8080 must be free. Firewall prompt on first run: Allow (local only is fine).
  - API docs: http://localhost:8080/swagger-ui.html
  - Full source code is in the "source" folder (see its README.md).
