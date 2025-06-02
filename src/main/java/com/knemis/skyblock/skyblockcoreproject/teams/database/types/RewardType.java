package com.knemis.skyblock.skyblockcoreproject.teams.database.types;

// import com.keviin.keviincore.Persist; // TODO: Replace with actual Persist class
import com.knemis.skyblock.skyblockcoreproject.teams.IridiumTeams;
import com.knemis.skyblock.skyblockcoreproject.teams.Reward;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

import java.sql.SQLException;

public class RewardType extends StringType {

    private static final RewardType instance = new RewardType();
    private static com.knemis.skyblock.skyblockcoreproject.teams.Persist persist; // TODO: Replace with actual Persist class

    public static RewardType getSingleton(IridiumTeams<?, ?> iridiumTeams) { // TODO: Update IridiumTeams generic type if needed
        persist = new com.knemis.skyblock.skyblockcoreproject.teams.Persist(com.knemis.skyblock.skyblockcoreproject.teams.Persist.PersistType.JSON, iridiumTeams); // TODO: Replace with actual Persist class
        return instance;
    }

    protected RewardType() {
        super(SqlType.STRING, new Class<?>[]{Reward.class});
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String value = (String) super.sqlArgToJava(fieldType, sqlArg, columnPos);
        return persist.load(com.knemis.skyblock.skyblockcoreproject.teams.Reward.class, value); // TODO: Uncomment when persist is available
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object object) {
        com.knemis.skyblock.skyblockcoreproject.teams.Reward reward = (com.knemis.skyblock.skyblockcoreproject.teams.Reward) object;
        return persist.toString(reward); // TODO: Uncomment when persist is available
    }

}
