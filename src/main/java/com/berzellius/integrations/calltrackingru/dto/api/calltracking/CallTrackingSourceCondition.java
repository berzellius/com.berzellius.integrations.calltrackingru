package com.berzellius.integrations.calltrackingru.dto.api.calltracking;


/**
 * Created by berz on 23.02.2016.
 */
public class CallTrackingSourceCondition{

    public CallTrackingSourceCondition(){}

    private Long id;

    private String sourceName;

    private String utmSource;

    private String utmMedium;

    private String utmCampaign;

    private Integer projectId;

    private Integer truth;

    private Integer phonesCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "CallTrackingSourceCondition#".concat(this.getId().toString());
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getUtmSource() {
        return utmSource;
    }

    public void setUtmSource(String utmSource) {
        this.utmSource = utmSource;
    }

    public String getUtmMedium() {
        return utmMedium;
    }

    public void setUtmMedium(String utmMedium) {
        this.utmMedium = utmMedium;
    }

    public String getUtmCampaign() {
        return utmCampaign;
    }

    public void setUtmCampaign(String utmCampaign) {
        this.utmCampaign = utmCampaign;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getTruth() {
        return truth;
    }

    public void setTruth(Integer truth) {
        this.truth = truth;
    }

    public Integer getPhonesCount() {
        return phonesCount;
    }

    public void setPhonesCount(Integer phonesCount) {
        this.phonesCount = phonesCount;
    }
}
