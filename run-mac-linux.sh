#!/bin/bash
cd "$(dirname "$0")"
echo "Starting MediSphere AI... open http://localhost:8080 in ~30 seconds. Ctrl+C to stop."
java -jar medisphere-ai.jar --spring.profiles.active=h2
