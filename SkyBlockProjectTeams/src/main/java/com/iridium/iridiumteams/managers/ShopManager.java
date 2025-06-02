package com.keviin.keviinteams.managers;

import com.keviin.keviincore.utils.InventoryUtils;
import com.keviin.keviincore.utils.Placeholder;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.bank.BankItem;
import com.keviin.keviinteams.configs.Shop;
import com.keviin.keviinteams.database.keviinUser;
import com.keviin.keviinteams.database.Team;
import com.keviin.keviinteams.database.TeamBank;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShopManager<T extends Team, U extends keviinUser<T>> {
    private final keviinTeams<T, U> keviinTeams;

    public ShopManager(keviinTeams<T, U> keviinTeams) {
        this.keviinTeams = keviinTeams;
    }

    public void buy(Player player, Shop.ShopItem shopItem, int amount) {
        if (!canPurchase(player, shopItem, amount)) {
            keviinTeams.getShop().failSound.play(player);
            return;
        }

        purchase(player, shopItem, amount);

        if (shopItem.command == null) {
            // Add item to the player Inventory
            if (!keviinTeams.getShop().dropItemWhenFull && !InventoryUtils.hasEmptySlot(player.getInventory())) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().inventoryFull
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
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

        keviinTeams.getShop().successSound.play(player);

        List<Placeholder> bankPlaceholders = keviinTeams.getBankItemList().stream()
                .map(BankItem::getName)
                .map(name -> new Placeholder(name + "_cost", formatPrice(getBankBalance(player, name))))
                .collect(Collectors.toList());
        double moneyCost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.money);

        player.sendMessage(StringUtils.color(StringUtils.processMultiplePlaceholders(keviinTeams.getMessages().successfullyBought
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%amount%", String.valueOf(amount))
                        .replace("%item%", StringUtils.color(shopItem.name))
                        .replace("%vault_cost%", formatPrice(moneyCost)),
                bankPlaceholders)
        ));
    }

    public void sell(Player player, Shop.ShopItem shopItem, int amount) {
        int inventoryAmount = InventoryUtils.getAmount(player.getInventory(), shopItem.type);
        if (inventoryAmount == 0) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().noSuchItem
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            keviinTeams.getShop().failSound.play(player);
            return;
        }
        int soldAmount = Math.min(inventoryAmount, amount);
        double moneyReward = calculateCost(soldAmount, shopItem.defaultAmount, shopItem.sellCost.money);

        InventoryUtils.removeAmount(player.getInventory(), shopItem.type, soldAmount);

        keviinTeams.getEconomy().depositPlayer(player, moneyReward);

        player.sendMessage(StringUtils.color(keviinTeams.getMessages().successfullySold
                .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                .replace("%amount%", String.valueOf(soldAmount))
                .replace("%item%", StringUtils.color(shopItem.name))
                .replace("%vault_reward%", String.valueOf(moneyReward))
        ));
        keviinTeams.getShop().successSound.play(player);
    }

    private double getBankBalance(Player player, String bankItem) {
        U user = keviinTeams.getUserManager().getUser(player);
        return keviinTeams.getTeamManager().getTeamViaID(user.getTeamID())
                .map(team -> keviinTeams.getTeamManager().getTeamBank(team, bankItem))
                .map(TeamBank::getNumber)
                .orElse(0.0);
    }

    private void setBankBalance(Player player, String bankItem, double amount) {
        U user = keviinTeams.getUserManager().getUser(player);
        Optional<T> team = keviinTeams.getTeamManager().getTeamViaID(user.getTeamID());
        if (!team.isPresent()) return;
        keviinTeams.getTeamManager().getTeamBank(team.get(), bankItem).setNumber(amount);
    }

    private boolean canPurchase(Player player, Shop.ShopItem shopItem, int amount) {

        if(shopItem.minLevel > 1) {
            U user = keviinTeams.getUserManager().getUser(player);
            Optional<T> team = keviinTeams.getTeamManager().getTeamViaID(user.getTeamID());

            if(!team.isPresent()) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().dontHaveTeam
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)));
                return false;
            }

            if(team.get().getLevel() < shopItem.minLevel) {
                player.sendMessage(StringUtils.color(keviinTeams.getMessages().notHighEnoughLevel
                        .replace("%prefix%", keviinTeams.getConfiguration().prefix)
                        .replace("%level%", String.valueOf(shopItem.minLevel))));
                return false;
            }
        }

        double moneyCost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.money);
        Economy economy = keviinTeams.getEconomy();
        for (String bankItem : shopItem.buyCost.bankItems.keySet()) {
            double cost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.bankItems.get(bankItem));
            if (getBankBalance(player, bankItem) < cost) return false;
        }

        if(!(moneyCost == 0 || economy != null && economy.getBalance(player) >= moneyCost)) {
            player.sendMessage(StringUtils.color(keviinTeams.getMessages().cannotAfford
                    .replace("%prefix%", keviinTeams.getConfiguration().prefix)
            ));
            return false;
        }

        return true;
    }

    private void purchase(Player player, Shop.ShopItem shopItem, int amount) {
        double moneyCost = calculateCost(amount, shopItem.defaultAmount, shopItem.buyCost.money);
        keviinTeams.getEconomy().withdrawPlayer(player, moneyCost);

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
        if (keviinTeams.getShop().abbreviatePrices) {
            return keviinTeams.getConfiguration().numberFormatter.format(value);
        }
        return String.valueOf(value);
    }
}
