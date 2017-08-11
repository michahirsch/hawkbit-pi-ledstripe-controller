package org.michahirsch.hawkbitpistipecontroller;

import java.io.IOException;

import org.michahirsch.hawkbit.ledstripe.ddi.api.DdiClientInterface;
import org.michahirsch.hawkbit.ledstripe.ddi.client.DdiClientFactory;
import org.michahirsch.hawkbit.ledstripe.ddi.client.DdiClientProperties;
import org.michahirsch.hawkbit.ledstripe.ddi.client.DownloadPersistenceStrategy;
import org.michahirsch.hawkbit.ledstripe.ddi.client.FilePersistenceStrategy;
import org.michahirsch.hawkbit.ledstripe.ddi.client.script.LedScriptExecutorService;
import org.michahirsch.hawkbit.ledstripe.ddi.client.script.ScriptExecutorService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.google.common.collect.Lists;

import feign.Feign;
import feign.Logger.Level;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

@SpringBootApplication
@EnableConfigurationProperties({ DdiClientProperties.class })
public class HawkbitDdiLedStripeControllerApplication {

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(HawkbitDdiLedStripeControllerApplication.class, args);

    }

    @Bean
    DdiClientFactory ddiClientFactory(final DdiClientProperties clientProperties,
            final DdiClientInterface clientInterface, final DownloadPersistenceStrategy downloadPersistenceStrategy,
            final ScriptExecutorService scriptExecutorService) {
        return new DdiClientFactory(clientProperties, clientInterface, downloadPersistenceStrategy,
                scriptExecutorService);
    }

    @Bean
    DdiClientRunOnStartupApplicationListener ddiClientRunOnStartupApplicationListener(
            final DdiClientFactory ddiClientFactory) {
        return new DdiClientRunOnStartupApplicationListener(ddiClientFactory);
    }

    @Bean
    DdiClientInterface ddiClientInterface(final DdiClientProperties ddiClientProperties) {
        return Feign.builder().logLevel(Level.FULL)
                .decoder(new JacksonDecoder(Lists.newArrayList(new Jackson2HalModule()))).encoder(new JacksonEncoder())
                .target(DdiClientInterface.class, ddiClientProperties.getBaseUrl());
    }

    @Bean
    DownloadPersistenceStrategy downloadPersistenceStrategy() {
        return new FilePersistenceStrategy();
    }

    @Bean
    ScriptExecutorService scriptExecutorService() {
        return new LedScriptExecutorService();
    }

}
