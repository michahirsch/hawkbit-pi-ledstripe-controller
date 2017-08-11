/**
 * 
 */
package org.michahirsch.hawkbit.ledstripe.ddi.api;

public class LedControllerIdUtil {

    private static final String PREFIX = "led_";

    public static String prefix(final int ledId) {
        return PREFIX + ledId;
    }

    public static int unprefix(final String controllerId) {
        return Integer.parseInt(controllerId.substring(0, PREFIX.length()));
    }

}
