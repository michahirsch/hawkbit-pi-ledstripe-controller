package org.michahirsch.hawkbit.ledstripe.ddi.api;

import org.eclipse.hawkbit.ddi.json.model.DdiActionFeedback;
import org.eclipse.hawkbit.ddi.json.model.DdiControllerBase;
import org.eclipse.hawkbit.ddi.json.model.DdiDeploymentBase;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface DdiClientInterface {

    @RequestLine("GET /{tenant}/controller/v1/{controllerId}")
    DdiControllerBase getControllerBase(@Param("tenant") String tenant, @Param("controllerId") String controllerId);

    @RequestLine("GET /{tenant}/controller/v1/{controllerId}/deploymentBase/{actionId}")
    DdiDeploymentBase getControllerBasedeploymentAction(@Param("tenant")String tenant, @Param("controllerId") String controllerId,
            @Param("actionId")long actionId);

    @RequestLine("POST /{tenant}/controller/v1/{controllerId}/deploymentBase/{actionId}/feedback")
    @Headers("Content-Type: application/json")
    void postBasedeploymentActionFeedback(DdiActionFeedback feedback, @Param("tenant")String tenant, @Param("controllerId")String controllerId,
            @Param("actionId") long actionId);

}
