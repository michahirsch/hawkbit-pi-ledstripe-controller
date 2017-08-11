package org.michahirsch.hawkbit.ledstripe.ddi.client;

import java.util.concurrent.ScheduledExecutorService;

import org.michahirsch.hawkbit.ledstripe.ddi.api.DdiClientInterface;
import org.michahirsch.hawkbit.ledstripe.ddi.client.script.ScriptExecutorService;

public class DdiClientFactory {

    private final DdiClientProperties ddiClientProperties;
    private final DdiClientInterface ddiClientInterface;
    private final DownloadPersistenceStrategy downloadPersistenceStrategy;
    private final ScriptExecutorService scriptExecutorService;

    public DdiClientFactory(final DdiClientProperties ddiClientProperties, final DdiClientInterface ddiClientInterface,
            final DownloadPersistenceStrategy downloadPersistenceStrategy,
            final ScriptExecutorService scriptExecutorService) {
        this.ddiClientProperties = ddiClientProperties;
        this.ddiClientInterface = ddiClientInterface;
        this.downloadPersistenceStrategy = downloadPersistenceStrategy;
        this.scriptExecutorService = scriptExecutorService;
    }

    public DdiClient create(final ScheduledExecutorService executorService, final String controllerId) {
        return new DdiClient(controllerId, executorService, ddiClientProperties, ddiClientInterface,
                downloadPersistenceStrategy, scriptExecutorService);
    }

}
