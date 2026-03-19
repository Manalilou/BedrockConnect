package main.com.pyratron.pugmatt.bedrockconnect.server;

import main.com.pyratron.pugmatt.bedrockconnect.BedrockConnect;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    private static final String CONFIG_RESOURCE = "/servers-config.json";

    private List<ServerInfo> featuredServers = new ArrayList<>();
    private List<ServerInfo> otherServers    = new ArrayList<>();

    private String userMenuName  = "Mes Serveurs";
    private String userMenuImage = "https://i.imgur.com/nhumQVP.png";

    public ServerManager() {
        loadServers();
    }

    public void loadServers() {
        featuredServers.clear();
        otherServers.clear();

        InputStream resource = getClass().getResourceAsStream(CONFIG_RESOURCE);
        if (resource == null) {
            BedrockConnect.logger.error("[ServerManager] servers-config.json introuvable dans les resources du JAR !");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(resource)) {
            JSONObject root = (JSONObject) new JSONParser().parse(reader);

            // Lecture du bloc user_menu (nom + icône du bouton "Mes Serveurs")
            JSONObject userMenuObj = (JSONObject) root.get("user_menu");
            if (userMenuObj != null) {
                if (userMenuObj.get("name")  != null) userMenuName  = (String) userMenuObj.get("name");
                if (userMenuObj.get("image") != null) userMenuImage = (String) userMenuObj.get("image");
            }

            int f = parseArray((JSONArray) root.get("featured"), featuredServers, "featured");
            int o = parseArray((JSONArray) root.get("other"),    otherServers,    "other");

            BedrockConnect.logger.info(String.format(
                "[ServerManager] Chargé : %d featured, %d other (groupes inclus)", f, o
            ));
        } catch (Exception e) {
            BedrockConnect.logger.error("[ServerManager] Erreur chargement servers-config.json", e);
        }
    }

    private int parseArray(JSONArray array, List<ServerInfo> target, String category) {
        if (array == null) return 0;
        int count = 0;

        for (Object obj : array) {
            JSONObject entry = (JSONObject) obj;

            if (entry.get("name") == null) continue;

            Object enabledRaw = entry.get("enabled");
            boolean enabled = (enabledRaw == null) || Boolean.TRUE.equals(enabledRaw);
            if (!enabled) {
                BedrockConnect.logger.debug("[ServerManager] Entrée désactivée ignorée : " + entry.get("id"));
                continue;
            }

            String name    = (String) entry.get("name");
            String image   = (String) entry.get("image");

            JSONArray serversArray = (JSONArray) entry.get("servers");
            if (serversArray != null) {
                List<ServerInfo> groupServers = new ArrayList<>();
                parseArray(serversArray, groupServers, category);
                target.add(new ServerInfo(name, image, category, groupServers));
                BedrockConnect.logger.debug("[ServerManager] Groupe chargé [" + category + "] : " + name + " (" + groupServers.size() + " serveurs)");
                count++;
                continue;
            }

            if (entry.get("address") == null) continue;

            String address = (String) entry.get("address");
            int    port    = ((Long)  entry.get("port")).intValue();

            target.add(new ServerInfo(name, address, port, image, category));
            BedrockConnect.logger.debug("[ServerManager] Serveur chargé [" + category + "] : " + name);
            count++;
        }

        return count;
    }

    public List<ServerInfo> getFeaturedServers() { return featuredServers; }
    public List<ServerInfo> getOtherServers()    { return otherServers; }
    public String getUserMenuName()              { return userMenuName; }
    public String getUserMenuImage()             { return userMenuImage; }
}