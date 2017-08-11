package org.michahirsch.hawkbit.ledstripe.ddi.client.script;

import java.io.File;

import org.michahirsch.hawkbit.ledstripe.ddi.api.LedControllerIdUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import de.pi3g.pi.ws2812.WS2812;

public class LedScriptExecutorService implements ScriptExecutorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(final String controllerId, final File file) {

        try {
            final RgbJson rgbJson = objectMapper.readValue(file, RgbJson.class);
            final int ledId = LedControllerIdUtil.unprefix(controllerId);
            WS2812.get().setPixelColor(ledId, (byte) rgbJson.getR(), (byte) rgbJson.getG(), (byte) rgbJson.getB());
            WS2812.get().show();
        } catch (final Exception e) {
            Throwables.throwIfUnchecked(e);
        }

    }
}
