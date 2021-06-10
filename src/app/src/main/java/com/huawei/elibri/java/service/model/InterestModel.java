/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.elibri.java.service.model;

/**
 * Definition of InterestModel.
 *
 * @since 2020-12-11
 */
public class InterestModel {
    private final Interest userInterests;
    private boolean isSelected;
    private int id;
    private ProfileInterest interest;

    /**
     * Get the interest list
     *
     * @return the interest
     */
    public ProfileInterest getInterest() {
        return interest;
    }

    /**
     * Set the interest
     *
     * @param interest list of the user
     */
    public void setInterest(ProfileInterest interest) {
        this.interest = interest;
    }

    /**
     * Get the interests of the user
     *
     * @return the interest list
     */
    public Interest getUserInterests() {
        return userInterests;
    }

    /**
     * Get the interest ids
     *
     * @return interest id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the interest ids
     *
     * @param id of the interest
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Check which interest is selected
     *
     * @return interest is subscribed or not
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Set the interest of the user
     *
     * @param selected interest true or false
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Interest model constructor
     *
     * @param id interest id
     * @param userInterests user interests
     * @param isSelected is interest selected
     * @param interest list
     */
    public InterestModel(int id, Interest userInterests, boolean isSelected, ProfileInterest interest) {
        this.userInterests = userInterests;
        this.id = id;
        this.isSelected = isSelected;
        this.interest = interest;
    }
}
