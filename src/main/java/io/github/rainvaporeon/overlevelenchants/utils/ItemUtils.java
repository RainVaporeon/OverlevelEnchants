package io.github.rainvaporeon.overlevelenchants.utils;

import org.bukkit.inventory.ItemStack;

public class ItemUtils {
    public static boolean hasOverleveledEnchantments(ItemStack stack) {
        return stack.getEnchantments().entrySet().stream().anyMatch(entry -> entry.getKey().getMaxLevel() < entry.getValue());
    }
}
