package com.scl.core.dao;

import com.scl.core.model.ProductAliasModel;

import java.util.List;

public interface ProductAliasDao {

    List<ProductAliasModel> findAllProductAlias();

    List<ProductAliasModel> getProductAlias();
}
