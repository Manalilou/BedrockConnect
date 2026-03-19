#!/bin/bash

# ========================================
# BedrockConnect - Health Check Script
# ========================================
# Vérifie que tous les fichiers de configuration sont corrects
# et que le projet est prêt pour la compilation
# ========================================

echo "🏥 BedrockConnect - Vérification de Santé du Projet"
echo "===================================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

ERRORS=0
WARNINGS=0

# Fonction pour afficher des messages
check_ok() {
    echo -e "${GREEN}✓${NC}  $1"
}

check_warn() {
    echo -e "${YELLOW}⚠${NC}  $1"
    ((WARNINGS++))
}

check_error() {
    echo -e "${RED}✗${NC}  $1"
    ((ERRORS++))
}

info() {
    echo -e "${BLUE}ℹ${NC}  $1"
}

# Se déplacer dans le bon répertoire
if [ -d "serverlist-server" ]; then
    BASE_DIR="serverlist-server"
else
    BASE_DIR="."
fi

echo "📁 Vérification de la structure du projet..."
echo ""

# 1. Vérifier les fichiers essentiels
info "1. Fichiers de configuration"

FILES=(
    "$BASE_DIR/pom.xml"
    "$BASE_DIR/src/main/resources/images.json"
    "$BASE_DIR/src/main/resources/language.json"
    "$BASE_DIR/src/main/resources/minecraft-versions.json"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        check_ok "$(basename $file) trouvé"
    else
        check_error "$(basename $file) manquant à $file"
    fi
done

echo ""

# 2. Vérifier les fichiers Java importants
info "2. Fichiers Java principaux"

JAVA_FILES=(
    "$BASE_DIR/src/main/com/pyratron/pugmatt/bedrockconnect/BedrockConnect.java"
    "$BASE_DIR/src/main/com/pyratron/pugmatt/bedrockconnect/Config.java"
    "$BASE_DIR/src/main/com/pyratron/pugmatt/bedrockconnect/config/ImageAssets.java"
    "$BASE_DIR/src/main/com/pyratron/pugmatt/bedrockconnect/server/BedrockProtocol.java"
)

for file in "${JAVA_FILES[@]}"; do
    if [ -f "$file" ]; then
        check_ok "$(basename $file) trouvé"
    else
        check_error "$(basename $file) manquant"
    fi
done

echo ""

# 3. Vérifier la syntaxe JSON (si jq est installé)
info "3. Validation JSON"

if command -v jq &> /dev/null; then
    for file in "$BASE_DIR/src/main/resources"/*.json; do
        if [ -f "$file" ]; then
            if jq empty "$file" 2>/dev/null; then
                check_ok "$(basename $file) - JSON valide"
            else
                check_error "$(basename $file) - JSON invalide !"
            fi
        fi
    done
else
    check_warn "jq n'est pas installé - impossible de valider JSON"
    info "Installation: sudo apt install jq (Ubuntu) ou brew install jq (macOS)"
fi

echo ""

# 4. Vérifier les URLs d'images
info "4. Validation des URLs d'images"

IMAGES_FILE="$BASE_DIR/src/main/resources/images.json"
if [ -f "$IMAGES_FILE" ] && command -v jq &> /dev/null; then
    # Extraire toutes les URLs
    URLS=$(jq -r '.. | select(type == "string" and startswith("http"))' "$IMAGES_FILE")
    URL_COUNT=$(echo "$URLS" | wc -l)
    
    if [ $URL_COUNT -gt 0 ]; then
        check_ok "$URL_COUNT URLs d'images trouvées"
        
        # Vérifier quelques URLs (optionnel, peut être lent)
        # Décommentez si vous voulez vérifier la disponibilité
        # while read -r url; do
        #     if curl --output /dev/null --silent --head --fail "$url"; then
        #         check_ok "URL accessible: $url"
        #     else
        #         check_warn "URL inaccessible: $url"
        #     fi
        # done <<< "$URLS"
    else
        check_warn "Aucune URL trouvée dans images.json"
    fi
fi

echo ""

# 5. Vérifier les versions Minecraft
info "5. Versions Minecraft supportées"

VERSIONS_FILE="$BASE_DIR/src/main/resources/minecraft-versions.json"
if [ -f "$VERSIONS_FILE" ] && command -v jq &> /dev/null; then
    LATEST_CODEC=$(jq -r '.protocol_info.latest_version.codec' "$VERSIONS_FILE")
    LATEST_MC=$(jq -r '.protocol_info.latest_version.minecraft_version' "$VERSIONS_FILE")
    SUPPORTED_COUNT=$(jq '.supported_versions | length' "$VERSIONS_FILE")
    
    check_ok "Version la plus récente: $LATEST_MC (codec $LATEST_CODEC)"
    check_ok "$SUPPORTED_COUNT versions supportées au total"
fi

echo ""

# 6. Vérifier Maven
info "6. Environnement de développement"

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    check_ok "Maven installé: $MVN_VERSION"
else
    check_error "Maven n'est pas installé !"
fi

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    check_ok "Java installé: $JAVA_VERSION"
else
    check_error "Java n'est pas installé !"
fi

echo ""

# 7. Vérifier que ImageAssets est bien intégré
info "7. Intégration ImageAssets"

CONFIG_FILE="$BASE_DIR/src/main/com/pyratron/pugmatt/bedrockconnect/Config.java"
if [ -f "$CONFIG_FILE" ]; then
    if grep -q "ImageAssets" "$CONFIG_FILE"; then
        check_ok "ImageAssets importé dans Config.java"
    else
        check_error "ImageAssets non trouvé dans Config.java"
    fi
    
    if grep -q "getImageAssets()" "$CONFIG_FILE"; then
        check_ok "Getter getImageAssets() présent dans Config.java"
    else
        check_error "Getter getImageAssets() manquant dans Config.java"
    fi
fi

echo ""

# 8. Vérifier les scripts d'aide
info "8. Scripts d'aide"

SCRIPTS=(
    "scripts/check-minecraft-updates.sh"
    "scripts/add-minecraft-version.sh"
)

for script in "${SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            check_ok "$(basename $script) trouvé et exécutable"
        else
            check_warn "$(basename $script) trouvé mais pas exécutable"
            info "Exécutez: chmod +x $script"
        fi
    else
        check_warn "$(basename $script) manquant"
    fi
done

echo ""
echo "===================================================="

# Résumé
if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ Tout est parfait ! Le projet est prêt.${NC}"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  $WARNINGS avertissement(s) trouvé(s)${NC}"
    echo "Le projet devrait fonctionner mais certaines optimisations sont possibles."
    exit 0
else
    echo -e "${RED}❌ $ERRORS erreur(s) et $WARNINGS avertissement(s) trouvé(s)${NC}"
    echo "Veuillez corriger les erreurs avant de compiler."
    exit 1
fi
