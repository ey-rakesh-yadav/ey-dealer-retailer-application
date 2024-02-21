package com.eydms.core.dao;

import com.eydms.core.model.DataConstraintModel;
import com.eydms.occ.dto.DropdownListWsDTO;

import java.util.List;

public interface DataConstraintDao {

    Integer findDaysByConstraintName(String constraintName);

    String findVersionByConstraintName(String constraintName);

    List<DataConstraintModel> findAll();
}
