package com.amrit.futsal.model;

public class FutsalDetailDTO  {

    private Long id;

    private String topic;

    private String description;

    private Long futsal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getFutsal() {
        return futsal;
    }

    public void setFutsal(Long futsal) {
        this.futsal = futsal;
    }
}
