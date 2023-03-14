package net.zffu.npcapi.listeners;

import net.zffu.npcapi.packets.PacketReader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new PacketReader(event.getPlayer()).inject();
    }

}
