import com.knemis.skyblock.skyblockcoreproject.secondcore.multiversion.common.src.main.java.com.iridium.iridiumcore.multiversion.SkyBlockProjectInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SkyBlockProjectInventoryDefault extends SkyBlockProjectInventory {
    @Override
    public Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }
}
