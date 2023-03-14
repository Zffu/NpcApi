package net.zffu.npcapi.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import net.zffu.npcapi.reflections.Reflections;
import net.zffu.npcapi.registry.NPCRegistry;
import net.zffu.npcapi.utils.NPCApiUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public abstract class NPC extends Reflections {

    private String idName;
    private String name;
    private int id;

    private String signature;
    private String texture;
    private GameProfile gameProfile;

    private World world;
    private double x;
    private double y;
    private double z;
    private Location location;

    private PacketPlayOutNamedEntitySpawn spawnPacket;
    private PacketPlayOutAnimation animationPacket;

    private NPCRegistry registry;

    public NPC(NPCRegistry registry, int id, String idName, String name) {
        this.registry = registry;
        this.idName = idName;
        this.name = name;
        this.id = id;
        buildGameprofile();
        buildLocation();
    }

    public abstract void onClick(PlayerClickNPCEvent event);

    public void create() {
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
        setValue(packet, "a", this.id);
        setValue(packet, "b", this.gameProfile.getId());
        setValue(packet, "c", getFixLocation(this.location.getX()));
        setValue(packet, "d", getFixLocation(this.location.getY()));
        setValue(packet, "e", getFixLocation(this.location.getZ()));
        setValue(packet, "f", getFixRotation(this.location.getYaw()));
        setValue(packet, "g", getFixRotation(this.location.getPitch()));
        setValue(packet, "h", 0);
        DataWatcher watcher = new DataWatcher(null);
        watcher.a(6, (float)20);
        watcher.a(10, (float)127);
        setValue(packet, "i", watcher);
        this.gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
        this.spawnPacket = packet;
        this.registry.addActiveNPC(this);
    }

    public void spawn(Player player) {
        addToTablist(player);
        sendPacket(spawnPacket, player);
        new BukkitRunnable() {
            @Override
            public void run() {
                removeFromTablist(player);
            }
        }.runTaskLater(this.registry.getApi().getPlugin(), 80);
    }

    public void despawn(Player player) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.id);
        removeFromTablist(player);
        sendPacket(packet, player);
    }

    public void remove(Player player) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.id);

    }

    public void removeFromTablist(Player player) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(this.gameProfile, 1, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(this.registry.getApi().getTabNamePrefix() + this.idName)[0]);
        @SuppressWarnings("unchecked")
        List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
        players.add(data);
        setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        setValue(packet, "b", players);
        sendPacket(packet, player);
    }

    public void addToTablist(Player player) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(this.gameProfile, 1, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(this.registry.getApi().getTabNamePrefix() + this.idName)[0]);
        @SuppressWarnings("unchecked")
        List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
        players.add(data);
        setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        setValue(packet, "b", players);
        sendPacket(packet, player);
    }

    public void rotateHeadtoPlayer(Player player) {
        Location npcloc = location.setDirection(player.getLocation().subtract(location).toVector());
        float yaw1 = npcloc.getYaw();
        float pitch1 = npcloc.getPitch();
        PacketPlayOutEntity.PacketPlayOutEntityLook packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.id, getFixRotation(yaw1), getFixRotation(pitch1), true);
        PacketPlayOutEntityHeadRotation packetHead = new PacketPlayOutEntityHeadRotation();
        setValue(packetHead, "a", this.id);
        setValue(packetHead, "b", getFixRotation(yaw1));
        sendPacket(packet, player);
        sendPacket(packetHead, player);
    }











    public void setIdName(String idName) {this.idName = idName;}
    public void setName(String name) {this.name = name; buildGameprofile();}
    public void setId(int id) {this.id = id;}
    public void setSignature(String signature) {this.signature  = signature;}
    public void setTexture(String texture) {this.texture = texture;}
    public void setWorld(World world) {this.world = world;}
    public void setX(double x) {this.x = x; buildLocation();}
    public void setY(double y) {this.y = y; buildLocation();}
    public void setZ(double z) {this.z = z; buildLocation();}

    public String getIdName() {return this.idName;}
    public String getName() {return this.name;}
    public int getId() {return this.id;}
    public String getSignature() {return this.signature;}
    public String getTexture() {return this.texture;}
    public World getWorld() {return this.world;}
    public GameProfile getGameProfile() {return this.gameProfile;}
    public Location getLocation() {return this.location;}

    public void buildGameprofile() {
        this.gameProfile = new GameProfile(UUID.randomUUID(), NPCApiUtils.colorize(this.name));
    }

    public void buildLocation() {
        this.location = new Location(this.world, this.x, this.y, this.z);

    }

    public int getFixLocation(double pos) {
        return MathHelper.floor(pos * 32.0D);
    }

    public byte getFixRotation(float rotation) {
        return (byte) ((int) (rotation * 256.0F / 360.0F));
    }





    public static class PlayerClickNPCEvent extends Event implements Cancellable {
        private final Player player;
        private final NPC npc;
        private boolean isCancelled;
        @Getter
        private final int entityId;
        private static final HandlerList HANDLERS = new HandlerList();
        @Getter
        private final ClickType clickType;
        public PlayerClickNPCEvent(Player player, ClickType clickType, int entityId, NPC npc) {
            this.player = player;
            this.npc = npc;
            this.clickType = clickType;
            this.entityId = entityId;
        }
        public NPC getNpc() {
            return npc;
        }
        public Player getPlayer() {
            return player;
        }
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
        @Override
        public boolean isCancelled() {
            return isCancelled;
        }
        @Override
        public void setCancelled(boolean b) {
            isCancelled = b;
        }
        public enum ClickType {
            LEFT,
            RIGHT,
            SHIFT_LEFT,
            SHIFT_RIGHT
        }
    }




}
