package com.keviin.keviinteams.gui;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.google.common.collect.ImmutableMap;
import com.keviin.keviinteams.SettingType;
import com.keviin.keviinteams.TeamBuilder;
import com.keviin.keviinteams.UserBuilder;
import com.keviin.keviinteams.sorting.ExperienceTeamSort;
import com.keviin.keviinteams.sorting.ValueTeamSort;
import com.keviin.testplugin.TestPlugin;
import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopGUITest {
    private final Map<Integer, Material> inventoryLayout = new ImmutableMap.Builder<Integer, Material>()
            .put(14, Material.PLAYER_HEAD)
            .put(22, Material.BARRIER)
            .put(23, Material.BARRIER)
            .put(24, Material.BARRIER)
            .put(30, Material.BARRIER)
            .put(31, Material.BARRIER)
            .put(32, Material.BARRIER)
            .put(33, Material.BARRIER)
            .put(34, Material.BARRIER)
            .put(18, Material.DIAMOND)
            .put(27, Material.EXPERIENCE_BOTTLE)
            .put(51, Material.LIME_STAINED_GLASS_PANE)
            .put(47, Material.RED_STAINED_GLASS_PANE)
            .build();
    private final Map<Integer, Material> noPlayersInventoryLayout = new ImmutableMap.Builder<Integer, Material>()
            .put(14, Material.BARRIER)
            .put(22, Material.BARRIER)
            .put(23, Material.BARRIER)
            .put(24, Material.BARRIER)
            .put(30, Material.BARRIER)
            .put(31, Material.BARRIER)
            .put(32, Material.BARRIER)
            .put(33, Material.BARRIER)
            .put(34, Material.BARRIER)
            .put(18, Material.DIAMOND)
            .put(27, Material.EXPERIENCE_BOTTLE)
            .put(51, Material.LIME_STAINED_GLASS_PANE)
            .put(47, Material.RED_STAINED_GLASS_PANE)
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
    public void TopGUI__HasItems() {
        new TeamBuilder().build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().valueTeamSort, null, TestPlugin.getInstance());
        Inventory inventory = topGUI.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            assertEquals(inventoryLayout.getOrDefault(i, Material.BLACK_STAINED_GLASS_PANE), inventory.getContents()[i].getType(), "Item on slot " + i + " not as expected");
        }
    }

    @Test
    public void TopGUI__HidesPrivateValues() {
        new TeamBuilder().withSetting(SettingType.VALUE_VISIBILITY, "Private").build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().valueTeamSort, null, TestPlugin.getInstance());
        Inventory inventory = topGUI.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            assertEquals(noPlayersInventoryLayout.getOrDefault(i, Material.BLACK_STAINED_GLASS_PANE), inventory.getContents()[i].getType(), "Item on slot " + i + " not as expected");
        }
    }

    @Test
    public void TopGUI__Click_SortValue() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().experienceTeamSort, null, TestPlugin.getInstance());
        playerMock.openInventory(topGUI.getInventory());

        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(TestPlugin.getInstance().getTop().valueTeamSort.item.slot);

        assertTrue(inventoryClickEvent.isCancelled());
        assertTrue(topGUI.getSortingType() instanceof ValueTeamSort<TestTeam>);
    }

    @Test
    public void topGUI__SortExperience() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().valueTeamSort, null, TestPlugin.getInstance());
        playerMock.openInventory(topGUI.getInventory());

        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(TestPlugin.getInstance().getTop().experienceTeamSort.item.slot);

        assertTrue(inventoryClickEvent.isCancelled());
        assertTrue(topGUI.getSortingType() instanceof ExperienceTeamSort<TestTeam>);
    }

    @Test
    public void topGUI__PreviousPage__Error() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().experienceTeamSort, null, TestPlugin.getInstance());
        playerMock.openInventory(topGUI.getInventory());

        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(TestPlugin.getInstance().getInventories().topGUI.size - 7);

        assertTrue(inventoryClickEvent.isCancelled());
        assertEquals(1, topGUI.getPage());
    }

    @Test
    public void topGUI__PreviousPage__Success() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().experienceTeamSort, null, TestPlugin.getInstance());
        topGUI.setPage(2);
        playerMock.openInventory(topGUI.getInventory());

        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(TestPlugin.getInstance().getInventories().topGUI.size - 7);

        assertTrue(inventoryClickEvent.isCancelled());
        assertEquals(1, topGUI.getPage());
    }

    @Test
    public void topGUI__NextPage__Error() {
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().experienceTeamSort, null, TestPlugin.getInstance());
        playerMock.openInventory(topGUI.getInventory());

        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(TestPlugin.getInstance().getInventories().topGUI.size - 3);

        assertTrue(inventoryClickEvent.isCancelled());
        assertEquals(1, topGUI.getPage());
    }

    @Test
    public void topGUI__NextPage__Success() {
        for (int i = 0; i < 10; i++) {
            new TeamBuilder().build();
        }
        PlayerMock playerMock = new UserBuilder(serverMock).build();
        TopGUI<TestTeam, User> topGUI = new TopGUI<>(TestPlugin.getInstance().getTop().experienceTeamSort, null, TestPlugin.getInstance());
        playerMock.openInventory(topGUI.getInventory());

        InventoryClickEvent inventoryClickEvent = playerMock.simulateInventoryClick(TestPlugin.getInstance().getInventories().topGUI.size - 3);

        assertTrue(inventoryClickEvent.isCancelled());
        assertEquals(2, topGUI.getPage());
    }
}