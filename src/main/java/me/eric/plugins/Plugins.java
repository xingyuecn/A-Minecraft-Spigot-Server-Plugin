package me.eric.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Plugins extends JavaPlugin implements Listener, CommandExecutor {

    private Permission announcePermission;
    private Set<UUID> craftedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Plugin enabled!");

        getServer().getPluginManager().registerEvents(this, this);

        announcePermission = new Permission("plugins.announce");
        getServer().getPluginManager().addPermission(announcePermission);

        ItemStack enchantedGoldenApple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ShapedRecipe enchantedGoldenAppleRecipe = new ShapedRecipe(enchantedGoldenApple);
        enchantedGoldenAppleRecipe.shape("GGG", "GAG", "GGG");
        enchantedGoldenAppleRecipe.setIngredient('G', Material.GOLD_BLOCK);
        enchantedGoldenAppleRecipe.setIngredient('A', Material.APPLE);
        Bukkit.addRecipe(enchantedGoldenAppleRecipe);

        ItemStack diamondHorseArmor = new ItemStack(Material.DIAMOND_HORSE_ARMOR);
        ShapedRecipe diamondHorseArmorRecipe = new ShapedRecipe(diamondHorseArmor);
        diamondHorseArmorRecipe.shape("DDD", "DID", "DDD");
        diamondHorseArmorRecipe.setIngredient('D', Material.DIAMOND);
        diamondHorseArmorRecipe.setIngredient('I', Material.IRON_INGOT);
        Bukkit.addRecipe(diamondHorseArmorRecipe);

        // 注册/heal命令
        getCommand("heal").setExecutor(this);
        // 注册/gui命令
        getCommand("gui").setExecutor(this);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("Plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("announce")) {
            if (!sender.hasPermission(announcePermission)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /announce <message>");
                return true;
            }

            String message = ChatColor.GOLD + "[Announcement] " + ChatColor.WHITE + String.join(" ", args);
            sendAnnouncement(message);
            sender.sendMessage(ChatColor.GREEN + "Announcement sent!");
            return true;
        }

        // 处理/heal命令
        if (command.getName().equalsIgnoreCase("heal")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // 检查玩家血量是否高于6
                if (player.getHealth() > 6) {
                    player.sendMessage(ChatColor.RED + "你的血量已经很高了，无需使用/heal命令！");
                    return true;
                }

                player.setHealth(20);
                player.sendMessage(ChatColor.GREEN + "你的血量已回满！");
            }
            return true;
        }

        // 处理/gui命令
        if (command.getName().equalsIgnoreCase("gui")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                openGUI(player);
            }
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String message = ChatColor.GREEN + "Hello " + player.getName();
        Bukkit.broadcastMessage(message);
        player.giveExp(1); // 给玩家1级经验
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String message = ChatColor.YELLOW + player.getName() + " temporarily left the server";
        Bukkit.broadcastMessage(message);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            Player player = (Player) event.getWhoClicked();
            UUID playerUUID = player.getUniqueId();

            if (!craftedPlayers.contains(playerUUID)) {
                // 确保玩家是第一次合成附魔金苹果
                craftedPlayers.add(playerUUID);

                // 触发成就
                player.sendMessage(ChatColor.YELLOW + "恭喜" + player.getName() + "获得了成就“君临天下”！");
                player.giveExp(1);
            }
        }
    }

    private void sendAnnouncement(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    private void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 6 * 9, "奖励箱");
        ItemStack pickaxe = new ItemStack(Material.STONE_PICKAXE);
        ItemStack axe = new ItemStack(Material.STONE_AXE);
        ItemStack GA = new ItemStack(Material.GOLDEN_APPLE);

        // 设置物品的Lore
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.setLore(Collections.singletonList("Pickaxe Lore"));
        pickaxe.setItemMeta(pickaxeMeta);

        ItemMeta axeMeta = axe.getItemMeta();
        axeMeta.setLore(Collections.singletonList("Axe Lore"));
        axe.setItemMeta(axeMeta);

        ItemMeta GAMeta = GA.getItemMeta();
        GAMeta.setLore(Collections.singletonList("Golden Apple Lore"));
        GA.setItemMeta(GAMeta);

        gui.setItem(0, pickaxe);
        gui.setItem(1, axe);
        gui.setItem(2, GA);

        // 检查玩家背包是否已经包含了GUI中的物品
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta.hasLore()) {
                    List<String> lore = itemMeta.getLore();
                    if (lore.contains("Pickaxe Lore")) {
                        gui.removeItem(pickaxe);
                    } else if (lore.contains("Axe Lore")) {
                        gui.removeItem(axe);
                    } else if (lore.contains("Golden Apple Lore")) {
                        gui.removeItem(GA);
                    }
                }
            }
        }

        player.openInventory(gui);
    }
}