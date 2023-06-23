package me.TahaCheji.discordCommand;


import me.TahaCheji.Inv;
import me.TahaCheji.InventoryDataHandler;
import me.TahaCheji.objects.DatabaseInventoryData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class InventoryCommand extends ListenerAdapter implements Listener {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }
        if (e.getMessage().getContentRaw().contains("!Inv")) {
            String[] args = e.getMessage().getContentRaw().split(" ");
            if (args.length == 1) {
                e.getChannel().sendMessage("Error: !Inv [Player Name]").queue();
                return;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if (!player.hasPlayedBefore()) {
                e.getChannel().sendMessage("Error: That player does not exist").queue();
                return;
            }
            DatabaseInventoryData data = Inv.getInstance().getInvMysqlInterface().getData(player);
            try {
                List<String> items = new ArrayList<>();
                for (ItemStack itemStack : new InventoryDataHandler(Inv.getInstance()).decodeItems(data.getRawInventory())) {
                    if (itemStack == null) {
                        continue;
                    }
                    items.add(itemStack.getItemMeta().getDisplayName());
                    sendEmbed(e.getChannel(), player.getName() + " Inventory", items.toString(), "This is all logged in a data base");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void sendEmbed(MessageChannel channel, String title, String description, String footer) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription(description);
        embed.setFooter(footer);
        channel.sendMessage((CharSequence) embed.build()).queue();
    }

}
