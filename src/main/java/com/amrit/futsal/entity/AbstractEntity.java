package com.amrit.futsal.entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class AbstractEntity<PK extends Serializable> extends AbstractPersistable<PK> {

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();


    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @Version
    @Column
    private int version;


    public AbstractEntity() {
        setCreated(new Date());
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreated() {
        return created;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

}