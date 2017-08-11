package org.michahirsch.hawkbit.ledstripe.ddi.api;

import feign.RequestLine;
import feign.Response;

public interface DdiClientDownloadInterface {

    @RequestLine("GET ")
    Response download();

}