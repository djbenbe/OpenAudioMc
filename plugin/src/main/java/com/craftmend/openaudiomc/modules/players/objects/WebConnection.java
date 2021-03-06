package com.craftmend.openaudiomc.modules.players.objects;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.modules.players.interfaces.ClientConnection;
import com.craftmend.openaudiomc.services.networking.NetworkingService;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.URISyntaxException;

public abstract class WebConnection implements ClientConnection {

    //spigot
    @Getter protected Player player;

    //socket
    @Getter protected Boolean isConnected = false;
    protected Session session;

    WebConnection(Player player) {
        this.player = player;
        session = new TokenFactory().build(this);
    }

    /**
     * send the client a message that they can click on to open the client
     * and generate a hashed token
     */
    public void publishUrl() {
        NetworkingService service = OpenAudioMc.getInstance().getNetworkingService();
        if (service.isConnecting()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', OpenAudioMc.getInstance().getConfig().getString("messages.api-starting-up")));
            return;
        }

        if (isConnected) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', OpenAudioMc.getInstance().getConfig().getString("messages.client-already-connected")));
            return;
        }

        try {
            OpenAudioMc.getInstance().getNetworkingService().connectIfDown();
        } catch (URISyntaxException e) {
            player.sendMessage(OpenAudioMc.getLOG_PREFIX() + "Failed to execute goal.");
            e.printStackTrace();
        }

        TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', OpenAudioMc.getInstance().getConfig().getString("messages.click-to-connect")));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                OpenAudioMc.getInstance().getConfigurationModule().getDataConfig().getString("keyset.base-url") + session.getToken()));
        player.spigot().sendMessage(message);
    }

    /**
     * @return true if the client is connected
     */
    @Override
    public Boolean isConnected() {
        return this.isConnected;
    }
}
