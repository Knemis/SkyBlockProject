package com.keviin.testplugin;

import com.knemis.skyblock.skyblockcoreproject.teams.database.SkyBlockProjectUser;

import java.util.UUID;

public class User extends SkyBlockProjectUser<TestTeam> {
    public User(UUID uuid, String username) {
        super();
        setUuid(uuid);
        setName(username);
    }
}
