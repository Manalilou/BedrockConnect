package main.com.pyratron.pugmatt.bedrockconnect.server;

import main.com.pyratron.pugmatt.bedrockconnect.*;
import main.com.pyratron.pugmatt.bedrockconnect.config.Whitelist;
import main.com.pyratron.pugmatt.bedrockconnect.config.Custom.CustomEntry;
import main.com.pyratron.pugmatt.bedrockconnect.config.Custom.CustomServer;
import main.com.pyratron.pugmatt.bedrockconnect.config.Custom.CustomServerGroup;
import main.com.pyratron.pugmatt.bedrockconnect.logging.LogColors;
import main.com.pyratron.pugmatt.bedrockconnect.server.gui.MainFormButton;
import main.com.pyratron.pugmatt.bedrockconnect.server.gui.ManageFormButton;
import main.com.pyratron.pugmatt.bedrockconnect.server.gui.UIComponents;
import main.com.pyratron.pugmatt.bedrockconnect.server.gui.UIForms;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.data.AttributeData;
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.bedrock.util.ChainValidationResult;
import org.cloudburstmc.protocol.bedrock.util.ChainValidationResult.IdentityData;
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.security.PublicKey;
import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PacketHandler implements BedrockPacketHandler {
    private BedrockServerSession session;
    private String name;
    private String uuid;
    private IdentityData extraData;
    private BCPlayer player;
    private ScheduledThreadPoolExecutor executor = null;

    // Index du groupe featured ouvert (pour naviguer dans son sous-menu)
    private int selectedFeaturedGroup = -1;

    public PacketHandler(BedrockServerSession session, boolean packetListening) {
        this.session = session;
    }

    public void setPlayer(BCPlayer player) {
        this.player = player;
    }

    public String getIP(String hostname) {
        try {
            if (BedrockConnect.getConfig().canFetchFeaturedIps() || BedrockConnect.getConfig().canFetchIps()) {
                InetAddress host = InetAddress.getByName(hostname);
                String address = host.getHostAddress();
                BedrockConnect.logger.debug("Retrieved " + address + " from hostname " + hostname);
                return address;
            } else {
                return BedrockConnect.getConfig().getFeaturedServerIps().get(hostname);
            }
        } catch (UnknownHostException ex) {
            BedrockConnect.logger.error("Error retrieving IP from hostname", ex);
        }
        return hostname;
    }

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        if (BedrockConnect.getConfig().isDebugEnabled() && !(packet instanceof PlayerAuthInputPacket)) {
            String id = (name != null) ? name : session.getSocketAddress().toString();
            if (packet instanceof LoginPacket)
                BedrockConnect.logger.debug(LogColors.gray("[ " + id + " ] LoginPacket"));
            else
                BedrockConnect.logger.debug(LogColors.gray("[ " + id + " ] " + packet));
        }
        BedrockPacketHandler.super.handlePacket(packet);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(RequestChunkRadiusPacket packet) {
        ChunkRadiusUpdatedPacket chunkUpdate = new ChunkRadiusUpdatedPacket();
        chunkUpdate.setRadius(packet.getRadius());
        session.sendPacketImmediately(chunkUpdate);
        PlayStatusPacket playStatus = new PlayStatusPacket();
        playStatus.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(playStatus);
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(PlayerActionPacket packet) {
        player.movementOpen();
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(AnimatePacket packet) {
        if (packet.getAction() == AnimatePacket.Action.SWING_ARM)
            player.movementOpen();
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(ModalFormResponsePacket packet) {
        player.setActive();
        player.resetMovementOpen();

        switch (packet.getFormId()) {

            // ── MOTD ────────────────────────────────────────────────────────
            case UIForms.MOTD:
                if (BedrockConnect.getConfig().isMotdCooldownEnabled()) {
                    BedrockConnect.getDataUtil().setViewedMotd(player.getUuid());
                }
                player.openForm(UIForms.MAIN);
                break;

            // ── MENU PRINCIPAL ───────────────────────────────────────────────
            case UIForms.MAIN:
                if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                    if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                    player.openForm(UIForms.MAIN);
                } else {
                    int chosen = Integer.parseInt(packet.getFormData().replaceAll("\\s+", ""));

                    CustomEntry[] customServers = BedrockConnect.getConfig().getCustomServers();
                    List<String> playerServers  = player.getServerList();

                    MainFormButton button     = UIForms.getMainFormButton(chosen, customServers, playerServers);
                    int serverIndex           = UIForms.getServerIndex(chosen, customServers, playerServers);

                    switch (button) {
                        case USER_MENU:
                            session.sendPacketImmediately(UIForms.createUserMenu(player.getServerList(), session));
                            player.setCurrentForm(UIForms.USER_MENU);
                            break;
                        case CONNECT:
                            player.openForm(UIForms.DIRECT_CONNECT);
                            break;
                        case MANAGE:
                            player.openForm(UIForms.MANAGE_SERVER);
                            break;
                        case EXIT:
                            player.disconnect(BedrockConnect.getConfig().getLanguage().getWording("disconnect", "exit"));
                            break;
                        case USER_SERVER:
                            String address = player.getServerList().get(serverIndex);
                            if (address.split(":").length > 1) {
                                transfer(address.split(":")[0], Integer.parseInt(address.split(":")[1]));
                            } else {
                                player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "invalidUserServer"));
                            }
                            break;
                        case CUSTOM_SERVER:
                            CustomEntry server = customServers[serverIndex - playerServers.size()];
                            if (server instanceof CustomServer) {
                                transfer(((CustomServer) server).getAddress(), ((CustomServer) server).getPort());
                            } else if (server instanceof CustomServerGroup) {
                                player.setSelectedGroup(serverIndex - playerServers.size());
                                player.openForm(UIForms.SERVER_GROUP);
                            }
                            break;
                        case NG_SERVER:
                            // Serveur featured (peut être direct ou groupe)
                            int featuredIndex = serverIndex - customServers.length;
                            List<ServerInfo> featuredServers = BedrockConnect.getServerManager().getFeaturedServers();
                            if (featuredIndex >= 0 && featuredIndex < featuredServers.size()) {
                                ServerInfo featured = featuredServers.get(featuredIndex);
                                if (featured.isGroup()) {
                                    // Ouvrir le sous-menu du groupe
                                    selectedFeaturedGroup = featuredIndex;
                                    session.sendPacketImmediately(UIForms.createFeaturedGroup(featured, session));
                                    player.setCurrentForm(UIForms.FEATURED_GROUP);
                                } else {
                                    transfer(getIP(featured.getAddress()), featured.getPort());
                                }
                            }
                            break;
                        case OTHER_BUTTON:
                            // Le bouton "Other Server" précède "Quitter" — différencier par index
                            int customCount   = customServers.length;
                            int featuredCount = BedrockConnect.getServerManager().getFeaturedServers().size();
                            int otherBtnIndex = (BedrockConnect.getConfig().isUserServersEnabled() ? 1 : 0)
                                                + customCount + featuredCount;
                            int exitBtnIndex  = otherBtnIndex + (BedrockConnect.getConfig().featuredServer() ? 1 : 0);

                            if (chosen == exitBtnIndex) {
                                player.disconnect(BedrockConnect.getConfig().getLanguage().getWording("disconnect", "exit"));
                            } else {
                                player.openForm(UIForms.OTHER);
                            }
                            break;
                    }
                }
                break;

            // ── SOUS-MENU "MES SERVEURS" ─────────────────────────────────────
            case UIForms.USER_MENU:
                if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                    if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                    player.openForm(UIForms.MAIN);
                } else {
                    int chosen = Integer.parseInt(packet.getFormData().replaceAll("\\s+", ""));
                    switch (chosen) {
                        case UIForms.USER_MENU_CONNECT:
                            player.openForm(UIForms.DIRECT_CONNECT);
                            break;
                        case UIForms.USER_MENU_MANAGE:
                            player.openForm(UIForms.MANAGE_SERVER);
                            break;
                        case UIForms.USER_MENU_BACK:
                            player.openForm(UIForms.MAIN);
                            break;
                        default:
                            // Serveur perso du joueur (index >= USER_MENU_SERVER_OFFSET)
                            int serverIdx = chosen - UIForms.USER_MENU_SERVER_OFFSET;
                            List<String> playerServers = player.getServerList();
                            if (serverIdx >= 0 && serverIdx < playerServers.size()) {
                                String addr = playerServers.get(serverIdx);
                                if (addr.split(":").length > 1) {
                                    transfer(addr.split(":")[0], Integer.parseInt(addr.split(":")[1]));
                                } else {
                                    player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "invalidUserServer"));
                                }
                            }
                            break;
                    }
                }
                break;

            // ── SOUS-MENU GROUPE FEATURED ────────────────────────────────────
            case UIForms.FEATURED_GROUP:
                if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                    if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                    player.openForm(UIForms.MAIN);
                } else {
                    int chosen = Integer.parseInt(packet.getFormData().replaceAll("\\s+", ""));
                    if (chosen == 0) {
                        // Bouton "Retour"
                        player.openForm(UIForms.MAIN);
                    } else {
                        List<ServerInfo> featuredServers = BedrockConnect.getServerManager().getFeaturedServers();
                        if (selectedFeaturedGroup >= 0 && selectedFeaturedGroup < featuredServers.size()) {
                            ServerInfo group = featuredServers.get(selectedFeaturedGroup);
                            int subIndex = chosen - 1; // -1 pour le bouton retour
                            if (subIndex >= 0 && subIndex < group.getServers().size()) {
                                ServerInfo server = group.getServers().get(subIndex);
                                transfer(getIP(server.getAddress()), server.getPort());
                            }
                        }
                    }
                }
                break;

            // ── SOUS-MENU GROUPE CUSTOM ──────────────────────────────────────
            case UIForms.SERVER_GROUP:
                if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                    if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                    player.openForm(UIForms.MAIN);
                } else {
                    int chosen = Integer.parseInt(packet.getFormData().replaceAll("\\s+", ""));
                    CustomEntry[] customServers = BedrockConnect.getConfig().getCustomServers();
                    CustomServerGroup group = (CustomServerGroup) customServers[player.getSelectedGroup()];
                    if (chosen == 0) {
                        player.openForm(UIForms.MAIN);
                    } else {
                        CustomServer server = group.getServers().get(chosen - 1);
                        transfer(server.getAddress(), server.getPort());
                    }
                }
                break;

            // ── SOUS-MENU OTHER ──────────────────────────────────────────────
            case UIForms.OTHER:
                if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                    player.openForm(UIForms.MAIN);
                    break;
                }
                int chosenOther = Integer.parseInt(packet.getFormData().replaceAll("\\s+", ""));
                List<ServerInfo> otherServers = BedrockConnect.getServerManager().getOtherServers();
                if (chosenOther == otherServers.size()) {
                    player.openForm(UIForms.MAIN);
                } else if (chosenOther >= 0 && chosenOther < otherServers.size()) {
                    ServerInfo serverInfo = otherServers.get(chosenOther);
                    transfer(getIP(serverInfo.getAddress()), serverInfo.getPort());
                }
                break;

            // ── GESTION SERVEURS JOUEUR ──────────────────────────────────────
            case UIForms.MANAGE_SERVER:
                if (packet.getFormData() == null) {
                    if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                    player.openForm(UIForms.MAIN);
                } else {
                    int chosen = Integer.parseInt(packet.getFormData().replaceAll("\\s+", ""));
                    ManageFormButton button = UIForms.getManageFormButton(chosen);
                    switch (button) {
                        case ADD:    player.openForm(UIForms.ADD_SERVER); break;
                        case EDIT:   player.openForm(UIForms.EDIT_CHOOSE_SERVER); break;
                        case REMOVE: player.openForm(UIForms.REMOVE_SERVER); break;
                    }
                }
                break;

            case UIForms.ADD_SERVER:
                try {
                    if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                        if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                        player.openForm(UIForms.MANAGE_SERVER);
                    } else {
                        ArrayList<String> data = UIComponents.getFormData(packet.getFormData());
                        if (data.size() > 1) {
                            data = UIComponents.cleanAddress(data);
                            if (UIComponents.validateServerInfo(data.get(0), data.get(1), data.get(2), player)) {
                                player.addServer(data.get(0), data.get(1), data.get(2));
                                player.openForm(UIForms.MANAGE_SERVER);
                            }
                        }
                    }
                } catch (Exception e) {
                    player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "invalidServerConnect"));
                }
                break;

            case UIForms.DIRECT_CONNECT:
                try {
                    if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                        if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                        player.openForm(UIForms.MAIN);
                    } else {
                        ArrayList<String> data = UIComponents.getFormData(packet.getFormData());
                        if (data.size() > 1) {
                            data = UIComponents.cleanAddress(data);
                            if (UIComponents.validateServerInfo(data.get(0), data.get(1), data.get(2), player)) {
                                boolean addServer = Boolean.parseBoolean(data.get(3));
                                if (addServer) {
                                    if (player.addServer(data.get(0), data.get(1), data.get(2))) {
                                        transfer(data.get(0).replace(" ", ""), Integer.parseInt(data.get(1)));
                                    }
                                } else {
                                    transfer(data.get(0).replace(" ", ""), Integer.parseInt(data.get(1)));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "invalidServerConnect"));
                }
                break;

            case UIForms.EDIT_CHOOSE_SERVER:
                try {
                    if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                        if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                        player.openForm(UIForms.MANAGE_SERVER);
                    } else {
                        ArrayList<String> data = UIComponents.getFormData(packet.getFormData());
                        int chosen = Integer.parseInt(data.get(0));
                        String server = player.getServerList().get(chosen);
                        String[] serverInfo = UIComponents.validateAddress(server, player);
                        if (serverInfo != null) {
                            player.setEditingServer(chosen);
                            session.sendPacketImmediately(UIForms.createEditServer(serverInfo[0], serverInfo[1], serverInfo.length > 2 ? serverInfo[2] : ""));
                            player.setCurrentForm(UIForms.EDIT_SERVER);
                        }
                    }
                } catch (Exception e) {
                    player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "invalidServerEdit"));
                }
                break;

            case UIForms.EDIT_SERVER:
                if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                    if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                    player.openForm(UIForms.EDIT_CHOOSE_SERVER);
                } else {
                    ArrayList<String> data = UIComponents.getFormData(packet.getFormData());
                    if (data.size() > 1) {
                        data.set(0, data.get(0).replaceAll("\\s", ""));
                        data.set(1, data.get(1).replaceAll("\\s", ""));
                        if (UIComponents.validateServerInfo(data.get(0), data.get(1), data.get(2), player)) {
                            String value = data.get(0) + ":" + data.get(1);
                            if (!data.get(2).isEmpty()) value += ":" + data.get(2);
                            List<String> servers = player.getServerList();
                            servers.set(player.getEditingServer(), value);
                            player.setServerList(servers);
                            player.openForm(UIForms.EDIT_CHOOSE_SERVER);
                        }
                    }
                }
                break;

            case UIForms.REMOVE_SERVER:
                try {
                    if (packet.getFormData() == null || packet.getFormData().contains("null")) {
                        if (player.getCurrentForm() != packet.getFormId()) return PacketSignal.HANDLED;
                        player.openForm(UIForms.MANAGE_SERVER);
                    } else {
                        ArrayList<String> data = UIComponents.getFormData(packet.getFormData());
                        int chosen = Integer.parseInt(data.get(0));
                        List<String> serverList = player.getServerList();
                        serverList.remove(chosen);
                        player.setServerList(serverList);
                        player.openForm(UIForms.MANAGE_SERVER);
                    }
                } catch (Exception e) {
                    player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "invalidServerRemove"));
                }
                break;

            case UIForms.ERROR:
                player.openForm(player.getCurrentForm());
                break;
        }

        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(NetworkStackLatencyPacket packet) {
        UpdateAttributesPacket updateAttr = new UpdateAttributesPacket();
        updateAttr.setRuntimeEntityId(1);
        List<AttributeData> attributes = Collections.singletonList(
            new AttributeData("minecraft:player.level", 0f, 24791.00f, 0, 0f)
        );
        updateAttr.setAttributes(attributes);
        if (executor == null) executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(() -> session.sendPacket(updateAttr), 500, TimeUnit.MILLISECONDS);
        return PacketSignal.HANDLED;
    }

    public void transfer(String ip, int port) {
        try {
            TransferPacket tp = new TransferPacket();
            if (BedrockConnect.getConfig().canFetchIps() && UIComponents.isDomain(ip)) {
                tp.setAddress(getIP(ip));
            } else {
                tp.setAddress(ip);
            }
            tp.setPort(port);
            session.sendPacketImmediately(tp);
            BedrockConnect.logger.debug("Transferred player " + name + " to " + tp.getAddress() + ":" + tp.getPort());
        } catch (Exception e) {
            player.createError(BedrockConnect.getConfig().getLanguage().getWording("error", "transferError"));
        }
    }

    @Override
    public PacketSignal handle(SetLocalPlayerAsInitializedPacket packet) {
        if (BedrockConnect.getConfig().getMotdMessage() != null && player.canShowMotd()) {
            player.openForm(UIForms.MOTD);
        } else {
            player.openForm(UIForms.MAIN);
        }
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(RequestNetworkSettingsPacket packet) {
        int protocolVersion = packet.getProtocolVersion();
        BedrockCodec packetCodec = BedrockProtocol.getBedrockCodec(protocolVersion);
        if (packetCodec == null) {
            PlayStatusPacket status = new PlayStatusPacket();
            status.setStatus(protocolVersion > BedrockProtocol.DEFAULT_BEDROCK_CODEC.getProtocolVersion()
                ? PlayStatusPacket.Status.LOGIN_FAILED_SERVER_OLD
                : PlayStatusPacket.Status.LOGIN_FAILED_CLIENT_OLD);
            session.sendPacketImmediately(status);
            return PacketSignal.HANDLED;
        }
        session.setCodec(packetCodec);
        PacketCompressionAlgorithm algorithm = PacketCompressionAlgorithm.ZLIB;
        NetworkSettingsPacket responsePacket = new NetworkSettingsPacket();
        responsePacket.setCompressionAlgorithm(algorithm);
        responsePacket.setCompressionThreshold(0);
        session.sendPacketImmediately(responsePacket);
        session.setCompression(algorithm);
        return PacketSignal.HANDLED;
    }

    @Override
    public void onDisconnect(CharSequence reason) {
        if (executor != null) executor.shutdown();
        if (player != null) BedrockConnect.getServer().removePlayer(player);
        BedrockConnect.logger.info("[ " + LogColors.cyan(BedrockConnect.getServer().getPlayers().size() + " online") + " ] Player disconnected: " + name + " (xuid: " + uuid + ")");
    }

    @Override
    public PacketSignal handle(ResourcePackClientResponsePacket packet) {
        switch (packet.getStatus()) {
            case COMPLETED:
                BedrockConnect.getDataUtil().initializePlayerData(uuid, name, session, this);
                break;
            case HAVE_ALL_PACKS:
                ResourcePackStackPacket rs = new ResourcePackStackPacket();
                rs.setForcedToAccept(false);
                rs.setGameVersion("*");
                session.sendPacket(rs);
                break;
            default:
                session.disconnect("disconnectionScreen.resourcePack");
                break;
        }
        return PacketSignal.HANDLED;
    }

    @Override
    public PacketSignal handle(LoginPacket packet) {
        try {
            ChainValidationResult result = EncryptionUtils.validatePayload(packet.getAuthPayload());
            if (BedrockConnect.getConfig().isOnlineModeEnabled() && !result.signed()) {
                throw new RuntimeException("Chain not signed");
            }
            PublicKey identityPublicKey = result.identityClaims().parsedIdentityPublicKey();
            byte[] clientDataPayload = EncryptionUtils.verifyClientData(packet.getClientJwt(), identityPublicKey);
            if (clientDataPayload == null) throw new IllegalStateException("Client data isn't signed by the given chain data");
            if (result.identityClaims().extraData == null) throw new RuntimeException("AuthData was not found!");

            extraData = result.identityClaims().extraData;
            name = extraData.displayName;
            uuid = extraData.identity.toString();

            BedrockConnect.logger.debug("Player made it through login: " + name + " (xuid: " + uuid + ")");
            if (!result.signed()) BedrockConnect.logger.debug("Chain not signed: " + name);

            Whitelist whitelist = BedrockConnect.getConfig().getWhitelist();
            if (whitelist.hasWhitelist() && !whitelist.isPlayerWhitelisted(name)) {
                session.disconnect(whitelist.getWhitelistMessage());
                BedrockConnect.logger.info("Kicked " + name + " (xuid: " + uuid + "): \"" + whitelist.getWhitelistMessage() + "\"");
            }

            PlayStatusPacket status = new PlayStatusPacket();
            status.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
            session.sendPacket(status);

            SetEntityMotionPacket motion = new SetEntityMotionPacket();
            motion.setRuntimeEntityId(1);
            motion.setMotion(Vector3f.ZERO);
            session.sendPacket(motion);

            ResourcePacksInfoPacket resourcePacksInfo = new ResourcePacksInfoPacket();
            resourcePacksInfo.setForcedToAccept(false);
            resourcePacksInfo.setScriptingEnabled(false);
            resourcePacksInfo.setWorldTemplateId(UUID.randomUUID());
            resourcePacksInfo.setWorldTemplateVersion("*");
            session.sendPacket(resourcePacksInfo);
        } catch (Exception e) {
            session.disconnect("disconnectionScreen.internalError.cantConnect");
            throw new RuntimeException("Unable to complete login", e);
        }
        return PacketSignal.HANDLED;
    }
}