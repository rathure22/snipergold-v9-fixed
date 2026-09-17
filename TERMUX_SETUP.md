# SniperGold v9 — Termux → GitHub

## Unpack
```bash
cd ~
mkdir -p snipergold-v9 && cd snipergold-v9
unzip -o /storage/emulated/0/Download/SniperGold_v9_Android_Compose_GitHub.zip -d .
```

## Verify Gradle
```bash
cat gradle/wrapper/gradle-wrapper.properties | grep 8.11
# distributionUrl=...gradle-8.11.1-bin.zip
```

## Git push
```bash
git init
git add -A
git commit -m "SniperGold v9 PATTERN+GUIDE - Compose Canvas - Gradle 8.11.1"
git branch -M main
git remote add origin https://github.com/YOUR_USER/snipergold-v9.git
git push -u origin main
# or force: git push -f origin main
```

## Open in Android Studio
Copy the folder to a PC or open via remote; sync Gradle 8.11.1.

Developer: Ji NG · SLOW-STONE™ v6
