package main.com.pyratron.pugmatt.bedrockconnect.server;

import java.util.List;

/**
 * Représente un serveur ou un groupe de serveurs dans la liste.
 *
 * Si servers != null → c'est un groupe (bouton qui ouvre un sous-menu)
 * Si servers == null → c'est un serveur direct (bouton qui connecte directement)
 *
 * Dans servers-config.json :
 *   Serveur direct  → objet avec "address" et "port"
 *   Groupe          → objet avec "servers" (tableau de serveurs directs), sans "address"
 */
public class ServerInfo {
    private String name;
    private String imageUrl;
    private String category;

    // Serveur direct
    private String address;
    private int port;

    // Groupe (sous-menu)
    private List<ServerInfo> servers;

    /** Constructeur pour un serveur direct */
    public ServerInfo(String name, String address, int port, String imageUrl, String category) {
        this.name     = name;
        this.address  = address;
        this.port     = port;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    /** Constructeur pour un groupe de serveurs */
    public ServerInfo(String name, String imageUrl, String category, List<ServerInfo> servers) {
        this.name     = name;
        this.imageUrl = imageUrl;
        this.category = category;
        this.servers  = servers;
    }

    public boolean isGroup() {
        return servers != null && !servers.isEmpty();
    }

    public String getName()         { return name; }
    public String getAddress()      { return address; }
    public int    getPort()         { return port; }
    public String getImageUrl()     { return imageUrl; }
    public String getCategory()     { return category; }
    public List<ServerInfo> getServers() { return servers; }
}
