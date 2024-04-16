    package lunatic.asmodeusplugin.listener;

    import io.lumine.mythic.api.mobs.MythicMob;
    import io.lumine.mythic.bukkit.BukkitAdapter;
    import io.lumine.mythic.bukkit.MythicBukkit;
    import lunatic.asmodeusplugin.Main;
    import lunatic.asmodeusplugin.cooldown.CooldownManager;
    import org.bukkit.Bukkit;
    import org.bukkit.Location;
    import org.bukkit.Material;
    import org.bukkit.Sound;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.Listener;
    import org.bukkit.event.block.Action;
    import org.bukkit.event.player.PlayerInteractEvent;
    import org.bukkit.inventory.EquipmentSlot;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;
    import org.bukkit.potion.PotionEffect;
    import org.bukkit.potion.PotionEffectType;

    import java.sql.SQLException;
    import java.util.Arrays;
    import java.util.Objects;
    import java.util.stream.Collectors;

    public class JoinListener implements Listener {
        private Main plugin;
        private CooldownManager cooldownManager;
        int clickCount;

        public JoinListener(Main plugin){
            this.plugin = plugin;
            this.cooldownManager = new CooldownManager();
        }

        @EventHandler
        public void joinAsmodeus(PlayerInteractEvent event) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if (event.getHand().equals(EquipmentSlot.HAND)) {
                    Player player = event.getPlayer();
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (player.getWorld().getName().equals("spawn-mix")) {
                        if (itemInHand.getType() == Material.NAME_TAG) {
                            ItemMeta meta = itemInHand.getItemMeta();
                            clickCount++;
                            if (clickCount >= 5){
                                player.setRotation((float) (player.getLocation().getYaw() + 180), player.getLocation().getPitch());
                            }
                            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§c§lBelial Ticket")) {
                                if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHISELED_STONE_BRICKS && event.getClickedBlock().getLocation().getY() == 87) {
                                    if (plugin.asmodeusStarted) {
                                        if (!cooldownManager.isOnCooldown(player.getName(), "asmodeus")) {
                                            if (!cooldownManager.isOnCooldown(player.getAddress().getAddress().getHostAddress(), player.getAddress().getAddress().getHostAddress())) {
                                                if (plugin.queueIndex < 5) {
                                                    cooldownManager.setCooldown(player.getAddress().getAddress().getHostAddress(), player.getAddress().getAddress().getHostAddress(), 2400);
                                                    if (Arrays.asList(plugin.joinQueue).contains(player)) {
                                                        player.sendMessage("§cYou are already in the queue.");
                                                        return;
                                                    }
                                                    int position = plugin.queueIndex + 1; // Calculate position in queue
                                                    plugin.joinQueue[plugin.queueIndex] = player;
                                                    plugin.queueIndex++;
                                                    Bukkit.broadcastMessage("");
                                                    Bukkit.broadcastMessage("§e" + player.getName() + " §fmemasuki antrian §c§lBelial Realm§f! §7(" + position + "/5)");
                                                    Bukkit.broadcastMessage("§aQueued Players: " + Arrays.stream(plugin.joinQueue).filter(Objects::nonNull).map(Player::getName).collect(Collectors.joining(", ")));
                                                    Bukkit.broadcastMessage("");
                                                    player.sendMessage("§aYou have joined the queue as position " + position + ". Please wait for your turn.");
                                                    plugin.processQueue();
                                                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                                                } else {
                                                    player.sendMessage("§cThe queue is full. Please try again later.");
                                                }
                                            }else{
                                                player.sendMessage("§cYour IP still on cooldown!");
                                            }
                                        } else {
                                            cooldownManager.sendCooldownMessage(player, "asmodeus");
                                        }
                                    } else {
                                        if (!cooldownManager.isOnCooldown(player.getName(), "asmodeus")) {
                                            if (!cooldownManager.isOnCooldown(player.getAddress().getAddress().getHostAddress(), player.getAddress().getAddress().getHostAddress())) {
                                                boolean isJoinQueueEmpty = true;
                                                for (Object obj : plugin.joinQueue) {
                                                    if (obj != null) {
                                                        isJoinQueueEmpty = false;
                                                        break;
                                                    }
                                                }

                                                if (isJoinQueueEmpty) {
                                                    startAsmodeusEvent(player);
                                                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                                                    cooldownManager.setCooldown(player.getAddress().getAddress().getHostAddress(), player.getAddress().getAddress().getHostAddress(), 2400);
                                                }
                                            }
                                            else{
                                                player.sendMessage("§cYour IP still on cooldown!");
                                            }

                                        } else {
                                            cooldownManager.sendCooldownMessage(player, "asmodeus");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }



        // Method to start the Asmodeus event for a player
        public void startAsmodeusEvent(Player player) {
            if (!plugin.asmodeusStarted) {
                if (player.isOnline()) {
                    plugin.asmodeusPlayer = player.getName();
                    plugin.asmodeusStarted = true;
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§e" + player.getName() + " §fmemasuki §c§lBelial Realm§f!");
                    Bukkit.broadcastMessage("");
                    cooldownManager.setCooldown(player.getName(), player.getAddress().getAddress().getHostAddress(), 2400);
                    cooldownManager.setCooldown(player.getName(), "asmodeus", 2400);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_WOLF_HOWL, 0.2f, 1f);
                    }

                    if (!plugin.playerResistance.containsKey(player.getName())) {
                        try {
                            int damageResistance = plugin.database.getDamageResistance(player.getUniqueId(), player.getName());
                            plugin.playerResistance.putIfAbsent(player.getName(), damageResistance);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        int damageResistance = 0;
                        try {
                            damageResistance = plugin.database.getDamageResistance(player.getUniqueId(), player.getName());
                            plugin.playerResistance.putIfAbsent(player.getName(), damageResistance);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Check and set damage if not present
                    if (!plugin.playerDamage.containsKey(player.getName())) {
                        try {
                            int damageBooster = plugin.database.getDamageBooster(player.getUniqueId(), player.getName());
                            plugin.playerDamage.putIfAbsent(player.getName(), damageBooster);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        int damageBooster = 0;
                        try {
                            damageBooster = plugin.database.getDamageBooster(player.getUniqueId(), player.getName());
                            plugin.playerDamage.putIfAbsent(player.getName(), damageBooster);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                        onlinePlayers.playSound(onlinePlayers.getLocation(), Sound.ENTITY_ALLAY_HURT, 1f, 0f);
                    }

                    Location teleportLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -278, 32, 1473, -180, 0);
                    player.teleport(teleportLocation);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (plugin.asmodeusPlayer != null) {
                            player.sendMessage("§cBelial §fwill spawn in 10 seconds!");
                            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 255555555, 255));
                        }
                    }, 2 * 20L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (plugin.asmodeusPlayer != null) {
                            MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("Belial").orElse(null);
                            Location spawnLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -302, 13, 1362);
                            mob.spawn(BukkitAdapter.adapt(spawnLocation), 1);

                            MythicMob abaddon = MythicBukkit.inst().getMobManager().getMythicMob("Abaddon").orElse(null);
                            Location abaddonLocation = new Location(Bukkit.getWorld("worldAsmodeus"), -334, 25, 1440);
                            abaddon.spawn(BukkitAdapter.adapt(abaddonLocation), 1);
                        }
                    }, 12 * 20L);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (plugin.asmodeusPlayer != null) {
                            player.sendMessage("§cYou must kill Abaddon on the Hell Pillar first!");
                        }
                    }, 14 * 20L);
//                    } else {
//                        Bukkit.broadcastMessage("");
//                        Bukkit.broadcastMessage("§e" + player.getName() + " §fdisconnect dari antrian §c§lBelial Realm§f!");
//                        Bukkit.broadcastMessage("§aQueued Players: " + Arrays.stream(plugin.joinQueue)
//                                .filter(Objects::nonNull)
//                                .map(Player::getName)
//                                .collect(Collectors.joining(", ")));
//                        Bukkit.broadcastMessage("");
//                        plugin.removeFromQueue(player.getName());
//                        plugin.processQueue();
//                    }
                }
            }
        }
    }
