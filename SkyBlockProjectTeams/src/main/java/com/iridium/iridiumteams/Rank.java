package com.keviin.keviinteams;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rank {
    VISITOR(-1),
    OWNER(-2);
    private int id;
}
