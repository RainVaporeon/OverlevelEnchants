package io.github.rainvaporeon.overlevelenchants;

import io.github.rainvaporeon.overlevelenchants.handlers.AnvilEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin {
    private static org.bukkit.plugin.Plugin INSTANCE;
    private static final Object lock = new Object();;

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.getLogger().info("Registering event handlers");
        AnvilEventHandler anvilHandler = new AnvilEventHandler();
        Bukkit.getPluginManager().registerEvents(anvilHandler, this);
        this.getLogger().info("Registered event handlers");
    }

    @Override
    public void onDisable() {
        if (Plugin.getInstance() == null) return;
        this.getLogger().info("De-registering event handlers...");
        HandlerList.unregisterAll(INSTANCE);
        INSTANCE = null;
        this.getLogger().info("OK! Quitting.");
    }

    public static org.bukkit.plugin.Plugin getInstance() {
        synchronized (lock) {
            return INSTANCE;
        }
    }

}
