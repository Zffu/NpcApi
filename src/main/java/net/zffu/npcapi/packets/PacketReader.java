package net.zffu.npcapi.packets;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;
import net.zffu.npcapi.NpcAPI;
import net.zffu.npcapi.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

public class PacketReader {
    Player player;
    Channel channel;

    public PacketReader(Player player) {
        this.player = player;
    }

    public void inject() {
        CraftPlayer cPlayer = (CraftPlayer) this.player;
        channel = cPlayer.getHandle().playerConnection.networkManager.channel;
        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext arg0, Packet<?> packet, List<Object> arg2) throws Exception {
                arg2.add(packet);
                readPacket(packet);
            }
        });
    }

    public void uninject() {
        try {
            if (channel.pipeline().get("PacketInjector") != null) {
                channel.pipeline().remove("PacketInjector");
            }
        } catch (Exception e){}
    }


    public void readPacket(Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {
            int id = (Integer) getValue(packet, "a");

            for (NPC npc : NpcAPI.getInstance().getRegistry().getNPCs()) {
                if (npc.getId() == id) {
                    if (getValue(packet, "action").toString().equalsIgnoreCase("ATTACK")) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NpcAPI.getInstance().getPlugin(), () -> {
                            if(player.isSneaking()) {
                                NpcAPI.getInstance().getRegistry().getNPC(npc.getIdName()).onClick(new NPC.PlayerClickNPCEvent(player, NPC.PlayerClickNPCEvent.ClickType.SHIFT_LEFT, id, npc));
                                return;
                            }
                            NpcAPI.getInstance().getRegistry().getNPC(npc.getIdName()).onClick(new NPC.PlayerClickNPCEvent(player, NPC.PlayerClickNPCEvent.ClickType.LEFT, id, npc));
                        }, 1);

                    } else if (getValue(packet, "action").toString().equalsIgnoreCase("INTERACT")) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NpcAPI.getInstance().getPlugin(), () -> {
                            if(player.isSneaking()) {
                                NpcAPI.getInstance().getRegistry().getNPC(npc.getIdName()).onClick(new NPC.PlayerClickNPCEvent(player, NPC.PlayerClickNPCEvent.ClickType.SHIFT_RIGHT, id, npc));
                                return;
                            }
                            NpcAPI.getInstance().getRegistry().getNPC(npc.getIdName()).onClick(new NPC.PlayerClickNPCEvent(player, NPC.PlayerClickNPCEvent.ClickType.RIGHT, id, npc));
                        }, 1);
                    }
                }
            }
        }
    }


    public void setValue(Object obj, String name, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
        }
    }

    public Object getValue(Object obj, String name) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
        }
        return null;
    }
}