#!/bin/bash

# ========================================
# BedrockConnect - Version Checker Script
# ========================================
# Ce script vérifie automatiquement si de nouvelles versions
# de Minecraft Bedrock sont disponibles
# ========================================

echo "🔍 Vérification des mises à jour Minecraft Bedrock..."
echo ""

# Couleurs pour le terminal
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# URL du repository Maven
MAVEN_URL="https://repo.opencollab.dev/maven-snapshots/org/cloudburstmc/protocol/bedrock-connection/"
GITHUB_API="https://api.github.com/repos/CloudburstMC/Protocol/releases/latest"

echo -e "${BLUE}📦 Repository Maven:${NC}"
echo "$MAVEN_URL"
echo ""

echo -e "${BLUE}🔗 GitHub Protocol:${NC}"
echo "https://github.com/CloudburstMC/Protocol"
echo ""

# Lire la version actuelle depuis minecraft-versions.json
CURRENT_VERSION_FILE="serverlist-server/src/main/resources/minecraft-versions.json"

if [ -f "$CURRENT_VERSION_FILE" ]; then
    echo -e "${GREEN}✅ Fichier de configuration trouvé${NC}"
    
    # Extraire la version actuelle (nécessite jq)
    if command -v jq &> /dev/null; then
        CURRENT_CODEC=$(jq -r '.protocol_info.latest_version.codec' "$CURRENT_VERSION_FILE")
        CURRENT_PROTOCOL=$(jq -r '.protocol_info.latest_version.protocol' "$CURRENT_VERSION_FILE")
        CURRENT_MC_VERSION=$(jq -r '.protocol_info.latest_version.minecraft_version' "$CURRENT_VERSION_FILE")
        CURRENT_DEPENDENCY=$(jq -r '.protocol_info.dependency.current_version' "$CURRENT_VERSION_FILE")
        
        echo ""
        echo -e "${YELLOW}📊 Version actuelle:${NC}"
        echo "   Codec: $CURRENT_CODEC"
        echo "   Protocol: $CURRENT_PROTOCOL"
        echo "   Minecraft: $CURRENT_MC_VERSION"
        echo "   Dependency: $CURRENT_DEPENDENCY"
        echo ""
    else
        echo -e "${YELLOW}⚠️  jq n'est pas installé. Installez-le pour voir les détails de version${NC}"
        echo "   Installation: sudo apt install jq (Ubuntu/Debian) ou brew install jq (macOS)"
        echo ""
    fi
else
    echo -e "${RED}❌ Fichier de configuration non trouvé: $CURRENT_VERSION_FILE${NC}"
    echo ""
fi

echo -e "${YELLOW}🔔 Actions recommandées:${NC}"
echo ""
echo "1. Visitez le repository Maven pour voir les dernières versions disponibles:"
echo "   $MAVEN_URL"
echo ""
echo "2. Vérifiez les releases GitHub pour les nouveaux codecs:"
echo "   https://github.com/CloudburstMC/Protocol/releases"
echo ""
echo "3. Consultez le changelog Minecraft Bedrock:"
echo "   https://minecraft.wiki/w/Bedrock_Edition_version_history"
echo ""
echo "4. Si une nouvelle version est disponible, suivez le guide:"
echo "   cat MAINTENANCE.md"
echo ""

# Optionnel: Essayer de récupérer la dernière release depuis GitHub
if command -v curl &> /dev/null && command -v jq &> /dev/null; then
    echo -e "${BLUE}🌐 Vérification GitHub...${NC}"
    
    LATEST_RELEASE=$(curl -s "$GITHUB_API" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ ! -z "$LATEST_RELEASE" ]; then
        TAG_NAME=$(echo "$LATEST_RELEASE" | jq -r '.tag_name')
        RELEASE_DATE=$(echo "$LATEST_RELEASE" | jq -r '.published_at' | cut -d'T' -f1)
        
        if [ "$TAG_NAME" != "null" ]; then
            echo ""
            echo -e "${GREEN}📢 Dernière release GitHub: $TAG_NAME${NC}"
            echo "   Date: $RELEASE_DATE"
            echo ""
        fi
    fi
fi

echo -e "${GREEN}✅ Vérification terminée${NC}"
echo ""
echo "💡 Conseil: Exécutez ce script régulièrement (toutes les 2-4 semaines)"
echo "   ou configurez un cron job pour automatiser la vérification."
