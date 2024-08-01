package com.scl.core.dao;

import com.scl.core.model.DataConstraintModel;
import com.scl.occ.dto.DropdownListWsDTO;

import java.util.List;

public interface DataConstraintDao {

    Integer findDaysByConstraintName(String constraintName);

    String findVersionByConstraintName(String constraintName);

    String findQueryByConstraintName(String constraintName);

    String findPasswordByConstraintName(String constraintName);

    List<DataConstraintModel> findAll();
}
