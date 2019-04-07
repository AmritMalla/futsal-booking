package com.amrit.futsal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "FutsalDetail")
public class FutsalDetail extends AbstractEntity {


    private static final long serialVersionUID = -5462788365600793391L;

    @Column
    private String topic;

    @Column
    private String description;

    @ManyToOne
    private Futsal futsal;

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
