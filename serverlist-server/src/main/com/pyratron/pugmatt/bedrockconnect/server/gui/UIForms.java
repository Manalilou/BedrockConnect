package main.com.pyratron.pugmatt.bedrockconnect.server.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.com.pyratron.pugmatt.bedrockconnect.*;
import main.com.pyratron.pugmatt.bedrockconnect.config.Custom.CustomEntry;
import main.com.pyratron.pugmatt.bedrockconnect.config.Custom.CustomServer;
import main.com.pyratron.pugmatt.bedrockconnect.config.Custom.CustomServerGroup;
import main.com.pyratron.pugmatt.bedrockconnect.server.ServerInfo;

import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket;
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket;

import java.util.ArrayList;
import java.util.List;

public class UIForms {
    public static final int ERROR          = 2;
    public static final int MAIN           = 0;
    public static final int OTHER          = 10;
    public static final int DIRECT_CONNECT = 1;
    public static final int REMOVE_SERVER  = 3;
    public static final int MANAGE_SERVER  = 4;
    public static final int EDIT_SERVER    = 5;
    public static final int EDIT_CHOOSE_SERVER = 6;
    public static final int ADD_SERVER     = 7;
    public static final int SERVER_GROUP   = 8;  // groupe Custom (custom_servers)
    public static final int MOTD           = 9;
    public static final int FEATURED_GROUP = 11; // groupe Featured (servers-config.json)
    public static final int USER_MENU      = 12; // sous-menu "Mes Serveurs"

    public static JsonArray mainMenuButtons  = new JsonArray();
    public static JsonArray userMenuButtons  = new JsonArray();
    public static JsonArray manageListButtons = new JsonArray();
    public static JsonArray otherServer      = new JsonArray();

    public static final int DEFAULT_PORT = 19132;

    static {
        // Menu principal : uniquement le bouton "Mes Serveurs" (featured + other construits dynamiquement)
        mainMenuButtons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("main", "exitBtn")));

        // Sous-menu "Mes Serveurs" : Connexion directe + Gérer
        userMenuButtons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("userMenu", "connectBtn")));
        userMenuButtons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("userMenu", "manageBtn")));
        userMenuButtons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("userMenu", "backBtn")));

        String removeBtnText = !BedrockConnect.getConfig().getLanguage().getWording("manage", "removeBtn").equals("N/A")
            ? BedrockConnect.getConfig().getLanguage().getWording("manage", "removeBtn")
            : BedrockConnect.getConfig().getLanguage().getWording("manage", "removeBtn");
        manageListButtons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("manage", "addBtn")));
        manageListButtons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("manage", "editBtn")));
        manageListButtons.add(UIComponents.createButton(removeBtnText));

        otherServer.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("main", "otherBtn")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu principal
    // ─────────────────────────────────────────────────────────────────────────

    public static ModalFormRequestPacket createMain(List<String> servers, BedrockServerSession session) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.MAIN);

        JsonObject out = UIComponents.createForm("form", BedrockConnect.getConfig().getLanguage().getWording("main", "heading"));
        out.addProperty("content", BedrockConnect.getConfig().getLanguage().getWording("main", "message"));

        JsonArray buttons = new JsonArray();

        // Bouton "Mes Serveurs" (ouvre le sous-menu USER_MENU)
        if (BedrockConnect.getConfig().isUserServersEnabled()) {
            String userMenuName  = BedrockConnect.getServerManager().getUserMenuName();
            String userMenuImage = BedrockConnect.getServerManager().getUserMenuImage();
            buttons.add(UIComponents.createButton(userMenuName, userMenuImage, "url"));
        }

        // Custom servers (custom_servers.json)
        CustomEntry[] customServers = BedrockConnect.getConfig().getCustomServers();
        for (CustomEntry cs : customServers) {
            buttons.add(UIComponents.createButton(cs.getName(), cs.getIconUrl(), "url"));
        }

        // Featured servers (servers-config.json) — serveurs directs ET groupes
        if (BedrockConnect.getConfig().NG_Server()) {
            List<ServerInfo> featured = BedrockConnect.getServerManager().getFeaturedServers();
            for (ServerInfo server : featured) {
                buttons.add(UIComponents.createButton(server.getName(), server.getImageUrl(), "url"));
            }
        }

        // Bouton "Other Server"
        if (BedrockConnect.getConfig().featuredServer()) {
            buttons.addAll(otherServer);
        }

        // Bouton "Quitter"
        buttons.addAll(mainMenuButtons);

        out.add("buttons", buttons);
        mf.setFormData(out.toString());

        fixIcons(session);
        return mf;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sous-menu "Mes Serveurs" (USER_MENU)
    // ─────────────────────────────────────────────────────────────────────────

    public static ModalFormRequestPacket createUserMenu(List<String> servers, BedrockServerSession session) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.USER_MENU);

        JsonObject out = UIComponents.createForm("form", BedrockConnect.getConfig().getLanguage().getWording("userMenu", "heading"));
        out.addProperty("content", "");

        JsonArray buttons = new JsonArray();

        // Connexion directe + Gérer + Retour (boutons fixes)
        buttons.addAll(userMenuButtons);

        // Serveurs personnels du joueur
        for (String server : servers) {
            buttons.add(UIComponents.createButton(
                UIComponents.getServerDisplayName(server),
                BedrockConnect.getConfig().getLanguage().getWording("main", "userServerIcon") != null
                    ? BedrockConnect.getConfig().getLanguage().getWording("main", "userServerIcon")
                    : "https://i.imgur.com/nhumQVP.png",
                "url"
            ));
        }

        out.add("buttons", buttons);
        mf.setFormData(out.toString());

        fixIcons(session);
        return mf;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sous-menu d'un groupe Featured (servers-config.json)
    // ─────────────────────────────────────────────────────────────────────────

    public static ModalFormRequestPacket createFeaturedGroup(ServerInfo group, BedrockServerSession session) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(FEATURED_GROUP);

        JsonObject out = UIComponents.createForm("form", group.getName());
        out.addProperty("content", "");

        JsonArray buttons = new JsonArray();
        buttons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("serverGroup", "backBtn")));

        for (ServerInfo server : group.getServers()) {
            buttons.add(UIComponents.createButton(server.getName(), server.getImageUrl(), "url"));
        }

        out.add("buttons", buttons);
        mf.setFormData(out.toString());

        fixIcons(session);
        return mf;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sous-menu "Other Server"
    // ─────────────────────────────────────────────────────────────────────────

    public static ModalFormRequestPacket createOtherList() {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(OTHER);
        JsonObject out = UIComponents.createForm("form", "Other Server");
        out.addProperty("content", "");

        JsonArray buttons = new JsonArray();
        List<ServerInfo> other = BedrockConnect.getServerManager().getOtherServers();
        for (ServerInfo server : other) {
            buttons.add(UIComponents.createButton(server.getName(), server.getImageUrl(), "url"));
        }
        buttons.add(UIComponents.createButton("Back"));

        out.add("buttons", buttons);
        mf.setFormData(out.toString());
        return mf;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Groupe Custom (custom_servers.json)
    // ─────────────────────────────────────────────────────────────────────────

    public static ModalFormRequestPacket createServerGroup(CustomServerGroup group, BedrockServerSession session) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.SERVER_GROUP);

        JsonObject out = UIComponents.createForm("form", group.getName());
        out.addProperty("content", "");

        JsonArray buttons = new JsonArray();
        buttons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("serverGroup", "backBtn")));

        for (CustomServer cs : group.getServers()) {
            buttons.add(UIComponents.createButton(cs.getName(), cs.getIconUrl(), "url"));
        }

        out.add("buttons", buttons);
        mf.setFormData(out.toString());

        fixIcons(session);
        return mf;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calcul de l'index et du type de bouton cliqué dans le menu principal
    // ─────────────────────────────────────────────────────────────────────────

    public static int getServerIndex(int btnId, CustomEntry[] customServers, List<String> playerServers) {
        // Offset = 1 (bouton "Mes Serveurs") si user servers actif, sinon 0
        int offset = BedrockConnect.getConfig().isUserServersEnabled() ? 1 : 0;
        return btnId - offset;
    }

    public static MainFormButton getMainFormButton(int btnId, CustomEntry[] customServers, List<String> playerServers) {
        // Bouton 0 = "Mes Serveurs" (si user servers actif)
        if (BedrockConnect.getConfig().isUserServersEnabled() && btnId == 0) {
            return MainFormButton.USER_MENU;
        }

        int serverIndex    = getServerIndex(btnId, customServers, playerServers);
        int customCount    = customServers.length;
        int featuredCount  = BedrockConnect.getServerManager().getFeaturedServers().size();

        if (serverIndex < customCount) {
            return MainFormButton.CUSTOM_SERVER;
        } else if (serverIndex < customCount + featuredCount) {
            return MainFormButton.NG_SERVER;
        } else {
            // Soit "Other Server", soit "Quitter" — différencié dans PacketHandler via OtherFormButton
            return MainFormButton.OTHER_BUTTON;
        }
    }

    public static ManageFormButton getManageFormButton(int btnId) {
        switch (btnId) {
            case 0: return ManageFormButton.ADD;
            case 1: return ManageFormButton.EDIT;
            case 2: return ManageFormButton.REMOVE;
        }
        return null;
    }

    /**
     * Interprète un clic dans le sous-menu USER_MENU.
     * Boutons fixes : 0=Connexion directe, 1=Gérer, 2=Retour
     * Boutons dynamiques : 3+ = serveurs perso du joueur
     */
    public static final int USER_MENU_CONNECT = 0;
    public static final int USER_MENU_MANAGE  = 1;
    public static final int USER_MENU_BACK    = 2;
    public static final int USER_MENU_SERVER_OFFSET = 3; // index du premier serveur perso

    public static OtherFormButton getOtherFormButton(int btnId) {
        switch (btnId) {
            case 0: return OtherFormButton.SERVER;
            case 1: return OtherFormButton.BACK;
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Formulaires de gestion (Manage / Add / Edit / Remove / Connect / Error / Motd)
    // ─────────────────────────────────────────────────────────────────────────

    public static ModalFormRequestPacket createManageList() {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.MANAGE_SERVER);
        JsonObject out = UIComponents.createForm("form", BedrockConnect.getConfig().getLanguage().getWording("manage", "heading"));
        out.addProperty("content", "");
        JsonArray buttons = new JsonArray();
        buttons.addAll(manageListButtons);
        out.add("buttons", buttons);
        mf.setFormData(out.toString());
        return mf;
    }

    public static ModalFormRequestPacket createAddServer() {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.ADD_SERVER);
        JsonObject out = UIComponents.createForm("custom_form", BedrockConnect.getConfig().getLanguage().getWording("add", "heading"));
        JsonArray inputs = new JsonArray();
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "addressTitle"), BedrockConnect.getConfig().getLanguage().getWording("connect", "addressPlaceholder")));
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "portTitle"), BedrockConnect.getConfig().getLanguage().getWording("connect", "portPlaceholder"), Integer.toString(DEFAULT_PORT)));
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "displayNameTitle"), "", ""));
        out.add("content", inputs);
        mf.setFormData(out.toString());
        return mf;
    }

    public static ModalFormRequestPacket createDirectConnect() {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.DIRECT_CONNECT);
        JsonObject out = UIComponents.createForm("custom_form", BedrockConnect.getConfig().getLanguage().getWording("connect", "heading"));
        JsonArray inputs = new JsonArray();
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "addressTitle"), BedrockConnect.getConfig().getLanguage().getWording("connect", "addressPlaceholder")));
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "portTitle"), BedrockConnect.getConfig().getLanguage().getWording("connect", "portPlaceholder"), Integer.toString(DEFAULT_PORT)));
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "displayNameTitle"), "", ""));
        inputs.add(UIComponents.createToggle(BedrockConnect.getConfig().getLanguage().getWording("connect", "addToggle")));
        out.add("content", inputs);
        mf.setFormData(out.toString());
        return mf;
    }

    public static ModalFormRequestPacket createEditChooseServer(List<String> servers) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.EDIT_CHOOSE_SERVER);
        JsonObject out = UIComponents.createForm("custom_form", BedrockConnect.getConfig().getLanguage().getWording("edit", "chooseHeading"));
        JsonArray inputs = new JsonArray();
        List<String> displayServers = new ArrayList<>();
        for (int i = 0; i < servers.size(); i++) {
            displayServers.add(UIComponents.getServerDisplayName(servers.get(i)));
        }
        inputs.add(UIComponents.createDropdown(displayServers, BedrockConnect.getConfig().getLanguage().getWording("edit", "serverDropdown"), "0"));
        out.add("content", inputs);
        mf.setFormData(out.toString());
        return mf;
    }

    public static ModalFormRequestPacket createEditServer(String address, String port, String name) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.EDIT_SERVER);
        JsonObject out = UIComponents.createForm("custom_form", BedrockConnect.getConfig().getLanguage().getWording("edit", "heading"));
        JsonArray inputs = new JsonArray();
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "addressTitle"), BedrockConnect.getConfig().getLanguage().getWording("connect", "addressPlaceholder"), address));
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "portTitle"), BedrockConnect.getConfig().getLanguage().getWording("connect", "portPlaceholder"), port));
        inputs.add(UIComponents.createInput(BedrockConnect.getConfig().getLanguage().getWording("connect", "displayNameTitle"), "", name));
        out.add("content", inputs);
        mf.setFormData(out.toString());
        return mf;
    }

    public static ModalFormRequestPacket createRemoveServer(List<String> servers) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.REMOVE_SERVER);
        JsonObject out = UIComponents.createForm("custom_form", BedrockConnect.getConfig().getLanguage().getWording("remove", "heading"));
        JsonArray inputs = new JsonArray();
        List<String> displayServers = new ArrayList<>();
        for (int i = 0; i < servers.size(); i++) {
            displayServers.add(UIComponents.getServerDisplayName(servers.get(i)));
        }
        inputs.add(UIComponents.createDropdown(displayServers, BedrockConnect.getConfig().getLanguage().getWording("remove", "serverDropdown"), "0"));
        out.add("content", inputs);
        mf.setFormData(out.toString());
        return mf;
    }

    public static ModalFormRequestPacket createError(String text) {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.ERROR);
        JsonObject form = new JsonObject();
        form.addProperty("type", "custom_form");
        form.addProperty("title", BedrockConnect.getConfig().getLanguage().getWording("error", "heading"));
        JsonArray content = new JsonArray();
        content.add(UIComponents.createLabel(text));
        form.add("content", content);
        mf.setFormData(form.toString());
        return mf;
    }

    public static ModalFormRequestPacket createMotd() {
        ModalFormRequestPacket mf = new ModalFormRequestPacket();
        mf.setFormId(UIForms.MOTD);
        JsonObject form = UIComponents.createForm("form", BedrockConnect.getConfig().getLanguage().getWording("motd", "heading"));
        form.addProperty("content", BedrockConnect.getConfig().getMotdMessage());
        JsonArray buttons = new JsonArray();
        buttons.add(UIComponents.createButton(BedrockConnect.getConfig().getLanguage().getWording("motd", "continueBtn")));
        form.add("buttons", buttons);
        mf.setFormData(form.toString());
        return mf;
    }

    public static void fixIcons(BedrockServerSession session) {
        NetworkStackLatencyPacket p = new NetworkStackLatencyPacket();
        p.setFromServer(true);
        p.setTimestamp(System.currentTimeMillis());
        session.sendPacket(p);
    }
}