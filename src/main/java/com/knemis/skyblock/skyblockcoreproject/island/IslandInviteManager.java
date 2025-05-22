package com.knemis.skyblock.skyblockcoreproject.island;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class IslandInviteManager {

    private final HashMap<UUID, UUID> invites = new HashMap<>();

    public void invitePlayer(Player owner, Player target) {
        invites.put(target.getUniqueId(), owner.getUniqueId());
    }

    public boolean hasInvite(Player player) {
        return invites.containsKey(player.getUniqueId());
    }

    public UUID getInviter(Player player) {
        return invites.get(player.getUniqueId());
    }

    public void removeInvite(Player player) {
        invites.remove(player.getUniqueId());
    }
}
