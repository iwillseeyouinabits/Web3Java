package com.Star.Star.services;

import java.util.Date;

/**
 * Service layer that contains date logic
 */
public class DateService {
    public static long getCurrentTime() {
        return new Date().getTime() / 1000 / 60;
    }
}
