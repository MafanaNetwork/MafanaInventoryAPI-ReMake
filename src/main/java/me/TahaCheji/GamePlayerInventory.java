package me.TahaCheji;

import de.tr7zw.nbtapi.NBTItem;
import me.TahaCheji.objects.DatabaseInventoryData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GamePlayerInventory {

    public List<ItemStack> inventoryItems;
    public List<ItemStack> inventoryArmor;
    public Player gamePlayer;
    public OfflinePlayer offlinePlayer;


    public GamePlayerInventory(Player gamePlayer) throws Exception {
        this.gamePlayer = gamePlayer;
        DatabaseInventoryData data = Inv.getInstance().getInvMysqlInterface().getData(gamePlayer.getPlayer());
        if(gamePlayer.getPlayer().isOnline()){
            List<ItemStack> pInv = new ArrayList<>();
            for (ItemStack itemStack : gamePlayer.getPlayer().getInventory()){
                pInv.add(itemStack);
            }
            inventoryItems = pInv;
            inventoryArmor = Arrays.asList(gamePlayer.getPlayer().getInventory().getArmorContents());
            return;
        }

        inventoryItems = Arrays.asList(new InventoryDataHandler(Inv.getInstance()).decodeItems(data.getRawInventory()));
        inventoryArmor = Arrays.asList(new InventoryDataHandler(Inv.getInstance()).decodeItems(data.getRawArmor()));
    }

    public GamePlayerInventory(OfflinePlayer offlinePlayer) throws Exception {
        this.offlinePlayer = offlinePlayer;
        DatabaseInventoryData data = Inv.getInstance().getInvMysqlInterface().getData(offlinePlayer);
        inventoryItems = Arrays.asList(new InventoryDataHandler(Inv.getInstance()).decodeItems(data.getRawInventory()));
        inventoryArmor = Arrays.asList(new InventoryDataHandler(Inv.getInstance()).decodeItems(data.getRawArmor()));
    }

    public List<ItemStack> getInventoryArmor() {
        return inventoryArmor;
    }

    public List<ItemStack> getInventoryItems() {
        return inventoryItems;
    }

    public void addItem(ItemStack itemStack) {
        List<ItemStack> itemStacks = getInventoryItems();
        itemStacks.add(itemStack);
        setInventoryItems(itemStacks);
    }

    public void removeItem(ItemStack itemStack) {
        List<ItemStack> itemStacks = getInventoryItems();
        List<ItemStack> updatedItemStacks = new ArrayList<>();

        for (ItemStack item : itemStacks) {
            if(item == null) {
                continue;
            }
            if(item.getItemMeta() == null) {
                continue;
            }
            String itemUUID = new NBTItem(item).getString("GameItemUUID");
            String targetUUID = new NBTItem(itemStack).getString("GameItemUUID");

            if (!itemUUID.equalsIgnoreCase(targetUUID)) {
                updatedItemStacks.add(item);
            }
        }

        setInventoryItems(updatedItemStacks);
    }


    public void addArmorItem(ItemStack armorItem) {
        List<ItemStack> armorItems = getInventoryArmor();
        armorItems.add(armorItem);
        setInventoryArmor(armorItems);
    }

    public void removeArmor(ItemStack itemStack) {
        List<ItemStack> itemStacks = getInventoryArmor();
        List<ItemStack> updatedItemStacks = new ArrayList<>();

        for (ItemStack item : itemStacks) {
            if(item == null) {
                continue;
            }
            if(item.getItemMeta() == null) {
                continue;
            }
            String itemUUID = new NBTItem(item).getString("GameItemUUID");
            String targetUUID = new NBTItem(itemStack).getString("GameItemUUID");

            if (!itemUUID.equalsIgnoreCase(targetUUID)) {
                updatedItemStacks.add(item);
            }
        }

        setInventoryArmor(updatedItemStacks);
    }



    public void setInventoryArmor(List<ItemStack> inventoryArmor) {
        this.inventoryArmor = inventoryArmor;
        Player player = gamePlayer.getPlayer();
        if(player == null) {
            saveOfflineInventory(getInventoryItems(), inventoryArmor, offlinePlayer);
            return;
        }
        if(player.isOnline()){
            player.getInventory().setArmorContents(inventoryArmor.toArray(new ItemStack[0]));

            player.updateInventory(); // Update the player's inventory view
            player.sendMessage(ChatColor.GREEN + "[MafanaInventory Manager]: Your armor has been updated.");
            return;
        }
        saveOfflineInventory(inventoryArmor, getInventoryArmor(), player);
    }

    public void setInventoryItems(List<ItemStack> inventoryItems) {
        Player player = gamePlayer.getPlayer();
        if(player == null) {
            saveOfflineInventory(inventoryItems, getInventoryArmor(), offlinePlayer);
            return;
        }
        this.inventoryItems = inventoryItems;
        if(player.isOnline()){
            player.getInventory().clear(); // Clear the player's current inventory

            for (int i = 0; i < inventoryItems.size(); i++) {
                ItemStack item = inventoryItems.get(i);
                player.getInventory().setItem(i, item);
            }

            player.updateInventory(); // Update the player's inventory view
            player.sendMessage(ChatColor.GREEN + "[MafanaInventory Manager]: Your inventory has been updated.");
            return;

        }
    }

    public void setInventoryArmor(List<ItemStack> inventoryArmor, OfflinePlayer player) {
        this.inventoryArmor = inventoryArmor;
        saveOfflineInventory(getInventoryItems(), inventoryArmor, player);
    }

    public void setInventoryItems(List<ItemStack> inventoryItems, OfflinePlayer player) {
        this.inventoryItems = inventoryItems;
        saveOfflineInventory(inventoryItems, getInventoryArmor(), player);
    }

    public void saveOfflineInventory(List<ItemStack> inventoryItems, List<ItemStack> inventoryArmor, OfflinePlayer player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Inv.getInstance(), () -> {
            try {
                ItemStack[] inventory = inventoryItems.toArray(new ItemStack[0]);
                ItemStack[] armor = inventoryArmor.toArray(new ItemStack[0]);
                Inv.getInstance().getInvMysqlInterface().setData(player,
                        Inv.getInstance().getInventoryDataHandler().encodeItems(inventory),
                        Inv.getInstance().getInventoryDataHandler().encodeItems(armor),
                        "True");
            } catch (Exception e) {
                e.printStackTrace(); // Log the exception
            }
        }, 2L);

    }

}
