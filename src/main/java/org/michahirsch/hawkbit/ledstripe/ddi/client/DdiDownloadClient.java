package org.michahirsch.hawkbit.ledstripe.ddi.client;

import java.io.IOException;
import java.lang.reflect.Type;

import org.eclipse.hawkbit.ddi.json.model.DdiArtifact;
import org.michahirsch.hawkbit.ledstripe.ddi.api.DdiClientDownloadInterface;

import feign.Feign;
import feign.Feign.Builder;
import feign.FeignException;
import feign.Logger.Level;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.slf4j.Slf4jLogger;

public class DdiDownloadClient {

    public static DdiClientDownloadInterface create(final DdiArtifact ddiArtifact,
            final RequestInterceptor requestInterceptor) {
        final Builder feignBuilder = Feign.builder();
        if (requestInterceptor != null) {
            feignBuilder.requestInterceptor(requestInterceptor);
        }

        return feignBuilder.logLevel(Level.FULL).logger(new Slf4jLogger()).decoder(new Decoder() {
            @Override
            public Object decode(final Response response, final Type type)
                    throws IOException, DecodeException, FeignException {
                return response.body().asInputStream();
            }
        }).target(DdiClientDownloadInterface.class, ddiArtifact.getLink("download-http").getHref());
    }
}
