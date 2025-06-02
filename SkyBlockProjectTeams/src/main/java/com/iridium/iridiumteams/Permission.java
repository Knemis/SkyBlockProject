package com.keviin.keviinteams;

import com.keviin.keviincore.Item;
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
