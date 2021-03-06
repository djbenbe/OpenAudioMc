package com.craftmend.openaudiomc.modules.players;

import com.craftmend.openaudiomc.OpenAudioMc;
import com.craftmend.openaudiomc.modules.players.commands.ConnectCommand;
import com.craftmend.openaudiomc.modules.players.listeners.PlayerConnectionListener;
import com.craftmend.openaudiomc.modules.players.objects.Client;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerModule {

    private Map<UUID, Client> clientMap = new HashMap<>();

    public PlayerModule(OpenAudioMc openAudioMc) {
        openAudioMc.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), openAudioMc);
        openAudioMc.getCommand("audio").setExecutor(new ConnectCommand());
    }

    /**
     * @param player registers the player
     */
    public void register(Player player) {
        clientMap.put(player.getUniqueId(), new Client(player));
    }

    /**
     * @param uuid the uuid of a player
     * @return the client that corresponds to the player. can be null
     */
    public Client getClient(UUID uuid) {
        return clientMap.get(uuid);
    }

    /**
     * @return a collection of all clients
     */
    public Collection<Client> getClients() {
        return clientMap.values();
    }

    /**
     * @param player target player
     * @return the connection of the player
     */
    public Client getClient(Player player) {
        return getClient(player.getUniqueId());
    }

    /**
     * @param player the player to unregister
     */
    public void remove(Player player) {
        if (clientMap.containsKey(player.getUniqueId())) {
            clientMap.get(player.getUniqueId()).kick();
            clientMap.remove(player.getUniqueId());
        }
    }
}
