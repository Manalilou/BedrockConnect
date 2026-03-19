# 🎮 BedrockConnect - Projet Refactorisé

## 📦 Contenu de ce Package

Bienvenue dans votre projet BedrockConnect amélioré ! Ce package contient tous les fichiers nécessaires pour un projet plus maintenable et facile à gérer.

---

## 📖 Par Où Commencer ?

### Si vous êtes pressé :
👉 **Lisez `QUICKSTART.md`** - Guide rapide avec les tâches courantes

### Si vous voulez comprendre :
👉 **Lisez `README-IMPROVED.md`** - Documentation complète du projet

### Si vous devez faire une mise à jour :
👉 **Lisez `MAINTENANCE.md`** - Guide détaillé de maintenance

### Pour voir ce qui a changé :
👉 **Lisez `CHANGES.md`** - Résumé complet des améliorations

---

## 📁 Structure des Fichiers

```
bedrockconnect-refactored/
│
├── 📚 Documentation
│   ├── README-IMPROVED.md       ← Documentation complète
│   ├── QUICKSTART.md            ← Guide rapide
│   ├── MAINTENANCE.md           ← Guide de maintenance
│   ├── CHANGES.md               ← Résumé des changements
│   └── INDEX.md                 ← Ce fichier
│
├── 🛠️ Scripts d'Aide
│   └── scripts/
│       ├── check-minecraft-updates.sh    ← Vérifier les updates
│       ├── add-minecraft-version.sh      ← Ajouter une version MC
│       └── health-check.sh               ← Vérifier le projet
│
└── 💻 Code Source
    └── serverlist-server/
        ├── pom.xml                       ← Configuration Maven
        └── src/main/
            ├── com/pyratron/pugmatt/bedrockconnect/
            │   ├── Config.java           ← ✅ Modifié
            │   ├── config/
            │   │   ├── ImageAssets.java  ← ✨ Nouveau
            │   │   └── Custom/
            │   │       └── CustomEntry.java  ← ✅ Modifié
            │   └── server/
            │       ├── ServerManager.java    ← ✅ Modifié
            │       └── gui/
            │           └── UIForms.java      ← ✅ Modifié
            └── resources/
                ├── images.json           ← ✨ Nouveau (URLs d'images)
                ├── minecraft-versions.json   ← ✨ Nouveau (versions MC)
                └── language.json         ← Existant
```

---

## 🚀 Installation Rapide

```bash
# 1. Vérifier que tout est OK
./scripts/health-check.sh

# 2. Compiler
cd serverlist-server
mvn clean package

# 3. Lancer
java -jar target/BedrockConnect-1.0-SNAPSHOT.jar
```

---

## ✨ Principales Améliorations

### 1. 🖼️ Images Centralisées
- **Avant :** URLs éparpillées dans 5 fichiers Java
- **Après :** Un seul fichier `images.json`
- **Gain :** Changer une image en 30 secondes au lieu de 5-10 minutes

### 2. 🔄 Maintenance Facilitée
- **Nouveau :** Scripts automatisés pour les mises à jour
- **Nouveau :** Documentation étape par étape
- **Gain :** Update en 10 minutes au lieu de 30-45 minutes

### 3. 📝 Documentation Complète
- Guide rapide pour démarrer vite
- Guide détaillé pour tout comprendre
- Scripts auto-documentés

---

## 🎯 Cas d'Usage Courants

### Changer le Logo d'un Serveur
```bash
# Éditez images.json
nano serverlist-server/src/main/resources/images.json

# Redémarrez le serveur (pas besoin de recompiler !)
```

### Ajouter une Nouvelle Version Minecraft
```bash
# Lancez l'assistant
./scripts/add-minecraft-version.sh

# Suivez les instructions générées
```

### Vérifier les Mises à Jour Disponibles
```bash
./scripts/check-minecraft-updates.sh
```

---

## 📊 Fichiers Clés

| Fichier | Rôle | Fréquence de Modification |
|---------|------|---------------------------|
| `images.json` | URLs des images | Occasionnelle |
| `language.json` | Traductions | Rare |
| `minecraft-versions.json` | Versions supportées | Toutes les 2-4 semaines |
| `servers.json` | Serveurs featured | Occasionnelle |

---

## 🔧 Maintenance Régulière

### Toutes les 2 Semaines
```bash
./scripts/check-minecraft-updates.sh
```

### À Chaque Nouvelle Version Minecraft
```bash
./scripts/add-minecraft-version.sh
```

### Avant Chaque Commit
```bash
./scripts/health-check.sh
mvn clean package
```

---

## 🆘 En Cas de Problème

1. **Vérifiez la santé du projet**
   ```bash
   ./scripts/health-check.sh
   ```

2. **Consultez les guides**
   - Problème courant ? → `QUICKSTART.md`
   - Problème technique ? → `MAINTENANCE.md`
   - Vue d'ensemble ? → `README-IMPROVED.md`

3. **Compilez en mode propre**
   ```bash
   cd serverlist-server
   mvn clean
   mvn install -U
   mvn package
   ```

---

## 📚 Ordre de Lecture Recommandé

### Pour Démarrer (15 minutes)
1. `INDEX.md` ← Vous êtes ici
2. `QUICKSTART.md` ← Les bases
3. Lancez le projet

### Pour Approfondir (30 minutes)
1. `README-IMPROVED.md` ← Documentation complète
2. `CHANGES.md` ← Comprendre les changements

### Pour Maintenir (référence)
1. `MAINTENANCE.md` ← Gardez-le sous la main
2. Scripts dans `scripts/` ← Utilisez-les régulièrement

---

## 🎓 Concepts Importants

### ImageAssets
Classe Java qui gère toutes les URLs d'images depuis `images.json`.  
**Exemple :** `getImageAssets().getNationsGloryIcon("hub")`

### minecraft-versions.json
Fichier qui documente toutes les versions Minecraft supportées.  
**Usage :** Référence pour les mises à jour

### Scripts d'Aide
Outils bash pour automatiser les tâches répétitives.  
**Localisation :** `scripts/`

---

## 💡 Astuces

1. **Créez un alias** pour les tâches fréquentes :
   ```bash
   alias bc-check="./scripts/health-check.sh"
   alias bc-update="./scripts/check-minecraft-updates.sh"
   ```

2. **Automatisez la vérification** avec cron :
   ```bash
   # Tous les lundis à 10h
   0 10 * * 1 cd /path/to/project && ./scripts/check-minecraft-updates.sh
   ```

3. **Gardez la documentation à jour** quand vous modifiez le projet

---

## 🚀 Prochaines Étapes

1. ✅ Lisez `QUICKSTART.md`
2. ✅ Exécutez `./scripts/health-check.sh`
3. ✅ Compilez et testez le projet
4. ✅ Explorez les fichiers de configuration
5. ✅ Configurez votre environnement de développement

---

## 📞 Support

- **Questions générales :** Consultez `README-IMPROVED.md`
- **Problèmes techniques :** Consultez `MAINTENANCE.md`
- **Tâches rapides :** Consultez `QUICKSTART.md`

---

## 📝 Notes Importantes

- ⚠️ Tous les scripts sont en bash (Linux/macOS compatible)
- ⚠️ Java 8+ et Maven 3.6+ requis
- ⚠️ Les fichiers JSON doivent être valides (utilisez `jq` pour vérifier)
- ✅ Le projet est rétro-compatible avec l'ancienne version

---

**🎉 Bon développement !**

*Si vous avez des questions ou suggestions, n'hésitez pas à contribuer au projet.*
