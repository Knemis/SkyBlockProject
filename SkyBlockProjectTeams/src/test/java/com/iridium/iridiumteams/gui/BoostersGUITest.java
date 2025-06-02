package com.keviin.keviinteams.gui;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.google.common.collect.ImmutableMap;
import com.keviin.keviincore.utils.StringUtils;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.keviinteams.UserBuilder;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoostersGUITest {

    private final Map<Integer, Material> inventoryLayout = new ImmutableMap.Builder<Integer, Material>()
            .put(9, Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .put(10, Material.WHEAT)
            .put(11, Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .put(12, Material.SPAWNER)
            .put(13, Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .put(14, Material.EXPERIENCE_BOTTLE)
            .put(15, Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .put(16, Material.FEATHER)
            .put(17, Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .build();

    private ServerMock serverMock;

    @BeforeEach
    public void setup() {
        this.serverMock = MockBukkit.mock();
        MockBukkit.load(TestPlugin.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void BoostersGUI__HasItems() {
        TestTeam testTeam = new TeamBuilder().build();
        BoostersGUI<TestTeam, User> boostersGUI = new BoostersGUI<>(testTeam, null, TestPlugin.getInstance());
        Inventory inventory = boostersGUI.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            assertEquals(inventoryLayout.getOrDefault(i, Material.BLACK_STAINED_GLASS_PANE), inventory.getContents()[i].getType(),"Item on slot " + i + " not as expected");
        }
    }

    @Test
    public void BoostersGUI__Click() {
        TestTeam testTeam = new TeamBuilder().withExperience(125).build();
        PlayerMock playerMock = new UserBuilder(serverMock).withTeam(testTeam).build();
        TestPlugin.getInstance().getEconomy().depositPlayer(playerMock, 10000);

        BoostersGUI<TestTeam, User> boostersGUI = new BoostersGUI<>(testTeam, null, TestPlugin.getInstance());
        playerMock.openInventory(boostersGUI.getInventory());
        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(playerMock.getOpenInventory(), ClickType.LEFT, 16);

        assertTrue(inventoryClickEvent.isCancelled());
        playerMock.assertSaid(StringUtils.color(TestPlugin.getInstance().getMessages().purchasedBooster
                .replace("%prefix%", TestPlugin.getInstance().getConfiguration().prefix)
                .replace("%booster%", "flight")
        ));
        playerMock.assertNoMoreSaid();
    }
}