package com.amrit.futsal.entity;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

public class FutsalDetail extends AbstractEntity<Long> {

    @Column
    String topic;

    @Column
    String description;

    @ManyToOne
    Futsal futsal;

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

    public Futsal getFutsal() {
        return futsal;
    }

    public void setFutsal(Futsal futsal) {
        this.futsal = futsal;
    }
}
