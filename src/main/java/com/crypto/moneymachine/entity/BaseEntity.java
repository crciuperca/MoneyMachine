package com.crypto.moneymachine.entity;

import javax.persistence.Column;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_at")
    private Date createdAt;
}
