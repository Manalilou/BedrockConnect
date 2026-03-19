#!/bin/bash

# ========================================
# Helixya Connect - Outil d'Ajout de Serveur
# ========================================
# Ce script facilite l'ajout rapide d'un serveur
# dans servers.json (featured ou other)
# ========================================

# Couleurs
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

info()    { echo -e "${BLUE}ℹ${NC}  $1"; }
success() { echo -e "${GREEN}✓${NC}  $1"; }
warn()    { echo -e "${YELLOW}⚠${NC}  $1"; }
error()   { echo -e "${RED}✗${NC}  $1"; }
header()  { echo -e "${BOLD}${CYAN}$1${NC}"; }

echo ""
header "🎮 Helixya Connect - Ajout de Serveur"
header "========================================"
echo ""

# ─── Vérifier le fichier servers.json ───────────────────────────────────────

# Chercher servers.json depuis la racine du projet
SERVERS_JSON=""

if [ -f "servers.json" ]; then
    SERVERS_JSON="servers.json"
elif [ -f "../servers.json" ]; then
    SERVERS_JSON="../servers.json"
else
    error "servers.json introuvable. Exécutez ce script depuis la racine ou le dossier scripts/ du projet."
    exit 1
fi

success "Fichier trouvé: $SERVERS_JSON"
echo ""

# ─── Afficher les serveurs existants ────────────────────────────────────────

info "Serveurs actuels dans ${BOLD}featured${NC}:"
python3 -c "
import json
with open('$SERVERS_JSON') as f:
    data = json.load(f)
for s in data.get('featured', []):
    print(f\"  - {s['name']} ({s['address']}:{s['port']})\")
" 2>/dev/null || grep -o '"name":"[^"]*"' "$SERVERS_JSON" | head -20

echo ""
info "Serveurs actuels dans ${BOLD}other${NC}:"
python3 -c "
import json
with open('$SERVERS_JSON') as f:
    data = json.load(f)
for s in data.get('other', []):
    print(f\"  - {s['name']} ({s['address']}:{s['port']})\")
" 2>/dev/null

echo ""
echo "────────────────────────────────────────"
echo ""

# ─── Collecte des informations ──────────────────────────────────────────────

echo -e "${BOLD}Catégorie du serveur:${NC}"
echo "  1)