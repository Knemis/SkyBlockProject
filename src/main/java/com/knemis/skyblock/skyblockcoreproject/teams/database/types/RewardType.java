package com.knemis.skyblock.skyblockcoreproject.teams.database.types;

import com.keviin.keviincore.Persist;
import com.keviin.keviinteams.keviinTeams;
import com.keviin.keviinteams.Reward;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

import java.sql.SQLException;

public class RewardType extends StringType {

    private static final RewardType instance = new RewardType();
    private static Persist persist;

    public static RewardType getSingleton(keviinTeams<?, ?> keviinTeams) {
        persist = new Persist(Persist.PersistType.JSON, keviinTeams);
        return instance;
    }

    protected RewardType() {
        super(SqlType.STRING, new Class<?>[]{Reward.class});
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String value = (String) super.sqlArgToJava(fieldType, sqlArg, columnPos);
        return persist.load(Reward.class, value);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object object) {
        Reward reward = (Reward) object;
        return persist.toString(reward);
    }

}
