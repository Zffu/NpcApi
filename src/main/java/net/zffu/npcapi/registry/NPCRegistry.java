package net.zffu.npcapi.registry;

import net.zffu.npcapi.NpcAPI;
import net.zffu.npcapi.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class NPCRegistry {

    private NpcAPI api;

    private Map<String, List<String>> VIEWING_NPCS = new HashMap<>();
    private Map<String, NPC>  npcRegistry = new HashMap<>();
    private Map<String, Boolean> idkMap = new HashMap<>();
    private Map<UUID, BukkitTask> despawnTaskMap = new HashMap<>();
    private Map<UUID, BukkitTask> rotationTaskMap = new HashMap<>();

    private ArrayList<NPC> npcs = new ArrayList<>();

    public NPCRegistry(NpcAPI api) {
        this.api = api;
    }

    public boolean register(NPC npc) {
        this.add(npc.getIdName(), npc);
        return true;
    }

    public NPC getNPC(String id) {
        return this.npcRegistry.get(id);
    }

    public boolean add(String id, NPC npc) {
        npc.setRegistry(this);
        this.npcRegistry.put(id, npc);
        return true;
    }

    public void addActiveNPC(NPC npc) {
        this.npcs.add(npc);
    }

    public ArrayList<NPC> getNPCs() {
        return this.npcs;
    }

    public void removeActiveNPC(NPC npc) {
        if(this.npcs.contains(npc)) {
            this.npcs.remove(npc);
        }
    }

    public NpcAPI getApi() {return this.api;}

    public void startDespawnPreventer() {
        Bukkit.getScheduler().runTaskTimer(this.api.getPlugin(), () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                for(NPC npc : this.npcs) {
                    List<String> viewing = this.VIEWING_NPCS.get(player.getName());

                    if(npc.getLocation().distance(player.getLocation()) >= this.api.getViewDistance()) {
                        npc.despawn(player);

                        viewing.remove(npc.getIdName());
                        this.VIEWING_NPCS.put(player.getName(), viewing);
                    }
                    else {
                        if(viewing.contains(npc.getIdName())) return;

                        npc.despawn(player);
                        Bukkit.getScheduler().runTaskLater(this.api.getPlugin(), () -> npc.spawn(player), 5);

                        viewing.add(npc.getIdName());
                        this.VIEWING_NPCS.put(player.getName(), viewing);
                    }
                }
            });
        }, 10, 20);
    }

}
