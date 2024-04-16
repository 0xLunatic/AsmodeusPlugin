package lunatic.asmodeusplugin.listener;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import lunatic.asmodeusplugin.Main;
import lunatic.asmodeusplugin.skills.EvasiveManeuver;
import lunatic.asmodeusplugin.skills.MonsterGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.sql.SQLException;

public class BelialListener implements Listener {
    private Main plugin;

    public BelialListener(Main plugin) {
        this.plugin = plugin;
    }

    // @EventHandler
    public void onPlayerJoinSetStats(PlayerJoinEvent event){
        Player player = event.getPlayer();

        // Check and set resistance if not present
        int damageResistance = 0;
        if (!plugin.playerResistance.containsKey(player.getName())) {
            try {
                damageResistance = plugin.database.getDamageResistance(player.getUniqueId(), player.getName());
                plugin.playerResistance.putIfAbsent(player.getName(), damageResistance);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        // Check and set damage if not present
        int damageBooster = 0;
        if (!plugin.playerDamage.containsKey(player.getName())) {
            try {
                damageBooster = plugin.database.getDamageBooster(player.getUniqueId(), player.getName());
                plugin.playerDamage.putIfAbsent(player.getName(), damageBooster);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @EventHandler
    public void onDamageMagmaCube(EntityDamageByEntityEvent event) {
        if (event.getEntityType() == EntityType.MAGMA_CUBE) {
            if (event.getEntity().getName().equals("§c§lBelial")) {
                if (event.getDamager() instanceof Player) {
                    if (!plugin.isAstarothDeath) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        if (event.getDamager() instanceof Player){
            Player player = (Player) event.getDamager();
            if (player.getWorld().getName().equals("worldAsmodeus")){
                double damageBooster = plugin.playerDamage.getOrDefault(player.getName(), 1); // Default value is 1 if player not found

                if (damageBooster >= 1) {
                    double scaledDamage = event.getDamage() * (damageBooster / 100 + 1); // Multiply by 0.1 for scaling down
                    event.setDamage(scaledDamage);
                }
            }
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getWorld().getName().equals("worldAsmodeus")) {
                double damageResistance = plugin.playerResistance.getOrDefault(player.getName(), 0); // Default value is 0 if player not found

                if (damageResistance >= 1) {
                    double finalDamage = event.getDamage() - (event.getDamage() * (damageResistance / 100 + 1)); // Apply resistance

                    event.setDamage(finalDamage);
                }
            }
        }
    }


    @EventHandler
    public void onAbaddonDeath(EntityDeathEvent event) {
        if (event.getEntity().getName().equals("§c§lAbaddon")) {
            Bukkit.broadcastMessage("§e" + plugin.asmodeusPlayer + " §fbaru saja membunuh " + "§c§lAbaddon§f!");
            plugin.isAbaddonDeath = true;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0f);
            }

            Player player = Bukkit.getPlayer(plugin.asmodeusPlayer);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle("§c§lBelial", "§fHow dare you do that!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }, 2 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Location teleportLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -302, 13, 1390, -180, 0);
                player.teleport(teleportLocation);
                player.sendTitle("§c§lBelial", "§fGet Ready Now!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }, 4 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                EvasiveManeuver evasiveManeuver = new EvasiveManeuver(plugin);
                evasiveManeuver.evasiveManuever(player);
            }, 8 * 20L);

        }
    }

    @EventHandler
    public void onAzrealDeath(EntityDeathEvent event) {
        if (event.getEntity().getName().equals("§c§lAzrael")) {
            Bukkit.broadcastMessage("§e" + plugin.asmodeusPlayer + " §fbaru saja membunuh " + "§c§lAzrael§f!");
            plugin.isAzraelDeath = true;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0f);
            }

            Player player = Bukkit.getPlayer(plugin.asmodeusPlayer);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle("§c§lBelial", "§fHUH!!!!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }, 2 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle("§c§lBelial", "§f!!!!");
                player.sendMessage("§c§lBelial §f» Enough for now!");
                player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            }, 4 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                MonsterGenerator monsterGenerator = new MonsterGenerator(plugin);
                monsterGenerator.spawnGenerator(event.getEntity().getLocation());
                player.sendMessage("§c§lAzrael §f» Changing myself to Monster Generator!");
                player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            }, 8 * 20L);

        }
    }

    @EventHandler
    public void onCerberusDeath(EntityDeathEvent event) {
        if (event.getEntity().getName().equals("§c§lCerberus")) {
            Bukkit.broadcastMessage("§e" + plugin.asmodeusPlayer + " §fbaru saja membunuh " + "§c§lCerberus§f!");
            plugin.isCerberusDeath = true;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0f);
            }

            Player player = Bukkit.getPlayer(plugin.asmodeusPlayer);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle("§c§lBelial", "§f?!?!?!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }, 2 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§c§lBelial §f» NOOOO MY CERBERUS!!");
                player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
            }, 4 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§c§lBelial §f» ARGGGGHHHH!!");
                player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0f);
                for (Entity entity : Bukkit.getWorld("worldAsmodeus").getEntities()) {
                    if (entity instanceof LivingEntity) {
                        if (entity.getName().equals("§c§lBelial")) {
                            LivingEntity ent = (LivingEntity) entity;
                            ent.setHealth(ent.getHealth() - 2000);
                        }
                    }
                }
                MythicMob astaroth = MythicBukkit.inst().getMobManager().getMythicMob("Astaroth").orElse(null);
                Location astarothLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -209, 27, 1390);
                astaroth.spawn(BukkitAdapter.adapt(astarothLocation), 1);
            }, 8 * 20L);

        }

    }
    @EventHandler
    public void onAstarothDeath(EntityDeathEvent event) {
        if (event.getEntity().getName().equals("§c§lAstaroth")) {
            Bukkit.broadcastMessage("§e" + plugin.asmodeusPlayer + " §fbaru saja membunuh " + "§c§lAstaroth§f!");
            plugin.isAstarothDeath = true;
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 1f, 0f);
            }

            Player player = Bukkit.getPlayer(plugin.asmodeusPlayer);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle("§c§lBelial", "§fFeel Dizzy!");
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }, 2 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§c§lBelial §f» You!! Are!!! AAGHHH!!!!");
                player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                Location teleportLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -302, 13, 1390, -180, 0);
                player.teleport(teleportLocation);
            }, 4 * 20L);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Entity entity : Bukkit.getWorld("worldAsmodeus").getEntities()) {
                    if (entity instanceof MagmaCube) {
                        MagmaCube magmaCube = (MagmaCube) entity;
                        if (magmaCube.getCustomName() != null && magmaCube.getCustomName().equals("§c§lBelial")) {
                            // Initialize size variables
                            final int[] currentSize = {40};
                            int targetSize = 6;
                            int sizeStep = 1; // Adjust this to change the speed of size reduction

                            // Schedule a repeating task to gradually decrease the size
                            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                                if (currentSize[0] > targetSize) {
                                    magmaCube.setSize(currentSize[0]);
                                    currentSize[0] -= sizeStep;
                                    magmaCube.setMaxHealth(2000);
                                    magmaCube.setHealth(magmaCube.getMaxHealth());
                                    magmaCube.getWorld().playSound(magmaCube.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 100f, 0f);
                                } else {
                                    // Set final size and trigger other effects
                                    magmaCube.setSize(targetSize);
                                    magmaCube.setMaxHealth(2000);
                                    magmaCube.setHealth(magmaCube.getMaxHealth());
                                    player.sendMessage("§c§lBelial §f» MY BODYYYY!!");
                                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0f);
                                    // Cancel the task once the size reaches the target
                                    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                                        for (Entity nearbyPlayer : magmaCube.getNearbyEntities(10, 10, 10)) {
                                            if (nearbyPlayer instanceof Player) {
                                                ((Creature) magmaCube).setTarget((LivingEntity) nearbyPlayer);
                                            }
                                        }
                                    }, 0L, 20L);

                                    Bukkit.getScheduler().cancelTasks(plugin);
                                }
                            }, 0L, 10L); // Adjust the delay (0L) and period (10L) as needed
                        }
                    }
                }
            }, 8 * 20L);

        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld().getName().equals("worldAsmodeus")) {
            Location loc = player.getLocation();
            // Check if the player's feet are touching lava
            if (loc.getBlock().getType() == Material.LAVA) {
                // Create a vector to push the player upwards
                Vector up = new Vector(0, 10, 0);
                player.setVelocity(up);
            }
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        try {
            plugin.database.registerDataPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
