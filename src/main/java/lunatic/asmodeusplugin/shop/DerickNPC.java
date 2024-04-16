package lunatic.asmodeusplugin.shop;

import lunatic.asmodeusplugin.Main;
import lunatic.asmodeusplugin.treasurepower.TreasurePowerUpgrader;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class DerickNPC implements CommandExecutor, Listener {
    private Main plugin;
    private Inventory gui; // Declare the GUI inventory instance

    public DerickNPC(Main plugin){
        this.plugin = plugin;
        this.gui = Bukkit.createInventory(null, 27 , "§f        \uFE01");
        plugin.getServer().getPluginManager().registerEvents(this, plugin); // Register events
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Ensure correct command usage
        if (args.length != 1) {
            sender.sendMessage("Usage: /derickgui <player>");
            return true;
        }

        // Get the targeted player
        Player target = Bukkit.getServer().getPlayer(args[0]);

        // Check if the targeted player is online
        if (target == null) {
            sender.sendMessage("Player '" + args[0] + "' is not online.");
            return true;
        }

        // Open GUI for the targeted player
        openGui(target);

        return true;
    }

    public void openGui(Player player){
        // Clear previous items in the GUI
        gui.clear();

        // Create center item
        ItemStack buyTicket = new ItemStack(Material.NAME_TAG);
        ItemMeta buyTicketMeta = buyTicket.getItemMeta();
        buyTicketMeta.setDisplayName("§c§lBelial Ticket");
        List<String> lore = new ArrayList<>();
        lore.add("§7This ticket allows access to Belial's realm.");
        lore.add("");
        lore.add("§c§lPrice : 15000");
        buyTicketMeta.setLore(lore);
        buyTicket.setItemMeta(buyTicketMeta);

        gui.setItem(11, buyTicket);

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§6Upgrader Shop");
        playerHead.setItemMeta(skullMeta);

        gui.setItem(13, playerHead);

        ItemStack belialShop = new ItemStack(Material.NAME_TAG);
        ItemMeta belialShopMeta = belialShop.getItemMeta();
        belialShopMeta.setDisplayName("§c§lBelial Shop");
        List<String> loreShop = new ArrayList<>();
        loreShop.add("");
        loreShop.add("§7Open Belial Shop");
        loreShop.add("");
        belialShopMeta.setLore(loreShop);
        belialShop.setItemMeta(belialShopMeta);

        gui.setItem(15, belialShop);

        // Open the inventory for the player
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked inventory is the same as our GUI inventory
        if (event.getInventory().equals(gui)) {
            // Check if the clicked slot is slot 11
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getRawSlot() == 11) {
                event.setCancelled(true); // Prevents the item from being moved or picked up
                player.closeInventory(); // Closes the inventory

                double playerBalance = plugin.econ.getBalance(player);

                if (playerBalance > (15000)) {
                    EconomyResponse cost = plugin.econ.withdrawPlayer(player, 15000);
                    if (cost.transactionSuccess()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material belial_ticket " + player.getName() + " 1");
                    }else{
                        player.sendMessage("§cTransaction failed!");
                    }
                }else{
                    player.sendMessage("§cNot enough money!");
                }
            }
            if (event.getRawSlot() == 13){
                TreasurePowerUpgrader powerUpgrader = new TreasurePowerUpgrader(plugin);
                powerUpgrader.openGUI(player);
            }
            if (event.getRawSlot() == 15){
                AsmodeusShop asmodeusShop = new AsmodeusShop(plugin);
                asmodeusShop.openShopGUI(player);
            }
        }
    }
}
