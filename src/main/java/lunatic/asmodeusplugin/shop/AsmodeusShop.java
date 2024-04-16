package lunatic.asmodeusplugin.shop;

import lunatic.asmodeusplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.Arrays;

public class AsmodeusShop implements Listener, CommandExecutor {

    private Main plugin;
    private Inventory shopInventory;

    public AsmodeusShop(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory != null && clickedInventory.getHolder() instanceof BelialShopHolder) {
            event.setCancelled(true); // Prevent items from being taken out of the inventory
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                if (displayName.equals("Belial Xiphos")) {
                    Player player = (Player) event.getWhoClicked();

                    int demonEssence = 0;
                    try {
                        demonEssence = plugin.database.getDemonEssence(player.getUniqueId(), player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    if (demonEssence >= 1500){
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§c" + player.getName() + " §fbaru saja membeli §c§lBelial Xiphos§f!");
                        Bukkit.broadcastMessage("");
                        for (Player playerOnline : Bukkit.getOnlinePlayers()){
                            playerOnline.playSound(player, Sound.ENTITY_WITHER_SHOOT, 10f, 0f);
                        }
                        try {
                            plugin.database.subtractDemonEssence(player.getUniqueId(), player.getName(), 1500);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give sword belial_xiphos " + player.getName() + " 1");
                    }else{
                        player.sendMessage("§cYou don't have enough Demon Essence for this!");
                    }
                }
                if (displayName.equals("Demon Nucleus")) {
                    Player player = (Player) event.getWhoClicked();

                    int demonEssence = 0;
                    try {
                        demonEssence = plugin.database.getDemonEssence(player.getUniqueId(), player.getName());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    if (demonEssence >= 5000){
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§c" + player.getName() + " §fbaru saja membeli §c§lDemon Nucleus§f!");
                        Bukkit.broadcastMessage("");
                        for (Player playerOnline : Bukkit.getOnlinePlayers()){
                            playerOnline.playSound(player, Sound.ENTITY_WITHER_SHOOT, 10f, 0f);
                        }
                        try {
                            plugin.database.subtractDemonEssence(player.getUniqueId(), player.getName(), 1500);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material demon_nucleus " + player.getName() + " 1");
                    }else{
                        player.sendMessage("§cYou don't have enough Demon Essence for this!");
                    }
                }
            }
        }

    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            openShopGUI((Player) sender);
        } else {
            sender.sendMessage("Only players can execute this command!");
        }
        return true;
    }


    public void openShopGUI(Player player) {
        Inventory shopInventory = Bukkit.createInventory(new BelialShopHolder(), 54, "§f        \uFE04");

        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackGlassMeta = blackGlass.getItemMeta();
        blackGlassMeta.setDisplayName("§c");
        blackGlass.setItemMeta(blackGlassMeta);
        for (int i = 0; i < 9; i++) {
            shopInventory.setItem(i, blackGlass);
        }
        shopInventory.setItem(9, blackGlass);
        shopInventory.setItem(17, blackGlass);
        shopInventory.setItem(18, blackGlass);
        shopInventory.setItem(26, blackGlass);
        shopInventory.setItem(27, blackGlass);
        shopInventory.setItem(35, blackGlass);
        shopInventory.setItem(36, blackGlass);
        shopInventory.setItem(44, blackGlass);
        for (int i = 45; i < 54; i++) {
            shopInventory.setItem(i, blackGlass);
        }

        ItemStack belialXiphos = new ItemStack(Material.NETHERITE_SWORD, 1);
        ItemMeta belialMeta = belialXiphos.getItemMeta();
        belialMeta.setDisplayName("§c§lBelial Xiphos");
        belialMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        String[] lore = {
                "§7Belial Killed : 0",
                "",
                "§7Skills: §6Gigantic Slap §e§lRIGHT-CLICK",
                "§7Spawn a giant sword in front of you and",
                "§7adeal 80 damages on normal mobs, if this",
                "§7skills hitting monster, slowing them down",
                "§7by giving §bSlowness II §7for 3s",
                "§8Cooldown: §a15s",
                "",
                "§7Skills: §6Soul of Madness §e§lPASSIVE",
                "§7Every kill of Belial, it will increase",
                "§7your damage by §c+1",
                "",
                "§c§lPrice : 1500 Demon Essence"
        };

        belialMeta.setLore(Arrays.asList(lore));
        belialXiphos.setItemMeta(belialMeta);

        shopInventory.setItem(10, belialXiphos);

        ItemStack demonNucleus = new ItemStack(Material.RED_DYE, 1);
        ItemMeta demonNucleusMeta = demonNucleus.getItemMeta();
        demonNucleusMeta.setDisplayName("§c§lDemon Nucleus");
        demonNucleusMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        String[] loreDemonNucleus = {
                "§7Use this item to upgrade §c§lOrbs",
                "§7you can use this at §6/warp belial",
                "",
                "§c§lPrice : 5000 Demon Essence"
        };

        demonNucleusMeta.setLore(Arrays.asList(loreDemonNucleus));
        demonNucleus.setItemMeta(demonNucleusMeta);

        shopInventory.setItem(11, demonNucleus);

        int playerDemonEssence = 0;
        try {
            playerDemonEssence = plugin.database.getDemonEssence(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ItemStack demonEssence = new ItemStack(Material.REDSTONE, 1);
        ItemMeta demonEssenceMeta = demonEssence.getItemMeta();
        demonEssenceMeta.setDisplayName("§c§lStatus");
        demonEssenceMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        String[] loredemonEssence = {
                "",
                "§7Your Essence : §c" +playerDemonEssence+ " Demon Essence",
                ""
        };

        demonEssenceMeta.setLore(Arrays.asList(loredemonEssence));
        demonEssence.setItemMeta(demonEssenceMeta);

        shopInventory.setItem(4, demonEssence);

        player.openInventory(shopInventory);
    }


    private class BelialShopHolder implements org.bukkit.inventory.InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null; // No need to implement since we won't access the inventory holder
        }
    }
}
