package lunatic.asmodeusplugin.treasurepower;

import lunatic.asmodeusplugin.Main;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TreasurePowerUpgrader implements CommandExecutor, Listener {
    private Main plugin;

    public TreasurePowerUpgrader(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        openGUI(player);
        return true;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, 27, "§f        \uFE01");

        // Fill the first and third rows with empty slots
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, new ItemStack(Material.AIR));
            gui.setItem(i + 18, new ItemStack(Material.AIR));
        }

        // Place the player head in the center of the GUI
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§6" + player.getName() + "'s Treasure Power");

        // Treasure Power
        List<String> lore = new ArrayList<>();
        lore.add("");
        int treasurePower = 0;
        try {
            treasurePower = plugin.database.getTreasurePower(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        lore.add("§7Your current power: §6" + treasurePower + "%");
        lore.add("§bNext Upgrade §7: " + treasurePower + "%" + " §7» §b" + (treasurePower + 1) + "%");
        lore.add("");
        lore.add("§eUpgrade Price » " + (10_000 + treasurePower * 1_000));
        skullMeta.setLore(lore);

        playerHead.setItemMeta(skullMeta);
        gui.setItem(13, playerHead);


        // Damage Resistance
        int resistancePower = 0;
        try {
            resistancePower = plugin.database.getDamageResistance(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ItemStack damageResistance = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta damageResistanceMeta = damageResistance.getItemMeta();
        damageResistanceMeta.setDisplayName("§aDamage Resistance");
        List<String> damageResistanceLore = new ArrayList<>();
        damageResistanceLore.add("§7Increase your damage resistance");
        damageResistanceLore.add("§7agains §cBelial§7!");
        damageResistanceLore.add("");
        damageResistanceLore.add("§7Your current power: §6" + resistancePower + "%");
        damageResistanceLore.add("§bNext Upgrade §7: " + resistancePower + "%" + " §7» §b" + (resistancePower + 1) + "%");
        damageResistanceLore.add("");
        damageResistanceLore.add("§eUpgrade Price » " + (30_000 + resistancePower * 10_000));
        damageResistanceMeta.setLore(damageResistanceLore);
        damageResistanceMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        damageResistance.setItemMeta(damageResistanceMeta);
        gui.setItem(11, damageResistance);

        // Damage Booster
        int boosterPower = 0;
        try {
            boosterPower = plugin.database.getDamageBooster(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ItemStack damageBooster = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta damageBoosterMeta = damageBooster.getItemMeta();
        damageBoosterMeta.setDisplayName("§cDamage Booster");
        List<String> damageBoosterLore = new ArrayList<>();
        damageBoosterLore.add("§7Increase your damage boost");
        damageBoosterLore.add("§7agains §cBelial§7!");
        damageBoosterLore.add("");
        damageBoosterLore.add("§7Your current power: §6" + boosterPower + "%");
        damageBoosterLore.add("§bNext Upgrade §7: " + boosterPower + "%" + " §7» §b" + (boosterPower + 1) + "%");
        damageBoosterLore.add("");
        damageBoosterLore.add("§eUpgrade Price » " + (20_000 + boosterPower * 5_000));
        damageBoosterMeta.setLore(damageBoosterLore);
        damageBoosterMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        damageBooster.setItemMeta(damageBoosterMeta);
        gui.setItem(15, damageBooster);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the click is from a player
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        // Check if the clicked inventory is the player's open inventory
        if (clickedInventory == null || !clickedInventory.equals(player.getOpenInventory().getTopInventory())) return;

        // Check if the event is already cancelled
        if (event.isCancelled()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;

        // Check if the clicked item is the player's head (Treasure Power)
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta != null && meta.getDisplayName().equals("§6" + player.getName() + "'s Treasure Power")) {
                handleTreasurePowerUpgrade(player);
                event.setCancelled(true);
            }
        }

        // Check if the clicked item is Damage Resistance
        if (clickedItem.getType() == Material.IRON_CHESTPLATE && clickedItem.getItemMeta().getDisplayName().equals("§aDamage Resistance")) {
            handleDamageResistanceUpgrade(player);
            event.setCancelled(true);
        }

        // Check if the clicked item is Damage Booster
        if (clickedItem.getType() == Material.DIAMOND_SWORD && clickedItem.getItemMeta().getDisplayName().equals("§cDamage Booster")) {
            handleDamageBoosterUpgrade(player);
            event.setCancelled(true);
        }
    }
    private void handleTreasurePowerUpgrade(Player player) {
        int treasurePower;
        try {
            treasurePower = plugin.database.getTreasurePower(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        double playerBalance = plugin.econ.getBalance(player);

        if (playerBalance > (10_000 + treasurePower * 1_000)) {
            EconomyResponse cost = plugin.econ.withdrawPlayer(player, (10_000 + treasurePower * 1_000));
            if (cost.transactionSuccess()) {
                int playerPower = 0;
                try {
                    plugin.database.addTreasurePower(player.getUniqueId(), player.getName());
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§e§lTreasure » §d" + player.getName() + " §fbaru saja mengupgrade §6Treasure Power §fke §6" + (treasurePower + 1) + "%§f!");
                    Bukkit.broadcastMessage("");
                    openGUI(player);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()){
                        onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_WITHER_HURT, 0.2f, 0.5f);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                // Notify the player about the upgrade
                player.sendMessage("§aYou have successfully upgraded your Damage Resistance!");
            }
        } else {
            // Notify the player if they don't have enough balance
            player.sendMessage("§cYou don't have enough money to upgrade your Damage Resistance!");
        }
    }
    private void handleDamageResistanceUpgrade(Player player) {
        int resistancePower;
        try {
            resistancePower = plugin.database.getDamageResistance(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (resistancePower < 10) {
            double playerBalance = plugin.econ.getBalance(player);

            // Check if player has enough balance for the upgrade
            if (playerBalance > (30_000 + resistancePower * 10_000)) {
                // Withdraw the cost from the player's balance
                EconomyResponse cost = plugin.econ.withdrawPlayer(player, (30_000 + resistancePower * 10_000));
                if (cost.transactionSuccess()) {
                    // Update the resistance power in the database
                    try {
                        plugin.database.addDamageResistance(player.getUniqueId(), player.getName());
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e§lTreasure » §d" + player.getName() + " §fbaru saja mengupgrade §bDamage Resistance §fke §6" + (resistancePower + 1) + "%§f!");
                        Bukkit.broadcastMessage("");
                        openGUI(player);
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_WITHER_HURT, 0.2f, 0.5f);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Notify the player about the upgrade
                    player.sendMessage("§aYou have successfully upgraded your Damage Resistance!");
                }
            } else {
                // Notify the player if they don't have enough balance
                player.sendMessage("§cYou don't have enough money to upgrade your Damage Resistance!");
            }
        }else{
            player.sendMessage("§cYou have been maxed out this upgrade!");
        }
    }

    private void handleDamageBoosterUpgrade(Player player) {
        int boosterPower;
        try {
            boosterPower = plugin.database.getDamageBooster(player.getUniqueId(), player.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (boosterPower < 50) {
            double playerBalance = plugin.econ.getBalance(player);

            // Check if player has enough balance for the upgrade
            if (playerBalance > (20_000 + boosterPower * 5_000)) {
                // Withdraw the cost from the player's balance
                EconomyResponse cost = plugin.econ.withdrawPlayer(player, (20_000 + boosterPower * 5_000));
                if (cost.transactionSuccess()) {
                    // Update the booster power in the database
                    try {
                        plugin.database.addDamageBooster(player.getUniqueId(), player.getName());
                        Bukkit.broadcastMessage("");
                        Bukkit.broadcastMessage("§e§lTreasure » §d" + player.getName() + " §fbaru saja mengupgrade §cDamage Booster §fke §6" + (boosterPower + 1) + "%§f!");
                        Bukkit.broadcastMessage("");
                        openGUI(player);
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_WITHER_HURT, 0.2f, 0.5f);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    // Notify the player about the upgrade
                    player.sendMessage("§aYou have successfully upgraded your Damage Booster!");
                }
            } else {
                // Notify the player if they don't have enough balance
                player.sendMessage("§cYou don't have enough money to upgrade your Damage Booster!");
            }
        }else{
            player.sendMessage("§cYou have been maxed out this upgrade!");
        }
    }
}
