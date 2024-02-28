package com.eydms.core.dao;

import de.hybris.platform.core.model.ItemModel;

import java.util.List;

public interface EYDMSGenericDao {
    ItemModel findItemByTypeCodeAndUidParam(String typeCode, String uidQualifier, String param);

    List<ItemModel> findListItemByTypeCodeAndUidParam(String typeCode, String uidQualifier, String param);
}
