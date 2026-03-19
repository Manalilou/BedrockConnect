# 📋 Guide de Maintenance BedrockConnect

Ce guide vous aide à maintenir facilement votre projet BedrockConnect lors des mises à jour Minecraft.

## 🔄 Processus de Mise à Jour Minecraft

### Étape 1 : Vérifier les Nouvelles Versions

**Fréquence recommandée :** Toutes les 2-4 semaines

**Sources à surveiller :**
- 🔗 [CloudburstMC Protocol GitHub](https://github.com/CloudburstMC/Protocol) - Releases
- 🔗 [Maven Repository](https://repo.opencollab.dev/maven-snapshots/org/cloudburstmc/protocol/bedrock-connection/) - Dernières versions
- 🔗 [Minecraft Bedrock Changelog](https://minecraft.wiki/w/Bedrock_Edition_version_history)

### Étape 2 : Mettre à Jour les Fichiers de Configuration

#### A. `minecraft-versions.json`
```json
{
  "protocol_info": {
    "dependency": {
      "current_version": "3.0.0.Beta12-XXXXXXXX.XXXXXX-X" // ← Mettre à jour ici
    },
    "latest_version": {
      "codec": "vXXX",           // ← Nouveau numéro de codec
      "protocol": XXX,            // ← Nouveau numéro de protocol
      "minecraft_version": "1.XX.XX",
      "last_updated": "2025-XX-XX"
    }
  },
  "supported_versions": [
    // Ajouter la nouvelle version ici
    {
      "codec": "vXXX",
      "protocol": XXX,
      "minecraft_version": "1.XX.XX",
      "deprecated": false,
      "is_latest": true  // ← Marquer comme latest
    }
  ]
}
```

### Étape 3 : Mettre à Jour le Code Java

#### A. `pom.xml`
```xml
<dependency>
    <groupId>org.cloudburstmc.protocol</groupId>
    <artifactId>bedrock-connection</artifactId>
    <version>3.0.0.Beta12-XXXXXXXX.XXXXXX-X</version> <!-- Mettre à jour -->
    <scope>compile</scope>
</dependency>
```

#### B. `BedrockProtocol.java`

**1. Ajouter l'import :**
```java
import org.cloudburstmc.protocol.bedrock.codec.vXXX.Bedrock_vXXX; // ← Nouveau
```

**2. Mettre à jour DEFAULT_BEDROCK_CODEC :**
```java
public static final BedrockCodec DEFAULT_BEDROCK_CODEC = Bedrock_vXXX.CODEC; // ← Nouveau
```

**3. Ajouter au SUPPORTED_BEDROCK_CODECS :**
```java
static {
    // ... versions existantes ...
    SUPPORTED_BEDROCK_CODECS.add(Bedrock_vXXX.CODEC); // ← Ajouter à la fin
}
```

### Étape 4 : Rebuild le Projet

```bash
# Nettoyer et compiler
mvn clean package

# Vérifier que le JAR est généré
ls -lh target/BedrockConnect-1.0-SNAPSHOT.jar
```

### Étape 5 : Tester

1. **Démarrer le serveur :**
   ```bash
   java -jar target/BedrockConnect-1.0-SNAPSHOT.jar
   ```

2. **Vérifier les logs :**
   - Rechercher : `Loaded XX supported versions`
   - Vérifier que la nouvelle version apparaît

3. **Tester la connexion :**
   - Se connecter avec un client Minecraft de la nouvelle version
   - Vérifier que la liste de serveurs s'affiche correctement

---

## 🖼️ Gestion des Images

### Modifier une Image

**Fichier :** `src/main/resources/images.json`

```json
{
  "nationsglory": {
    "hub": "https://nouvelle-url.com/Hub.png"  // ← Modifier l'URL ici
  }
}
```

**Avantages :**
- ✅ Un seul fichier à modifier
- ✅ Pas besoin de recompiler le code Java
- ✅ Changement immédiat après redémarrage

### Ajouter une Nouvelle Image

```json
{
  "nationsglory": {
    "nouveau_serveur": "https://url.com/nouveau.png"
  }
}
```

Puis dans le code :
```java
String imageUrl = BedrockConnect.getConfig().getImageAssets().getNationsGloryIcon("nouveau_serveur");
```

---

## 📝 Checklist Rapide de Mise à Jour

- [ ] Vérifier nouvelle version sur GitHub CloudburstMC
- [ ] Noter le numéro de version, codec, et protocol
- [ ] Mettre à jour `minecraft-versions.json`
- [ ] Mettre à jour `pom.xml` (version dependency)
- [ ] Ajouter import dans `BedrockProtocol.java`
- [ ] Mettre à jour `DEFAULT_BEDROCK_CODEC`
- [ ] Ajouter codec à `SUPPORTED_BEDROCK_CODECS`
- [ ] Exécuter `mvn clean package`
- [ ] Tester avec la nouvelle version de Minecraft
- [ ] Commit et push les changements

---

## 🔧 Dépannage

### Erreur "Cannot resolve codec vXXX"
**Solution :** Vérifiez que la version de `bedrock-connection` dans `pom.xml` contient le nouveau codec. 
Parfois il faut attendre que CloudburstMC publie la version.

### Erreur de compilation Maven
**Solution :** 
```bash
mvn clean
mvn install -U  # Force update dependencies
```

### Les images ne se chargent pas
**Solution :** 
1. Vérifiez que `images.json` est bien dans `src/main/resources/`
2. Rebuild le projet : `mvn clean package`
3. Vérifiez les URLs dans les logs au démarrage

---

## 📊 Historique de Maintenance

| Date | Version MC | Protocol | Codec | Notes |
|------|-----------|----------|-------|-------|
| 2025-02-09 | 1.21.80 | 924 | v924 | Version actuelle |
| À venir | ... | ... | ... | ... |

**💡 Conseil :** Gardez ce tableau à jour pour suivre l'historique des mises à jour.

---

## 🚀 Automatisation Future (Optionnel)

Pour automatiser davantage, vous pourriez créer :

1. **Script de vérification de version :**
   ```bash
   #!/bin/bash
   # check-updates.sh
   # Vérifie automatiquement les nouvelles versions
   ```

2. **GitHub Actions :**
   - Auto-vérification hebdomadaire
   - Notification si nouvelle version disponible

3. **Script de mise à jour :**
   ```bash
   #!/bin/bash
   # update-protocol.sh <version>
   # Met à jour automatiquement les fichiers
   ```

---

**📧 Questions ?** Consultez les issues GitHub ou la documentation CloudburstMC.
