/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.huawei.elibri.kotlin.service.model;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.Indexes;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.util.Date;

/**
 * Definition of ObjectType ProfileInterest.
 *
 * @since 2021-03-24
 */
@PrimaryKeys({"id"})
public class ProfileInterest extends CloudDBZoneObject {
    private Integer id;

    private String email;

    private String interestId;

    public ProfileInterest() {
        super(ProfileInterest.class);
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setInterestId(String interestId) {
        this.interestId = interestId;
    }

    public String getInterestId() {
        return interestId;
    }

}
