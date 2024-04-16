package lunatic.asmodeusplugin.skills;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import lunatic.asmodeusplugin.Main;
import lunatic.asmodeusplugin.beam.Laser;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonsterGenerator implements Listener {
    private final Main plugin;

    int guiPasscode = 1;

    public MonsterGenerator(Main plugin) {
        this.plugin = plugin;
        clearBlock();
    }

    public void clearBlock(){
        World world = Bukkit.getWorld("worldAsmodeus");
        plugin.generatorBlockAbove = world.getBlockAt(-302, 22, 1330);
        plugin.generatorRespawnAnchorBlock = world.getBlockAt(-302, 23, 1330);

        plugin.generatorBlockAbove.setType(Material.AIR);
        plugin.generatorRespawnAnchorBlock.setType(Material.AIR);
    }

//    @EventHandler
//    public void testSkills(PlayerSwapHandItemsEvent event) {
//        spawnGenerator(event.getPlayer().getLocation().add(0, 5, 0));
//    }

    public void spawnGenerator(Location monsterLoc){
        World world = Bukkit.getWorld("worldAsmodeus");
        if (world != null) {
            plugin.generatorBlockAbove = world.getBlockAt(-302, 22, 1330);
            plugin.generatorRespawnAnchorBlock = world.getBlockAt(-302, 23, 1330);

            Laser laser = null;
            try {
                laser = new Laser.GuardianLaser(monsterLoc, plugin.generatorBlockAbove.getLocation(), 6, 15);
                new BukkitRunnable(){
                    int sound = 0;
                    @Override
                    public void run() {
                        if (sound < 5) {
                            monsterLoc.getWorld().playSound(monsterLoc, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2f, 2f);
                            sound++;
                        }else{

                            ArmorStand hologram = plugin.generatorRespawnAnchorBlock.getWorld().spawn(plugin.generatorRespawnAnchorBlock.getLocation().add(0.5, 2, 0.5), ArmorStand.class);
                            hologram.setInvisible(true);
                            hologram.setCustomName("§c§lMonster Generator");
                            hologram.setCustomNameVisible(true);
                            hologram.setMarker(true);
                            hologram.setGravity(true);

                            plugin.armorStandList.add(hologram);

                            plugin.generatorBlockAbove.getWorld().playSound(plugin.generatorBlockAbove.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 0f);
                            plugin.generatorBlockAbove.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, plugin.generatorBlockAbove.getLocation(), 1);
                            plugin.generatorBlockAbove.setType(Material.COMMAND_BLOCK);
                            plugin.generatorRespawnAnchorBlock.setType(Material.RESPAWN_ANCHOR);
                            plugin.generatorBlockAbove.setMetadata("MonsterGenerator", new FixedMetadataValue(plugin, true));
                            plugin.generatorRespawnAnchorBlock.setMetadata("MonsterGenerator", new FixedMetadataValue(plugin, true));
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 20, 20);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            laser.start(plugin);


            new BukkitRunnable(){
                @Override
                public void run() {
                    if (plugin.generatorRespawnAnchorBlock != null) {
                        if (plugin.generatorRespawnAnchorBlock.getType().equals(Material.RESPAWN_ANCHOR)) {
                            MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("BelialMinion").orElse(null);
                            Location spawnLocation = plugin.generatorRespawnAnchorBlock.getLocation().add(0, 1, 0);
                            mob.spawn(BukkitAdapter.adapt(spawnLocation), 1);

                            plugin.generatorRespawnAnchorBlock.getWorld().spawnParticle(Particle.CLOUD, plugin.generatorRespawnAnchorBlock.getLocation().add(0, 1, 0), 1);

                            plugin.generatorRespawnAnchorBlock.getWorld().playSound(plugin.generatorRespawnAnchorBlock.getLocation(), Sound.AMBIENT_UNDERWATER_EXIT, 1f, 2f);
                        }
                    }
                }
            }.runTaskTimer(plugin, 100, 100);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && clickedBlock.hasMetadata("MonsterGenerator")) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR)) {
                if (event.getHand().equals(EquipmentSlot.HAND)) {
                    createPasscodeGUI(player);
                }
            }
        }
    }


    public void createPasscodeGUI(Player player) {
        // Create a new custom inventory with 54 slots
        Inventory passcodeGUI = Bukkit.createInventory(new NoClickInventoryHolder(), 54, "§f        \uFE08");

        // Fill the remaining slots with black stained glass panes
        ItemStack blackGlassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackGlassMeta = blackGlassPane.getItemMeta();
        blackGlassMeta.setDisplayName("§0"); // Set display name
        blackGlassPane.setItemMeta(blackGlassMeta);

        for (int i = 0; i < 54; i++) {
            passcodeGUI.setItem(i, blackGlassPane);
        }

        // Generate random slots for red panes
        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < 54; i++) {
            availableSlots.add(i);
        }
        Collections.shuffle(availableSlots);

        // Place red panes in the first few slots
        for (int i = 0; i < 20; i++) {
            int randomSlot = availableSlots.get(i);

            // Create a red stained glass pane item
            ItemStack redGlassPane = new ItemStack(Material.RED_STAINED_GLASS_PANE, i + 1);
            ItemMeta redGlassMeta = redGlassPane.getItemMeta();
            redGlassMeta.setDisplayName("§c");
            redGlassPane.setItemMeta(redGlassMeta);

            passcodeGUI.setItem(randomSlot, redGlassPane);
        }

        // Opening the GUI for the player
        player.openInventory(passcodeGUI);
    }



    // Custom InventoryHolder implementation to disable clicking
    private static class NoClickInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null; // Return null to indicate that this inventory holder doesn't have an inventory
        }
    }

    // Event handler to prevent clicking in the inventory
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof NoClickInventoryHolder) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true); // Cancel the event to prevent clicking in the inventory
            if (event.getCurrentItem().getAmount() == guiPasscode){
                event.getCurrentItem().setType(Material.GREEN_STAINED_GLASS_PANE);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 5, 1f);
                guiPasscode++;
                if (guiPasscode >= 21){
                    player.sendMessage("§aSuccess breaking the generator!");
                    player.sendMessage("§c§lBelial §f» ARGGHHH!! HOW CAN?!!");
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

                    plugin.generatorBlockAbove.setType(Material.AIR);
                    plugin.generatorRespawnAnchorBlock.setType(Material.AIR);
                    plugin.generatorBlockAbove.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, plugin.generatorBlockAbove.getLocation(), 1);
                    plugin.generatorBlockAbove.getWorld().playSound(plugin.generatorBlockAbove.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5, 0f);
                    player.closeInventory();
                    for (ArmorStand armorStand : plugin.armorStandList) {
                        armorStand.remove();
                    }
                    plugin.armorStandList.clear();
                    guiPasscode = 1;

                    for (Entity entity : Bukkit.getWorld("worldAsmodeus").getEntities()){
                        if (entity instanceof LivingEntity) {
                            if (entity.getName().equals("§c§lBelial")) {
                                LivingEntity ent = (LivingEntity) entity;
                                ent.setHealth(ent.getHealth()-2000);
                            }
                        }
                    }
                    player.sendTitle("§c§lBelial", "§fMega Madness!", 10, 70, 20);
                    player.sendMessage("§c§lBelial §f» Don't you dare to touch me again!!");
                    player.sendMessage("§c§lBelial §f» EAT THIS HAHAHA!");
                    player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

                    MythicMob cerberus = MythicBukkit.inst().getMobManager().getMythicMob("Cerberus").orElse(null);
                    Location cerberusLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -215, 26, 1343);
                    cerberus.spawn(BukkitAdapter.adapt(cerberusLocation), 1);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.sendMessage("§cNow kill Cerberus and Astaroth in Boner Lava Zone!");
                        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    }, 2 * 20L);
                }
            }else{
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_DEATH, 5, 1f);
                player.sendMessage("§cYou failed the passcode!");
                player.closeInventory();
                guiPasscode = 1;
            }
        }
    }
}
