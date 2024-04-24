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
        Map<Enchantment, Integer> appendSet = new HashMap<>(left.getEnchantments());
        Map<Enchantment, Integer> storedEnchantmentSet = new HashMap<>();
        if (result.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            storedEnchantmentSet.putAll(enchantStorage.getStoredEnchants());
        }
        result.getEnchantments().forEach((key, value) -> appendSet.compute(key, (k, v) -> {
            if (v == null) return value;
            if (appendSet.keySet().stream().anyMatch(ke -> !key.equals(ke) && key.conflictsWith(key))) return null;
            return Math.max(v, value);
        }));
        if (left.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            enchantStorage.getStoredEnchants().forEach((key, value) -> storedEnchantmentSet.compute(key, (k, v) -> {
                if (v == null) return value;
                if (storedEnchantmentSet.keySet().stream().anyMatch(ke -> !key.equals(ke) && key.conflictsWith(key))) return null;
                return Math.max(v, value);
            }));
        }
        right.getEnchantments().forEach((key, value) -> appendSet.compute(key, (k, v) -> {
            if (v == null) return value;
            // do not allow conflicting enchantments be added on RHS
            if (appendSet.keySet().stream().anyMatch(ke -> !key.equals(ke) && key.conflictsWith(key))) return null;
            return Math.max(v, value);
        }));
        if (right.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            enchantStorage.getStoredEnchants().forEach((key, value) -> storedEnchantmentSet.compute(key, (k, v) -> {
                if (v == null) return value;
                // do not allow conflicting enchantments be added on RHS
                if (storedEnchantmentSet.keySet().stream().anyMatch(ke -> !key.equals(ke) && key.conflictsWith(key))) return null;
                return Math.max(v, value);
            }));
        }

        // books can include multiple types
        appendSet.entrySet().removeIf(entry -> !entry.getKey().getItemTarget().includes(result.getType()));
        result.addUnsafeEnchantments(appendSet);
        if (result.getItemMeta() instanceof EnchantmentStorageMeta enchantStorage) {
            storedEnchantmentSet.forEach((k, v) -> {
                int metaLevel = enchantStorage.getStoredEnchantLevel(k);
                enchantStorage.addStoredEnchant(k, Math.max(metaLevel, v), true);
            });
            result.setItemMeta(enchantStorage);
        } else {
            appendSet.forEach(result::addUnsafeEnchantment);
            // override here to apply stored ones too
            storedEnchantmentSet.forEach((k, v) -> {
                if (!k.getItemTarget().includes(result)) return;
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
