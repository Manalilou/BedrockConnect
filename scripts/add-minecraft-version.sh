#!/bin/bash

# ========================================
# BedrockConnect - Outil d'Ajout de Version
# ========================================
# Ce script facilite l'ajout d'une nouvelle version Minecraft Bedrock
# ========================================

echo "🚀 BedrockConnect - Ajout de Nouvelle Version Minecraft"
echo "=========================================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher des messages
info() {
    echo -e "${BLUE}ℹ${NC}  $1"
}

success() {
    echo -e "${GREEN}✓${NC}  $1"
}

warn() {
    echo -e "${YELLOW}⚠${NC}  $1"
}

error() {
    echo -e "${RED}✗${NC}  $1"
}

# Vérifier que nous sommes dans le bon répertoire
if [ ! -f "pom.xml" ] && [ ! -f "serverlist-server/pom.xml" ]; then
    error "Erreur: pom.xml non trouvé. Exécutez ce script depuis la racine du projet."
    exit 1
fi

# Se déplacer dans le bon répertoire si nécessaire
if [ -f "serverlist-server/pom.xml" ]; then
    cd serverlist-server
fi

echo ""
info "Ce script va vous guider pour ajouter une nouvelle version Minecraft Bedrock"
echo ""

# Demander les informations
read -p "📝 Numéro de codec (ex: 950): v" CODEC_NUM
CODEC_VERSION="v${CODEC_NUM}"

read -p "📝 Numéro de protocol (ex: 950): " PROTOCOL_NUM

read -p "📝 Version Minecraft (ex: 1.22.0): " MC_VERSION

read -p "📝 Version bedrock-connection Maven (ex: 3.0.0.Beta12-20260215.123456-1): " MAVEN_VERSION

echo ""
info "Récapitulatif:"
echo "  - Codec: Bedrock_${CODEC_VERSION}"
echo "  - Protocol: $PROTOCOL_NUM"
echo "  - Minecraft: $MC_VERSION"
echo "  - Maven: $MAVEN_VERSION"
echo ""

read -p "Continuer? (o/N): " CONFIRM
if [[ ! $CONFIRM =~ ^[Oo]$ ]]; then
    warn "Annulé par l'utilisateur"
    exit 0
fi

echo ""
info "🔧 Génération des modifications..."
echo ""

# 1. Générer le code pour BedrockProtocol.java
echo "=========================================="
success "1. Code à ajouter dans BedrockProtocol.java"
echo "=========================================="
echo ""
echo "${YELLOW}Import à ajouter (ligne ~23):${NC}"
echo "import org.cloudburstmc.protocol.bedrock.codec.${CODEC_VERSION}.Bedrock_${CODEC_VERSION};"
echo ""
echo "${YELLOW}Modifier DEFAULT_BEDROCK_CODEC (ligne ~35):${NC}"
echo "public static final BedrockCodec DEFAULT_BEDROCK_CODEC = Bedrock_${CODEC_VERSION}.CODEC;"
echo ""
echo "${YELLOW}Ajouter dans SUPPORTED_BEDROCK_CODECS (ligne ~73, avant la ligne finale):${NC}"
echo "        SUPPORTED_BEDROCK_CODECS.add(Bedrock_${CODEC_VERSION}.CODEC);"
echo ""

# 2. Générer le code pour pom.xml
echo "=========================================="
success "2. Modification dans pom.xml"
echo "=========================================="
echo ""
echo "${YELLOW}Mettre à jour la version (ligne ~81):${NC}"
echo "        <dependency>"
echo "            <groupId>org.cloudburstmc.protocol</groupId>"
echo "            <artifactId>bedrock-connection</artifactId>"
echo "            <version>${MAVEN_VERSION}</version>"
echo "            <scope>compile</scope>"
echo "        </dependency>"
echo ""

# 3. Générer le JSON pour minecraft-versions.json
echo "=========================================="
success "3. Entrée à ajouter dans minecraft-versions.json"
echo "=========================================="
echo ""
echo "${YELLOW}Mettre à jour protocol_info.latest_version:${NC}"
cat << EOF
  "protocol_info": {
    "dependency": {
      "current_version": "${MAVEN_VERSION}"
    },
    "latest_version": {
      "codec": "${CODEC_VERSION}",
      "protocol": ${PROTOCOL_NUM},
      "minecraft_version": "${MC_VERSION}",
      "last_updated": "$(date +%Y-%m-%d)",
      "notes": "Current latest supported version"
    }
  }
EOF
echo ""
echo "${YELLOW}Ajouter dans supported_versions (retirer 'is_latest' des anciennes versions):${NC}"
cat << EOF
    {
      "codec": "${CODEC_VERSION}",
      "protocol": ${PROTOCOL_NUM},
      "minecraft_version": "${MC_VERSION}",
      "deprecated": false,
      "is_latest": true
    }
EOF
echo ""

# 4. Instructions de compilation
echo "=========================================="
success "4. Étapes suivantes"
echo "=========================================="
echo ""
echo "1️⃣  Appliquez les modifications ci-dessus dans les fichiers"
echo ""
echo "2️⃣  Compilez le projet:"
echo "   ${BLUE}mvn clean package${NC}"
echo ""
echo "3️⃣  Testez avec Minecraft ${MC_VERSION}"
echo ""
echo "4️⃣  Commitez les changements:"
echo "   ${BLUE}git add .${NC}"
echo "   ${BLUE}git commit -m \"Add support for Minecraft ${MC_VERSION} (protocol ${PROTOCOL_NUM})\"${NC}"
echo ""

# Option de sauvegarde
echo ""
read -p "💾 Sauvegarder ces instructions dans un fichier? (o/N): " SAVE
if [[ $SAVE =~ ^[Oo]$ ]]; then
    OUTPUT_FILE="add-version-${MC_VERSION}.txt"
    {
        echo "BedrockConnect - Ajout Minecraft ${MC_VERSION}"
        echo "=============================================="
        echo ""
        echo "Codec: Bedrock_${CODEC_VERSION}"
        echo "Protocol: ${PROTOCOL_NUM}"
        echo "Maven: ${MAVEN_VERSION}"
        echo ""
        echo "Voir les modifications détaillées ci-dessus dans la sortie du script"
    } > "$OUTPUT_FILE"
    
    success "Instructions sauvegardées dans: $OUTPUT_FILE"
fi

echo ""
success "Terminé! Bonne chance avec la mise à jour 🎮"
