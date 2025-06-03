package com.knemis.skyblock.skyblockcoreproject.teams;

// import com.knemis.skyblock.skyblockcoreproject.core.keviincore.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    private com.knemis.skyblock.skyblockcoreproject.teams.Item item; // TODO: Replace with actual Item class
    private int page;
    private int defaultRank;
}
