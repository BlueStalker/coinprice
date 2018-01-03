package com.stalker.bitcoin.http.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by curt on 1/2/18.
 */
public class Status {
    private Long uptime;
    private String status;
    private Boolean started;

    @JsonProperty
    public Long getUptime() {
        return uptime;
    }
    @JsonProperty
    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }
    @JsonProperty
    public String getStatus() {
        return status;
    }

    @JsonProperty
    public void setStatus(String status) {
        this.status = status;
    }
    @JsonProperty
    public Boolean getStarted() {
        return started;
    }
    @JsonProperty
    public void setStarted(Boolean started) {
        this.started = started;
    }
}
