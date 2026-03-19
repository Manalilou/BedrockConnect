# Helixya Connect 🎮

DNS personnalisé pour Minecraft Bedrock permettant aux consoles de se connecter à des serveurs personnalisés.

> Fork amélioré de [BedrockConnect](https://github.com/Pugmatt/BedrockConnect) par Pugmatt.

---

## 🎯 Fonctionnalités

- ✅ Support multi-versions Minecraft Bedrock (1.20.x → 1.21.80+)
- ✅ Gestion centralisée des serveurs via `servers-config.json` (images incluses)
- ✅ Gestion centralisée des versions via `minecraft-versions.json`
- ✅ **Système de groupes** — un bouton peut ouvrir un sous-menu de serveurs
- ✅ **Bouton "Mes Serveurs"** — connexion directe, gestion et serveurs perso dans un sous-menu dédié
- ✅ Désactivation de serveur sans suppression (`"enabled": false`)
- ✅ Compatible Docker & Raspberry Pi

---

## 📁 Structure du Projet

```
Helixya-Connect/
├── serverlist-server/
│   ├── src/main/
│   │   ├── com/pyratron/pugmatt/bedrockconnect/
│   │   │   ├── config/
│   │   │   └── server/
│   │   │       ├── ServerInfo.java       ← Serveur direct ou groupe
│   │   │       ├── ServerManager.java    ← Chargement depuis servers-config.json
│   │   │       ├── PacketHandler.java    ← Gestion des clics (tous les menus)
│   │   │       └── gui/UIForms.java      ← Formulaires in-game
│   │   └── resources/
│   │       ├── language.json             ← Traductions de l'interface
│   │       ├── minecraft-versions.json   ← Versions MC supportées
│   │       └── servers-config.json       ← Serveurs, groupes, config user_menu
│   └── pom.xml
├── scripts/
│   ├── add-minecraft-version.sh
│   └── check-minecraft-updates.sh
├── docker/
│   └── docker-compose.yml
└── Guide D'Utilisation/
```

---

## 🖥️ Structure du Menu In-Game

```
[Mes Serveurs]          ← sous-menu dédié au joueur
  ├ Se Connecter à un Serveur
  ├ Gérer la Liste des Serveurs
  ├ Retour
  └ ... serveurs sauvegardés du joueur
[NationsGlory]          ← groupe featured (ouvre un sous-menu)
  ├ Hub
  ├ Alpha
  └ ...
[Paladium Bedrock]      ← serveur featured direct
[Other Server]          ← sous-menu des serveurs publics populaires
[Quitter la Liste]
```

---

## 🚀 Démarrage Rapide

### Prérequis
- Java 8 ou supérieur
- Maven 3.6+

### Compilation & Lancement

```bash
cd serverlist-server
mvn clean package
java -jar target/BedrockConnect-1.0-SNAPSHOT.jar
```

---

## 🖥️ Gestion des Serveurs

Tout se passe dans `serverlist-server/src/main/resources/servers-config.json`. Un rebuild est nécessaire après chaque modification.

### Configurer le bouton "Mes Serveurs"

Le nom et l'icône du bouton sont définis dans le bloc `user_menu` :

```json
"user_menu": {
  "name": "Mes Serveurs",
  "image": "https://i.imgur.com/nhumQVP.png"
}
```

### Serveur direct

```json
{
  "id":      "the_hive",
  "name":    "The Hive",
  "address": "geo.hivebedrock.network",
  "port":    19132,
  "image":   "https://i.imgur.com/RfxfPGz.png",
  "enabled": true
}
```

### Groupe (bouton → sous-menu)

```json
{
  "id":      "nationsglory",
  "name":    "NationsGlory",
  "image":   "https://s3.nationstools.fr/public/bedrockconnect/Hub.png",
  "enabled": true,
  "servers": [
    { "id": "ng_hub",   "name": "Hub",   "address": "bedrock.nationsglory.fr", "port": 19132, "image": "https://..." },
    { "id": "ng_alpha", "name": "Alpha", "address": "bedrock.nationsglory.fr", "port": 19100, "image": "https://..." }
  ]
}
```

### Désactiver une entrée sans la supprimer

```json
{ "enabled": false, ... }
```

### Catégories

| Catégorie | Rôle |
|-----------|------|
| `featured` | Tes serveurs principaux — affichés dans le menu principal |
| `other` | Serveurs publics populaires — accessibles via "Other Server" |

> Les groupes ne sont supportés que dans `featured`.

---

## 📝 Fichiers de Configuration

| Fichier | Description | Rebuild requis |
|---------|-------------|---------------|
| `servers-config.json` | Serveurs, groupes, images et `user_menu` | ✅ Oui |
| `language.json` | Traductions de l'interface | ✅ Oui |
| `minecraft-versions.json` | Versions MC supportées | ✅ Oui |

---

## 🔄 Gestion des Versions Minecraft

```bash
# Ajouter une version (assistant interactif)
./scripts/add-minecraft-version.sh

# Vérifier les mises à jour disponibles
./scripts/check-minecraft-updates.sh
```

---

## 📊 Versions Supportées

Version actuelle : **Minecraft Bedrock 1.21.80** (Protocol 924)

---

## 🔧 Dépannage

**Les serveurs ne s'affichent pas**
1. Vérifier que `servers-config.json` est dans `src/main/resources/` et que le projet a été recompilé
2. Vérifier `"enabled": true` sur les entrées concernées
3. Consulter les logs : `[ServerManager] Chargé : X featured, Y other`

**Le bouton "Mes Serveurs" ne s'affiche pas**
- Vérifier que `user_servers` n'est pas désactivé dans les arguments de lancement
- Vérifier que le bloc `"user_menu"` est présent dans `servers-config.json`

**Un groupe n'ouvre pas de sous-menu**
1. Vérifier que l'entrée a un champ `"servers"` (tableau) et pas de champ `"address"`
2. Valider le JSON (validateur en ligne)

**Nouvelle version Minecraft non reconnue**
```bash
./scripts/add-minecraft-version.sh
```

**Erreurs de compilation**
```bash
cd serverlist-server
mvn clean && mvn install -U
```

---

## 📚 Documentation

- **[Guide D'Utilisation/START_HERE.md](Guide%20D'Utilisation/START_HERE.md)** — Par où commencer
- **[Guide D'Utilisation/MAINTENANCE.md](Guide%20D'Utilisation/MAINTENANCE.md)** — Guide complet de maintenance
- **[BedrockConnect Original](https://github.com/Pugmatt/BedrockConnect)** — Projet source

---

## 📄 Licence

[Voir LICENSE](LICENSE)

## 🙏 Remerciements

- [BedrockConnect](https://github.com/Pugmatt/BedrockConnect) par Pugmatt
- [CloudburstMC Protocol](https://github.com/CloudburstMC/Protocol)
- Communauté Minecraft Bedrock