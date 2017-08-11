package org.michahirsch.hawkbitpistipecontroller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.michahirsch.hawkbit.ledstripe.ddi.api.LedControllerIdUtil;
import org.michahirsch.hawkbit.ledstripe.ddi.client.DdiClient;
import org.michahirsch.hawkbit.ledstripe.ddi.client.DdiClientFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import de.pi3g.pi.ws2812.WS2812;

public class DdiClientRunOnStartupApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(3);
    private final DdiClientFactory ddiClientFactory;

    public DdiClientRunOnStartupApplicationListener(final DdiClientFactory ddiClientFactory) {
        this.ddiClientFactory = ddiClientFactory;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent arg0) {
        final int ledCount = 10;

        WS2812.get().init(ledCount);
        WS2812.get().clear();
        WS2812.get().show();

        for (int index = 0; index < ledCount; index++) {
            final DdiClient ddiClient = ddiClientFactory.create(scheduledExecutor, LedControllerIdUtil.prefix(index));
            scheduledExecutor.execute(ddiClient::run);
        }
    }

}
