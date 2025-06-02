package com.knemis.skyblock.skyblockcoreproject.teams.managers;

import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.InventoryUtils;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.Placeholder;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils.StringUtils;
import com.knemis.skyblock.skyblockcoreproject.teams.SkyBlockProjectTeams;
import com.knemis.skyblock.skyblockcoreproject.secondcore.utils;
import com.knemis.skyblock.skyblockcoreproject.teams.configs.Shop;
import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectTeamsUser;
import com.knemis.skyblock.skyblockcoreproject.teams.database.Team;
import com.knemis.skyblock.skyblockcoreproject.teams.database.TeamBank;
import com.knemis.skyblock.skyblockcoreproject.teams.bank.BankItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShopManager<T extends Team, U extends SkyBlockProjectTeamsUser<T>> {
    private final SkyBlockProjectTeams<T, U> SkyBlockProjectTeams;

    public ShopManager(SkyBlockProjectTeams<T, U> SkyBlockProjectTeams) {
        this.SkyBlockProjectTeams = SkyBlockProjectTeams;
    }

    public void buy(Player player, Shop.ShopItem shopItem, int amount) {
        if (!canPurchase(player, shopItem, amount)) {
            SkyBlockProjectTeams.getShop().failSound.play(player);
            return;
        }

        purchase(player, shopItem, amount);

        if (shopItem.command == null) {
            // Add item to the player Inventory
            if (!SkyBlockProjectTeams.getShop().dropItemWhenFull && !InventoryUtils.hasEmptySlot(player.getInventory())) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().inventoryFull
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                ));
                return;
            }

            ItemStack itemStack = shopItem.type.parseItem();
            itemStack.setAmount(amount);

            for (ItemStack dropItem : player.getInventory().addItem(itemStack).values()) {
                player.getWorld().dropItem(player.getEyeLocation(), dropItem);
            }
        } else {
            // Run the command
            String command = shopItem.command
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf(amount));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        SkyBlockProjectTeams.getShop().successSound.play(player);

        List<Placeholder> bankPlaceholders = SkyBlockProjectTeams.getBankItemList().stream()
                .map(BankItem::getName)
                .map(name -> new Placeholder(name + "_cost", formatPrice(getBankBalance(player, name))))
                .collect(Collectors.toList());
        double moneyCost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.money);

        player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(SkyBlockProjectTeams.getMessages().successfullyBought
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%item%", StringUtils.color(shopItem.name))
                        .replace("%vault_cost%", formatPrice(moneyCost)),
                bankPlaceholders)
        ));
    }

    public void sell(Player player, Shop.ShopItem shopItem, int amount) {
        int inventoryAmount = InventoryUtils.getAmount(player.getInventory(), shopItem.type);
        if (inventoryAmount == 0) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().noSuchItem
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            SkyBlockProjectTeams.getShop().failSound.play(player);
            return;
        }
        int soldAmount = Math.min(inventoryAmount, amount);
        double moneyReward = calculateCost(soldAmount, shopItem.defaultAmount, shopItem.sellCost.money);

        InventoryUtils.removeAmount(player.getInventory(), shopItem.type, soldAmount);

        SkyBlockProjectTeams.getEconomy().depositPlayer(player, moneyReward);

        player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().successfullySold
                .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                .replace("%amount%", String.valueOf(soldAmount))
                .replace("%item%", StringUtils.color(shopItem.name))
                .replace("%vault_reward%", String.valueOf(moneyReward))
        ));
        SkyBlockProjectTeams.getShop().successSound.play(player);
    }

    private double getBankBalance(Player player, String bankItem) {
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        return SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID())
                .map(team -> SkyBlockProjectTeams.getTeamManager().getTeamBank(team, bankItem))
                .map(TeamBank::getNumber)
                .orElse(0.0);
    }

    private void setBankBalance(Player player, String bankItem, double amount) {
        U user = SkyBlockProjectTeams.getUserManager().getUser(player);
        Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());
        if (!team.isPresent()) return;
        SkyBlockProjectTeams.getTeamManager().getTeamBank(team.get(), bankItem).setNumber(amount);
    }

    private boolean canPurchase(Player player, Shop.ShopItem shopItem, int amount) {

        if(shopItem.minLevel > 1) {
            U user = SkyBlockProjectTeams.getUserManager().getUser(player);
            Optional<T> team = SkyBlockProjectTeams.getTeamManager().getTeamViaID(user.getTeamID());

            if(!team.isPresent()) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().dontHaveTeam
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)));
                return false;
            }

            if(team.get().getLevel() < shopItem.minLevel) {
                player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().notHighEnoughLevel
                        .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
                        .replace("%level%", String.valueOf(shopItem.minLevel))));
                return false;
            }
        }

        double moneyCost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.money);
        Economy economy = SkyBlockProjectTeams.getEconomy();
        for (String bankItem : shopItem.buyCost.bankItems.keySet()) {
            double cost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.bankItems.get(bankItem));
            if (getBankBalance(player, bankItem) < cost) return false;
        }

        if(!(moneyCost == 0 || economy != null && economy.getBalance(player) >= moneyCost)) {
            player.sendMessage(StringUtils.color(SkyBlockProjectTeams.getMessages().cannotAfford
                    .replace("%prefix%", SkyBlockProjectTeams.getConfiguration().prefix)
            ));
            return false;
        }

        return true;
    }

    private void purchase(Player player, Shop.ShopItem shopItem, int amount) {
        double moneyCost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.money);
        SkyBlockProjectTeams.getEconomy().withdrawPlayer(player, moneyCost);

        for (String bankItem : shopItem.buyCost.bankItems.keySet()) {
            double cost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.bankItems.get(bankItem));
            setBankBalance(player, bankItem, getBankBalance(player, bankItem) - cost);
        }
    }

    private double calculateCost(int amount, int defaultAmount, double defaultPrice) {
        double costPerItem = defaultPrice / defaultAmount;
        return round(costPerItem * amount, 2);
    }

    private double round(double value, int places) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

    public String formatPrice(double value) {
        if (SkyBlockProjectTeams.getShop().abbreviatePrices) {
            return SkyBlockProjectTeams.getConfiguration().numberFormatter.format(value);
        }
        return String.valueOf(value);
    }
}
