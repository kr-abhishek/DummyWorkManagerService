package org.kaabhis.bulk.datacreator.common;

import java.util.Random;

public enum WorkStatus {
    PENDING,
    IN_PROGRESS,
    HOLD,
    COMPLETED;

    private static final Random PRNG = new Random();

    public static WorkStatus nextState() {
        WorkStatus[] directions = values();
        return directions[PRNG.nextInt(directions.length)];
    }
}