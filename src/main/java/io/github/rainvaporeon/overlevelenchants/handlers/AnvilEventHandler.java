package io.github.rainvaporeon.overlevelenchants.handlers;

import io.github.rainvaporeon.overlevelenchants.Plugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;

public class AnvilEventHandler implements Listener {

    @EventHandler
    public void onAnvilModification(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack left = inventory.getFirstItem();
        ItemStack right = inventory.getSecondItem();
        ItemStack result = inventory.getResult();
        if (left == null || right == null) return;
        if (result == null) {
            return;
        }
        Map<Enchantment, Integer> resultSet = new HashMap<>(result.getEnchantments());
        Map<Enchantment, Integer> resultStoredSet = new HashMap<>();
        if (result.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            resultSet.putAll(enchantStorage.getStoredEnchants());
        }

        left.getEnchantments().forEach((ench, level) -> {
            if (ench.getMaxLevel() >= level) return;
            resultSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
        });
        right.getEnchantments().forEach((ench, level) -> {
            if (ench.getMaxLevel() >= level) return;
            resultSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
        });
        if (left.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            storage.getStoredEnchants().forEach((ench, level) -> {
                if (ench.getMaxLevel() >= level) return;
                resultStoredSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
            });
        }
        if (right.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            storage.getStoredEnchants().forEach((ench, level) -> {
                if (ench.getMaxLevel() >= level) return;
                resultStoredSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
            });
        }

        result.removeEnchantments();
        resultSet.forEach(result::addUnsafeEnchantment);
        if (result.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            storage.getStoredEnchants().keySet().forEach(storage::removeStoredEnchant);
            resultStoredSet.forEach((k, v) -> storage.addStoredEnchant(k, v, true));
        } else {
            resultStoredSet.forEach((k, v) -> {
                if (result.containsEnchantment(k)) {
                    if (result.getEnchantmentLevel(k) > v) return;
                }
                result.addUnsafeEnchantment(k, v);
            });
        }
        event.setResult(result);
    }

    private static void log(String message) {
        Plugin.getInstance().getLogger().info(message);
    }
}
