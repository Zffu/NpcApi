package net.zffu.npcapi;

import net.zffu.npcapi.listeners.PlayerJoinListener;
import net.zffu.npcapi.registry.NPCRegistry;
import org.bukkit.plugin.Plugin;

public class NpcAPI {

    private Plugin plugin;

    private String tabNamePrefix = "§8";
    private int viewDistance = 60;
    private int refreshTime = 1;

    private static NpcAPI INSTANCE;

    private NPCRegistry registry;

    public NpcAPI(Plugin plugin) {
        INSTANCE = this;
        this.plugin = plugin;
        this.registry = new NPCRegistry(this);
        this.plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this.plugin);
    }

    public void setTabNamePrefix(String tabNamePrefix) {this.tabNamePrefix = tabNamePrefix;}
    public void setViewDistance(int viewDistance) {this.viewDistance = viewDistance;}
    public void setRefreshTime(int refreshTime) {this.refreshTime = refreshTime;}

    public NPCRegistry getRegistry() {return this.registry;}

    public static NpcAPI getInstance() {return INSTANCE;}

    public String getTabNamePrefix() {return this.tabNamePrefix;}
    public int getViewDistance() {return this.viewDistance;}
    public int getRefreshTime() {return this.refreshTime;}

    public Plugin getPlugin() {return this.plugin;}


}
