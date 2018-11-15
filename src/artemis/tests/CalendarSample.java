/*
 * Copyright (c) FOREVER, Jefferson Buot. All rights reserved.
 * Build | Imagine | Think | Explore -> By: Jeff 
 */
package artemis.tests;

import java.util.Calendar;

/**
 *
 * @author Jefferson
 */
public class CalendarSample {

    public static void main(String[] args) {
        Calendar dateTime = Calendar.getInstance();
        System.out.printf("1. %tc\n", dateTime);
        System.out.printf("2. %tF\n", dateTime);
        System.out.printf("3. %tD\n", dateTime);
        System.out.printf("4. %tr\n", dateTime);
        System.out.printf("5. %tT\n", dateTime);
        System.out.printf("6. %1$tA, %1$tB %1$td, %1$tY\n", dateTime);
        System.out.printf("7. %1$TA, %1$TB %1$Td, %1$TY\n", dateTime);
        System.out.printf("8. %1$ta, %1$tb %1$te, %1$ty\n", dateTime);
        System.out.printf("9. %1$tH:%1$tM:%1$tS\n", dateTime);
        System.out.printf("10. %1$tZ %1$tI:%1$tM:%1$tS ", dateTime);
    }


}
