package org.michahirsch.hawkbit.ledstripe.ddi.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("org.michahirsch.ddi.client")
public class DdiClientProperties {

    private String baseUrl;
    private String controllerId;
    private String tenant;
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String getControllerId() {
        return controllerId;
    }
    public void setControllerId(final String controllerId) {
        this.controllerId = controllerId;
    }
    public String getTenant() {
        return tenant;
    }
    public void setTenant(final String tenant) {
        this.tenant = tenant;
    }
}
