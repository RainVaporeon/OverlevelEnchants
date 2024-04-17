package io.github.rainvaporeon.overlevelenchants.handlers;

import io.github.rainvaporeon.overlevelenchants.Plugin;
import org.bukkit.Material;
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
        Map<Enchantment, Integer> appendSet = new HashMap<>(result.getEnchantments());
        Map<Enchantment, Integer> storedEnchantmentSet = new HashMap<>();
        if (result.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            storedEnchantmentSet.putAll(enchantStorage.getStoredEnchants());
        }
        left.getEnchantments().forEach((key, value) -> appendSet.compute(key, (k, v) -> {
            if (v == null) return value;
            return Math.max(v, value);
        }));
        if (left.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            enchantStorage.getStoredEnchants().forEach((key, value) -> storedEnchantmentSet.compute(key, (k, v) -> {
                if (v == null) return value;
                log("left meta k, max = " + k + ", " + Math.max(v, value));
                return Math.max(v, value);
            }));
        }
        right.getEnchantments().forEach((key, value) -> appendSet.compute(key, (k, v) -> {
            if (v == null) return value;
            return Math.max(v, value);
        }));
        if (right.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            enchantStorage.getStoredEnchants().forEach((key, value) -> storedEnchantmentSet.compute(key, (k, v) -> {
                if (v == null) return value;
                log("right meta k, max = " + k + ", " + Math.max(v, value));
                return Math.max(v, value);
            }));
        }

        result.addUnsafeEnchantments(appendSet);
        if (result.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            storedEnchantmentSet.forEach((k, v) -> {
                int metaLevel = enchantStorage.getStoredEnchantLevel(k);
                enchantStorage.addStoredEnchant(k, Math.max(metaLevel, v), true);
            });
            result.setItemMeta(enchantStorage);
        } else {
            storedEnchantmentSet.forEach((k, v) -> {
                int appendLevel = appendSet.getOrDefault(k, 0);
                result.addUnsafeEnchantment(k, Math.max(v, appendLevel));
            });
        }

        event.setResult(result);
    }

    private static void log(String message) {
        Plugin.getInstance().getLogger().info(message);
    }
}
