package fr.chsfleury.flux.core;

import java.sql.Timestamp;
import java.util.Date;

public class Time {

    public static Timestamp timestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static Timestamp timestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }

    public static Timestamp inOneHour() {
        return new Timestamp(System.currentTimeMillis() + 3_600_000);
    }


}
