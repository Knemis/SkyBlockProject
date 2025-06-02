package com.keviin.testplugin;

import com.keviin.keviinteams.database.keviinUser;

import java.util.UUID;

public class User extends keviinUser<TestTeam> {
    public User(UUID uuid, String username) {
        super();
        setUuid(uuid);
        setName(username);
    }
}
