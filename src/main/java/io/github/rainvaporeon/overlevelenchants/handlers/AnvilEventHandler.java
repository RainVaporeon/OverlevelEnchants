package io.github.rainvaporeon.overlevelenchants.handlers;

import io.github.rainvaporeon.overlevelenchants.Plugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

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
            resultStoredSet.putAll(enchantStorage.getStoredEnchants());
        } else {
            // In the event that the target is not a storage, we still want to
            // ensure that overleveled books can be applied.
            // Note that the result would do another comparison, so this is
            // a redundant preparation
            resultStoredSet.putAll(result.getEnchantments());
        }

        left.getEnchantments().forEach((ench, level) -> {
            resultSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
        });
        right.getEnchantments().forEach((ench, level) -> {
            resultSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
        });
        if (left.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            storage.getStoredEnchants().forEach((ench, level) -> {
                if (ench.getMaxLevel() >= level) return; // delegate to vanilla
                resultStoredSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
            });
        }
        if (right.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            storage.getStoredEnchants().forEach((ench, level) -> {
                if (ench.getMaxLevel() >= level) return; // delegate to vanilla
                resultStoredSet.computeIfPresent(ench, (k, v) -> Math.max(v, level));
            });
        }

        result.removeEnchantments();
        resultSet.forEach(result::addUnsafeEnchantment);
        // apply to storage, otherwise apply to item
        if (result.getItemMeta() instanceof EnchantmentStorageMeta storage) {
            storage.getStoredEnchants().keySet().forEach(storage::removeStoredEnchant);
            resultStoredSet.forEach((k, v) -> storage.addStoredEnchant(k, v, true));
            result.setItemMeta(storage);
        } else {
            resultStoredSet.forEach(result::addUnsafeEnchantment);
        }
        event.setResult(result);
    }

    private static void log(String message) {
        Plugin.getInstance().getLogger().info(message);
    }
}
