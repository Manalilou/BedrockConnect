# 🚀 Guide Rapide - BedrockConnect

## Pour les Pressés

### Installation et Lancement
```bash
cd serverlist-server
mvn clean package
java -jar target/BedrockConnect-1.0-SNAPSHOT.jar
```

### Vérifier que Tout Va Bien
```bash
./scripts/health-check.sh
```

---

## Tâches Courantes

### 📸 Changer une Image

**Fichier :** `serverlist-server/src/main/resources/images.json`

```json
{
  "nationsglory": {
    "hub": "https://nouvelle-url.com/Hub.png"  ← Modifiez ici
  }
}
```

Redémarrez le serveur. **C'est tout !** ✅

---

### 🆕 Ajouter une Nouvelle Version Minecraft

**Option 1 - Automatique (Recommandé)**
```bash
./scripts/add-minecraft-version.sh
# Suivez les instructions
```

**Option 2 - Manuel**
Consultez **MAINTENANCE.md** section "Processus de Mise à Jour"

---

### 🔍 Vérifier les Mises à Jour Disponibles
```bash
./scripts/check-minecraft-updates.sh
```

Faites-le toutes les 2-4 semaines ! ⏰

---

### ➕ Ajouter un Nouveau Serveur Featured

**1. Ajoutez l'image dans `images.json`**
```json
{
  "nationsglory": {
    "nouveau": "https://url.com/image.png"
  }
}
```

**2. Modifiez `ServerManager.java`**
```java
addServer(featured, "Nouveau Serveur", "address.com", 19132, 
    BedrockConnect.getConfig().getImageAssets().getNationsGloryIcon("nouveau"));
```

**3. Recompilez**
```bash
mvn clean package
```

---

## 🐛 Problèmes Courants

### "Image ne se charge pas"
```bash
# Vérifiez que images.json est valide
cat serverlist-server/src/main/resources/images.json | jq

# Recompilez
cd serverlist-server && mvn clean package
```

### "Version Minecraft non supportée"
```bash
# Utilisez l'assistant
./scripts/add-minecraft-version.sh

# Ou consultez MAINTENANCE.md
```

### "Erreur de compilation Maven"
```bash
cd serverlist-server
mvn clean
mvn install -U  # Force update
mvn package
```

---

## 📁 Fichiers Importants

| Fichier | Quand le Modifier |
|---------|-------------------|
| `images.json` | Changer/ajouter des images |
| `language.json` | Modifier les textes affichés |
| `minecraft-versions.json` | Documenter nouvelles versions |
| `servers.json` | Modifier les serveurs featured |

---

## 🛠️ Commandes Utiles

```bash
# Compiler
mvn clean package

# Lancer avec debug
java -jar target/BedrockConnect-1.0-SNAPSHOT.jar debug=true

# Vérifier santé du projet
./scripts/health-check.sh

# Chercher une version spécifique
grep -A 4 "minecraft_version.*1.21" serverlist-server/src/main/resources/minecraft-versions.json
```

---

## 📚 Pour Aller Plus Loin

- **Documentation Complète :** README-IMPROVED.md
- **Guide de Maintenance :** MAINTENANCE.md
- **Problème ?** Ouvrez une issue sur GitHub

---

## ⚡ Workflow de Mise à Jour Minecraft

```
1. ./scripts/check-minecraft-updates.sh
2. ./scripts/add-minecraft-version.sh
3. Appliquer les modifications suggérées
4. mvn clean package
5. Tester
6. git commit -m "Support Minecraft X.XX.XX"
```

**Durée totale : ~10 minutes** ⏱️

---

**💡 Conseil Pro :** Ajoutez un cron job pour vérifier les updates automatiquement :
```bash
# Tous les lundis à 10h
0 10 * * 1 cd /path/to/bedrockconnect && ./scripts/check-minecraft-updates.sh
```
