package fr.chsfleury.flux.core;

import java.sql.Timestamp;

public class Time {

    public static Timestamp timestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp inOneHour() {
        return new Timestamp(System.currentTimeMillis() + 3_600_000);
    }

}
