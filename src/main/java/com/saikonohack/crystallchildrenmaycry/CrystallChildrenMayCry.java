package com.saikonohack.crystallchildrenmaycry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class CrystallChildrenMayCry extends JavaPlugin implements Listener {

    private double damageMultiplier;
    private final Set<Material> bedMaterials = EnumSet.of(
            Material.WHITE_BED, Material.ORANGE_BED, Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED, Material.YELLOW_BED, Material.LIME_BED,
            Material.PINK_BED, Material.GRAY_BED, Material.LIGHT_GRAY_BED,
            Material.CYAN_BED, Material.PURPLE_BED, Material.BLUE_BED,
            Material.BROWN_BED, Material.GREEN_BED, Material.RED_BED, Material.BLACK_BED
    );

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        damageMultiplier = config.getDouble("damage-multiplier", 1.0);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.END_CRYSTAL) {
            event.setDamage(event.getDamage() / damageMultiplier);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            if (event.getEntity() instanceof Player player) {
                if (player.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER ||
                    player.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) {
                    event.setDamage(event.getDamage() / damageMultiplier);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkHotbar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        checkHotbar(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkHotbar(player);
                }
            }.runTaskLater(this, 1);
        }
    }

    private void checkHotbar(Player player) {
        if (player.hasPermission("CrystallChildrenMayCry.bypass")) {
            for (int i = 0; i < 9; i++) {
                Material item = player.getInventory().getItem(i) != null ? Objects.requireNonNull(player.getInventory().getItem(i)).getType() : null;

                if (item == Material.END_CRYSTAL || (item == Material.RESPAWN_ANCHOR && player.getWorld().getEnvironment() != org.bukkit.World.Environment.NETHER) || (bedMaterials.contains(item) && player.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END)) {
                    player.getInventory().setItem(i, null);
                    player.sendMessage("Вы не можете носить этот предмет в хотбаре!");
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("grantBypass")) {
            if (sender instanceof ConsoleCommandSender || sender.hasPermission("CrystallChildrenMayCry.grantBypass")) {
                if (args.length != 1) {
                    sender.sendMessage("Usage: /grantBypass <player>");
                    return false;
                }

                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("Player not found.");
                    return false;
                }

                PermissionAttachment attachment = target.addAttachment(this);
                attachment.setPermission("CrystallChildrenMayCry.bypass", true);
                sender.sendMessage("Granted CrystallChildrenMayCry.bypass to " + target.getName());
                target.sendMessage("You have been granted the CrystallChildrenMayCry.bypass permission.");

                return true;
            } else {
                sender.sendMessage("You do not have permission to use this command.");
                return false;
            }
        }
        return false;
    }
}
