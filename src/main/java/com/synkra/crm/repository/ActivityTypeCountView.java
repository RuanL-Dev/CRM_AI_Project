package com.synkra.crm.repository;

import com.synkra.crm.model.ActivityType;

public interface ActivityTypeCountView {

    ActivityType getType();

    long getTotal();
}
