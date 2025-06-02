package com.knemis.skyblock.skyblockcoreproject.teams;

import com.knemis.skyblock.skyblockcoreproject.secondcore.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    private Item item;
    private int page;
    private int defaultRank;
}
