package com.synkra.crm.repository;

import com.synkra.crm.model.DealStage;

import java.math.BigDecimal;

public interface DealStageValueView {

    DealStage getStage();

    BigDecimal getTotal();
}
