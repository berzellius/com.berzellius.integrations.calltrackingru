package com.berzellius.integrations.calltrackingru.dto.api.calltracking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Created by berz on 23.02.2016.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallTrackingWebsiteSourceConditionsResponse {

    private CallTrackingWebsiteSourceConditions response;
    private Integer error;

    public CallTrackingWebsiteSourceConditions getResponse() {
        return response;
    }

    public void setResponse(CallTrackingWebsiteSourceConditions response) {
        this.response = response;
    }

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }
}
