package org.michahirsch.hawkbit.ledstripe.ddi.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.hawkbit.ddi.json.model.DdiActionFeedback;
import org.eclipse.hawkbit.ddi.json.model.DdiArtifact;
import org.eclipse.hawkbit.ddi.json.model.DdiChunk;
import org.eclipse.hawkbit.ddi.json.model.DdiControllerBase;
import org.eclipse.hawkbit.ddi.json.model.DdiDeploymentBase;
import org.eclipse.hawkbit.ddi.json.model.DdiResult;
import org.eclipse.hawkbit.ddi.json.model.DdiResult.FinalResult;
import org.eclipse.hawkbit.ddi.json.model.DdiStatus;
import org.eclipse.hawkbit.ddi.json.model.DdiStatus.ExecutionStatus;
import org.michahirsch.hawkbit.ledstripe.ddi.api.DdiClientDownloadInterface;
import org.michahirsch.hawkbit.ledstripe.ddi.api.DdiClientInterface;
import org.michahirsch.hawkbit.ledstripe.ddi.client.script.ScriptExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;

import com.google.common.collect.Lists;

import feign.FeignException;

/**
 * 
 */
public class DdiClient implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DdiClient.class);

    private final String controllerId;
    private final String tenant;

    private final DdiClientInterface ddiClientInterface;
    private final DownloadPersistenceStrategy persistenceStrategy;
    private final ScriptExecutorService scriptExecutorService;

    private long runningAction = -1;

    private final ScheduledExecutorService executorService;

    public DdiClient(final String controllerId, final ScheduledExecutorService executorService,
            final DdiClientProperties ddiClientProperties, final DdiClientInterface ddiClientInterface,
            final DownloadPersistenceStrategy downloadPersistenceStrategy,
            final ScriptExecutorService scriptExecutorService) {
        this.executorService = executorService;
        this.ddiClientInterface = ddiClientInterface;
        persistenceStrategy = downloadPersistenceStrategy;
        this.scriptExecutorService = scriptExecutorService;
        this.controllerId = controllerId;
        this.tenant = ddiClientProperties.getTenant();
    }

    @Override
    public void run() {
        try {
            LOGGER.info(" Controller {} polling from hawkBit server", controllerId);
            final DdiControllerBase controllerBase = ddiClientInterface.getControllerBase(tenant, controllerId);
            final String pollingTimeFormReponse = controllerBase.getConfig().getPolling().getSleep();
            final LocalTime localtime = LocalTime.parse(pollingTimeFormReponse);
            final long pollingIntervalInMillis = localtime.getLong(ChronoField.MILLI_OF_DAY);
            final Link controllerDeploymentBaseLink = controllerBase.getLink("deploymentBase");
            if (controllerDeploymentBaseLink != null) {
                final Long actionId = getActionIdOutOfLink(controllerDeploymentBaseLink);
                if (actionId != runningAction) {
                    runningAction = actionId;
                    startDownloadAndExecute(actionId);
                } else {
                    sendFeedBackMessage(actionId, ExecutionStatus.REJECTED, FinalResult.FAILURE,
                            Collections.singleton("Already running action with id: " + runningAction
                                    + " cannot start new action id: " + actionId));
                }
            }
            executorService.schedule(this, pollingIntervalInMillis, TimeUnit.MILLISECONDS);
        } catch (final RuntimeException e) {
            LOGGER.error("Error during running ddi-client, keep running", e);
            executorService.schedule(this, 2000, TimeUnit.MILLISECONDS);
        }
    }

    private void startDownloadAndExecute(final Long actionId) {
        final DdiDeploymentBase ddiDeploymentBase = ddiClientInterface.getControllerBasedeploymentAction(tenant,
                controllerId, actionId);
        final List<DdiChunk> chunks = ddiDeploymentBase.getDeployment().getChunks();
        for (final DdiChunk chunk : chunks) {
            downloadArtifactsAndExecute(chunk, actionId);
        }
    }

    private void downloadArtifactsAndExecute(final DdiChunk chunk, final Long actionId) {
        final List<DdiArtifact> artifactList = chunk.getArtifacts();
        if (artifactList.isEmpty()) {
            sendFeedBackMessage(actionId, ExecutionStatus.CLOSED, FinalResult.FAILURE,
                    Collections.singleton("No artifacts to download for softwaremodule " + chunk.getName()));
            return;
        }

        FinalResult f = FinalResult.SUCESS;

        for (final DdiArtifact ddiArtifact : artifactList) {
            final File downloadArtifact = downloadArtifact(actionId, ddiArtifact);
            try {
                scriptExecutorService.execute(controllerId, downloadArtifact);
                sendFeedBackMessage(actionId, ExecutionStatus.PROCEEDING, FinalResult.NONE,
                        Collections.singleton("Successful invoked script " + downloadArtifact.getName()));

            } catch (final ScriptExecutionException e) {
                f = FinalResult.FAILURE;
                sendFeedBackMessage(actionId, ExecutionStatus.CLOSED, FinalResult.FAILURE,
                        Lists.newArrayList("Error during invoking " + downloadArtifact.getName(),
                                "Return Code of script: " + e.getRetCode(), "Detail message: " + e.getMessage()));
            }
        }
        if (f.equals(FinalResult.SUCESS)) {
            finishSuccessUpdate(actionId);
            return;
        }
        finishErrorUpdate(actionId, Collections.singleton("Bad update"));

    }

    private File downloadArtifact(final Long actionId, final DdiArtifact ddiArtifact) {

        final DdiClientDownloadInterface ddiDownloadClient = DdiDownloadClient.create(ddiArtifact, null);

        final String filename = ddiArtifact.getFilename();

        sendFeedBackMessage(actionId, ExecutionStatus.PROCEEDING, FinalResult.NONE,
                Collections.singleton("Starting download of artifact " + filename));
        LOGGER.info("Starting download of artifact " + filename);

        try {
            LOGGER.info("Preparing download stream");
            final InputStream downloadStream = ddiDownloadClient.download().body().asInputStream();
            LOGGER.info("Download stream received");
            final File storedFile = persistenceStrategy.handleInputStream(downloadStream, filename);
            sendFeedBackMessage(actionId, ExecutionStatus.PROCEEDING, FinalResult.NONE,
                    Collections.singleton("Downloaded artifact " + filename));
            return storedFile;
        } catch (final IOException | FeignException e) {
            LOGGER.error("Download of artifact failed", e);
            sendFeedBackMessage(actionId, ExecutionStatus.PROCEEDING, FinalResult.NONE,
                    Collections.singleton("Download of artifact " + filename + "failed"));
        }
        return null;
    }

    private void sendFeedBackMessage(final Long actionId, final ExecutionStatus executionStatus,
            final FinalResult finalResult, final Collection<String> messages) {
        final DdiResult result = new DdiResult(finalResult, null);
        final List<String> details = new ArrayList<>(messages);
        final DdiStatus ddiStatus = new DdiStatus(executionStatus, result, details);
        final String time = String.valueOf(LocalDateTime.now());
        final DdiActionFeedback feedback = new DdiActionFeedback(actionId, time, ddiStatus);
        ddiClientInterface.postBasedeploymentActionFeedback(feedback, tenant, controllerId, actionId);
        LOGGER.info("Sent feedback message to HaktBit");
    }

    private void finishSuccessUpdate(final long actionId) {
        sendFeedBackMessage(actionId, ExecutionStatus.CLOSED, FinalResult.SUCESS,
                Collections.singleton("Script execution successfull"));
    }

    private void finishErrorUpdate(final long actionId, final Collection<String> messages) {
        sendFeedBackMessage(actionId, ExecutionStatus.CLOSED, FinalResult.FAILURE, messages);
    }

    private Long getActionIdOutOfLink(final Link controllerDeploymentBaseLink) {
        final String link = controllerDeploymentBaseLink.getHref();
        return Long.valueOf(link.substring(link.lastIndexOf('/') + 1, link.indexOf('?')));
    }
}
