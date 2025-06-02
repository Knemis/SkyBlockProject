package com.keviin.testplugin.api;

import com.keviin.testplugin.TestTeam;
import com.keviin.testplugin.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EnhancementUpdateEvent implements Listener {

    public static boolean called = false;

    @EventHandler
    public void onEnhancementUpdate(com.keviin.keviinteams.api.EnhancementUpdateEvent<TestTeam, User> event){
        called = true;
    }

}
