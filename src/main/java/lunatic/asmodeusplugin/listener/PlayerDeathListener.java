package lunatic.asmodeusplugin.listener;

import lunatic.asmodeusplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayerDeathListener implements Listener {
    private Main plugin;

    public PlayerDeathListener(Main plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity().getPlayer();
        if (plugin.asmodeusPlayer != null) {
            if (plugin.asmodeusPlayer.equals(player.getName())) {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§e" + player.getName() + " §fgagal melawan §c§lBelial§f!");
                Bukkit.broadcastMessage("");
                plugin.clearVariables();
                plugin.processQueue();
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (plugin.asmodeusPlayer != null) {
            if (plugin.asmodeusPlayer.equals(player.getName())) {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§e" + player.getName() + " §fgagal melawan §c§lBelial§f!");
                Bukkit.broadcastMessage("");
                plugin.clearVariables();
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                plugin.processQueue();
            }
        }
        if (Arrays.asList(plugin.joinQueue).contains(player)) {
            plugin.removeFromQueue(player.getName());
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§e" + player.getName() + " §fdisconnect dari antrian §c§lBelial Realm§f!");
            Bukkit.broadcastMessage("§aQueued Players: " + Arrays.stream(plugin.joinQueue)
                    .filter(Objects::nonNull)
                    .map(Player::getName)
                    .collect(Collectors.joining(", ")));
            Bukkit.broadcastMessage("");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("worldAsmodeus")) {
            if (plugin.asmodeusPlayer != null) {
                if (plugin.asmodeusPlayer.equals(player.getName())) {
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§e" + player.getName() + " §fgagal melawan §c§lBelial§f!");
                    Bukkit.broadcastMessage("");
                    plugin.clearVariables();
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    plugin.processQueue();
                }
            }
        }
    }
}
