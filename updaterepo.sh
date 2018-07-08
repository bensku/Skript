#!/bin/bash
./gradlew publish
cd ../ItemAPI/repo #This is due to me having strange file structure...
git add ch
git commit -m "Update Skript to latest source version"
git push repo master
