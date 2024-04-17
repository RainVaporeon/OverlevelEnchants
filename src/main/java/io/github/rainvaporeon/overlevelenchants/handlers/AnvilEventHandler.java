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
        Map<Enchantment, Integer> appendSet = new HashMap<>(left.getEnchantments());
        Map<Enchantment, Integer> storedEnchantmentSet = new HashMap<>();
        if (left.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            storedEnchantmentSet.putAll(enchantStorage.getStoredEnchants());
        }
        right.getEnchantments().forEach((key, value) -> appendSet.compute(key, (k, v) -> {
            if (v == null) return value;
            if (value < v) return v;
            return value;
        }));
        if (right.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            enchantStorage.getStoredEnchants().forEach((key, value) -> storedEnchantmentSet.compute(key, (k, v) -> {
                if (v == null) return value;
                if (value < v) return v;
                return value;
            }));
        }

        if (result.getType() == Material.ENCHANTED_BOOK) {
            if (result.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
                storedEnchantmentSet.forEach((k, v) -> {
                    int metaLevel = enchantStorage.getStoredEnchantLevel(k);
                    int appendLevel = appendSet.getOrDefault(k, 0);
                    if (metaLevel != 0 || appendLevel != 0) {
                        // pick highest
                        int max = v;
                        if (metaLevel > max) max = metaLevel;
                        if (appendLevel > max) max = appendLevel;
                        enchantStorage.addStoredEnchant(k, max, true);
                    } else {
                        // just add
                        enchantStorage.addStoredEnchant(k, v, true);
                    }
                });
            }
        } else {
            result.addUnsafeEnchantments(appendSet);
            storedEnchantmentSet.forEach((k, v) -> {
                int appendLevel = appendSet.getOrDefault(k, 0);
                result.addUnsafeEnchantment(k, Math.max(v, appendLevel));
            });
        }

        event.setResult(result);
    }

    private boolean isOverleveled(Map.Entry<Enchantment, Integer> entry) {
        return entry.getKey().getMaxLevel() < entry.getValue();
    }
}
