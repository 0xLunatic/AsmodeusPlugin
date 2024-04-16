package lunatic.asmodeusplugin.skills;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import lunatic.asmodeusplugin.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static lunatic.asmodeusplugin.Main.Action.*;

public class EvasiveManeuver implements Listener {
    private final Main plugin;
    public Map<Location, Material> originalBlocks;


    public EvasiveManeuver(Main plugin) {
        this.plugin = plugin;
        this.originalBlocks = new HashMap<>();
    }

//    @EventHandler
    public void evasiveManuever(Player player) {

        if (plugin.asmodeusPlayer.equals(player.getName())) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendTitle("§c§l§kBelial", "§cEvasive Maneuver!");
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                }
            }.runTaskLater(plugin, 10); // 40 ticks = 2 seconds

            // Add a 2-second delay before showing the second title
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendTitle("§c§lEvasive Maneuver", "§fDodge incoming attack!");
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                }
            }.runTaskLater(plugin, 40); // 40 ticks = 2 seconds

            // Add another 2-second delay before invoking setDanceFloor
            new BukkitRunnable() {
                @Override
                public void run() {
                    setDanceFloor(player);
                }
            }.runTaskLater(plugin, 80); // 80 ticks = 4 seconds (2 seconds after the second title)
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Vector velocity = player.getVelocity();
        // Check if the player is moving "up"
        if (velocity.getY() > 0) {
            // Default jump velocity
            double jumpVelocity = (double) 0.42F; // Default jump velocity
            PotionEffect jumpPotion = player.getPotionEffect(PotionEffectType.JUMP);
            if (jumpPotion != null) {
                // If player has jump potion add it to jump velocity
                jumpVelocity += (double) ((float) jumpPotion.getAmplifier() + 1) * 0.1F;
            }
            // Check if player is not on ladder and if jump velocity calculated is equals to player Y velocity
            if (player.getLocation().getBlock().getType() != Material.LADDER && Double.compare(velocity.getY(), jumpVelocity) == 0) {
                if (plugin.actions.get(player) != null) {
                    if (plugin.isInAction) {
                        if (plugin.actions.get(player) == JUMP) {
                            handleAction(event.getPlayer(), event.getPlayer().getLocation().getBlock().getLocation());
                        } else {
                            player.sendTitle("§c§lFailed!", "§fYou failed to " + plugin.actions.get(player) + "!", 10, 70, 20);
                            player.damage(40);
                            plugin.actions.remove(player);
                            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 5, 0f);
                            evasiveManuever(player);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.getPlayer().isSneaking()) {
            if (plugin.actions.get(player) != null) {
                if (plugin.isInAction) {
                    if (plugin.actions.get(player) == SNEAK) {
                        handleAction(event.getPlayer(), event.getPlayer().getLocation().getBlock().getLocation());
                    } else {
                        player.sendTitle("§c§lFailed!", "§fYou failed to " + plugin.actions.get(player) + "!", 10, 70, 20);
                        player.damage(40);
                        plugin.actions.remove(player);
                        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 5, 0f);
                        evasiveManuever(player);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.actions.get(player) != null) {
            if (plugin.isInAction) {
                if (plugin.actions.get(player) == CLICK) {
                    handleAction(event.getPlayer(), event.getPlayer().getLocation().getBlock().getLocation());
                } else {
                    player.sendTitle("§c§lFailed!", "§fYou failed to " + plugin.actions.get(player) + "!", 10, 70, 20);
                    player.damage(40);
                    plugin.actions.remove(player);
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 5, 0f);
                    evasiveManuever(player);
                }
            }
        }
    }

    private void handleAction(Player player, Location blockLocation) {
        Main.Action action = plugin.actions.get(player);
        Block blockBelow = blockLocation.subtract(0, 1, 0).getBlock();
        Material blockMaterial = blockBelow.getType();

        if (blockMaterial == Material.GREEN_CONCRETE ||
                blockMaterial == Material.ORANGE_CONCRETE ||
                blockMaterial == Material.YELLOW_CONCRETE ||
                blockMaterial == Material.RED_CONCRETE) {
            if (action != null) {
                switch (action) {
                    case JUMP:
                        player.sendTitle("§c§lEvasive Maneuver", "§fPassed!", 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 0f);

                        if (blockMaterial == Material.YELLOW_CONCRETE){
                            player.damage(10);
                        }
                        if (blockMaterial == Material.ORANGE_CONCRETE){
                            player.damage(20);
                        }
                        if (blockMaterial == Material.RED_CONCRETE){
                            player.damage(30);
                        }

                        plugin.manueverCount++;
                        if (plugin.manueverCount != 4){
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                evasiveManuever(player);
                            }, 3 * 20L);
                        }else{
                            for (Entity entity : Bukkit.getWorld("worldAsmodeus").getEntities()){
                                if (entity instanceof LivingEntity) {
                                    if (entity.getName().equals("§c§lBelial")) {
                                        LivingEntity ent = (LivingEntity) entity;
                                        ent.setHealth(ent.getHealth()-2000);
                                    }
                                }
                            }
                            player.sendTitle("§c§lBelial", "§fWhat?!", 10, 70, 20);
                            player.sendMessage("§c§lBelial §f» How could you do that?!");
                            player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                            MythicMob azrael = MythicBukkit.inst().getMobManager().getMythicMob("Azrael").orElse(null);
                            Location abaddonLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -358, 20, 1317);
                            azrael.spawn(BukkitAdapter.adapt(abaddonLocation), 1);

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                player.sendMessage("§cNow kill Azrael on Hell Sculk!");
                            }, 2 * 20L);

                        }
                        break;
                    case SNEAK:
                        player.sendTitle("§c§lEvasive Maneuver", "§fPassed!", 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 0f);

                        if (blockMaterial == Material.YELLOW_CONCRETE){
                            player.damage(10);
                        }
                        if (blockMaterial == Material.ORANGE_CONCRETE){
                            player.damage(20);
                        }
                        if (blockMaterial == Material.RED_CONCRETE){
                            player.damage(30);
                        }

                        plugin.manueverCount++;
                        if (plugin.manueverCount != 4){
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                evasiveManuever(player);
                            }, 3 * 20L);
                        }else{
                            for (Entity entity : Bukkit.getWorld("worldAsmodeus").getEntities()){
                                if (entity instanceof LivingEntity) {
                                    if (entity.getName().equals("§c§lBelial")) {
                                        LivingEntity ent = (LivingEntity) entity;
                                        ent.setHealth(ent.getHealth()-2000);
                                    }
                                }
                            }
                            player.sendTitle("§c§lBelial", "§fWhat?!", 10, 70, 20);
                            player.sendMessage("§c§lBelial §f» How could you do that?!");
                            player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                            MythicMob azrael = MythicBukkit.inst().getMobManager().getMythicMob("Azrael").orElse(null);
                            Location abaddonLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -358, 20, 1317);
                            azrael.spawn(BukkitAdapter.adapt(abaddonLocation), 1);

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                player.sendMessage("§cNow kill Azrael on Hell Sculk!");
                            }, 2 * 20L);
                        }
                        break;
                    case CLICK:
                        player.sendTitle("§c§lEvasive Maneuver", "§fPassed!", 10, 70, 20);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 0f);

                        if (blockMaterial == Material.YELLOW_CONCRETE){
                            player.damage(10);
                        }
                        if (blockMaterial == Material.ORANGE_CONCRETE){
                            player.damage(20);
                        }
                        if (blockMaterial == Material.RED_CONCRETE){
                            player.damage(30);
                        }

                        plugin.manueverCount++;
                        if (plugin.manueverCount != 4){
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                evasiveManuever(player);
                            }, 3 * 20L);
                        }else{
                            for (Entity entity : Bukkit.getWorld("worldAsmodeus").getEntities()){
                                if (entity instanceof LivingEntity) {
                                    if (entity.getName().equals("§c§lBelial")) {
                                        LivingEntity ent = (LivingEntity) entity;
                                        ent.setHealth(ent.getHealth()-2000);
                                    }
                                }
                            }
                            player.sendTitle("§c§lBelial", "§fWhat?!", 10, 70, 20);
                            player.sendMessage("§c§lBelial §f» How could you do that?!");
                            player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                            MythicMob azrael = MythicBukkit.inst().getMobManager().getMythicMob("Azrael").orElse(null);
                            Location abaddonLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -358, 20, 1317);
                            azrael.spawn(BukkitAdapter.adapt(abaddonLocation), 1);

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                player.sendMessage("§cNow kill Azrael on Hell Sculk!");
                                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            }, 2 * 20L);
                        }
                        break;
                }
                plugin.actions.remove(player);
                plugin.isInAction = false;
            }

            // Regardless of action, you can restore the block
            restoreBlock(blockLocation);
        }
        else {
            player.sendTitle("§c§lFailed!", "§fYou're outside of the zone!", 10, 70, 20);
            player.damage(50);
        }
    }

    public void setDanceFloor(Player player) {
        originalBlocks.clear();
        plugin.actions.clear();
        Location playerLocation = player.getLocation().add(0, -1, 0);
        double radiusSquared = 25;

        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 15, 0);

        // Loop through a cube that encloses the sphere
        for (int xOffset = -5; xOffset <= 5; xOffset++) {
            for (int yOffset = -5; yOffset <= 5; yOffset++) {
                for (int zOffset = -5; zOffset <= 5; zOffset++) {
                    Location blockLocation = playerLocation.clone().add(xOffset, yOffset, zOffset);
                    double distanceSquared = playerLocation.distanceSquared(blockLocation);

                    // Check if the block is within the sphere
                    if (distanceSquared <= radiusSquared && blockLocation.getBlock().getType() != Material.AIR) {
                        Block block = blockLocation.getBlock();
                        Material originalMaterial = block.getType();
                        originalBlocks.put(blockLocation.clone(), originalMaterial);

                        // Determine block color based on distance from center
                        if (distanceSquared < 2) { // Inside the green area
                            block.setType(Material.GREEN_CONCRETE);
                        } else if (distanceSquared < 5) { // Between the green and orange area
                            block.setType(Material.ORANGE_CONCRETE);
                        } else if (distanceSquared < 6) { // Between the green and orange area
                            block.setType(Material.ORANGE_CONCRETE);
                        } else {
                            block.setType(Material.RED_CONCRETE);
                        }
                        Location particleLoc = blockLocation.clone();
                        blockLocation.getWorld().spawnParticle(Particle.FLAME, particleLoc.add(0.5, 1, 0.5), 10, 0.2, 0.2, 0.2, 0);

                        final Location finalBlockLocation = blockLocation.clone();
                        // Schedule a task to play countdown sound and change the block to red concrete after 2 seconds
                        new BukkitRunnable() {
                            int countdown = 3;
                            int soundPitch = 3;

                            @Override
                            public void run() {
                                if (countdown > 0) {
                                    countdown--;
                                } else {
                                    Block finalBlock = finalBlockLocation.getBlock();
                                    if (finalBlock.getType() == Material.GREEN_CONCRETE || finalBlock.getType() == Material.ORANGE_CONCRETE || finalBlock.getType() == Material.YELLOW_CONCRETE || finalBlock.getType() == Material.RED_CONCRETE) {
                                        if (distanceSquared < 2) { // Inside the green area
                                            block.setType(Material.GREEN_CONCRETE);
                                        } else if (distanceSquared < 5) { // Between the green and orange area
                                            block.setType(Material.ORANGE_CONCRETE);
                                        } else if (distanceSquared < 6) { // Between the green and orange area
                                            block.setType(Material.ORANGE_CONCRETE);
                                        } else {
                                            block.setType(Material.RED_CONCRETE);
                                        }
                                        // Randomly determine the action (jump, sneak, click)
                                        Main.Action action = getRandomAction();
                                        plugin.actions.put(player, action);
                                        plugin.isInAction = true;
                                        // Send title message to the player when the block turns red
                                        String titleMessage = getTitleMessageForAction(action);
                                        player.sendTitle("§c§lEvasive Maneuver", "§fDo a " + titleMessage, 10, 70, 20);
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                if (plugin.actions.get(player) == null) {
                                                    restoreBlock(finalBlockLocation);
                                                    plugin.actions.remove(player);
                                                    cancel();
                                                } else {
                                                    restoreBlock(finalBlockLocation);
                                                    player.sendTitle("§c§lEvasive Maneuver", "§fYou failed to " + plugin.actions.get(player) + "!", 10, 70, 20);
                                                    plugin.actions.remove(player);
                                                    evasiveManuever(player);
                                                    player.damage(40);
                                                    cancel();
                                                }
                                            }
                                        }.runTaskLater(plugin, 40); // 40 ticks = 2 seconds
                                    } else {
                                        restoreBlock(finalBlockLocation);
                                        cancel();
                                    }
                                    cancel(); // Cancel this task after countdown reaches 0
                                }
                                player.playSound(player, Sound.ENTITY_GUARDIAN_HURT, 0.5f, soundPitch);
                                soundPitch--;
                            }
                        }.runTaskTimer(plugin, 0, 20); // Run every tick (20 ticks = 1 second)
                    }
                }
            }
        }
    }



    private String getTitleMessageForAction(Main.Action action) {
        switch (action) {
            case JUMP:
                return "Jump!";
            case SNEAK:
                return "Sneak!";
            case CLICK:
                return "Click!";
            default:
                return "";
        }
    }

    private Main.Action getRandomAction() {
        int randomNumber = new Random().nextInt(3); // 0, 1, or 2
        return Main.Action.values()[randomNumber];
    }

    public void restoreBlock(Location location) {
        Material originalMaterial = originalBlocks.get(location);
        if (originalMaterial != null) {
            location.getBlock().setType(originalMaterial);
            originalBlocks.remove(location);
        }
    }

    public void restoreBlocks() {
        for (Location location : originalBlocks.keySet()) {
            Material originalMaterial = originalBlocks.get(location);
            if (originalMaterial != null) {
                location.getBlock().setType(originalMaterial);
            }
        }
        originalBlocks.clear(); // Clear the map after restoring all blocks
    }


    // Enum to represent the possible actions

}
