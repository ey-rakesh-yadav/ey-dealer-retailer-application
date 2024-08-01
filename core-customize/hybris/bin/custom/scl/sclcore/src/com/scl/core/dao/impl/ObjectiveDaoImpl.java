package com.scl.core.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import java.util.Collections;
import java.util.List;

import com.scl.core.dao.ObjectiveDao;
import com.scl.core.model.ObjectiveModel;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.internal.dao.DefaultGenericDao;

public class ObjectiveDaoImpl extends DefaultGenericDao<ObjectiveModel> implements ObjectiveDao {

	public ObjectiveDaoImpl() {
		super(ObjectiveModel._TYPECODE);
	}


	@Override
	public ObjectiveModel findByObjectiveId(String objectiveId) {
        validateParameterNotNullStandardMessage("objectiveId", objectiveId);
        final List<ObjectiveModel> objectiveModelListe = this.find(Collections.singletonMap(ObjectiveModel.OBJECTIVEID, objectiveId));
        if (objectiveModelListe.size() > 1)
        {
            throw new AmbiguousIdentifierException(
                    String.format("Found %d objectives with the objectiveId value: '%s', which should be unique", objectiveModelListe.size(),
                    		objectiveId));
        }
        else
        {
            return objectiveModelListe.isEmpty() ? null : objectiveModelListe.get(0);
        }
    }
	
	@Override
	public List<ObjectiveModel> findAllObjective() {
        final List<ObjectiveModel> objectiveModelListe = this.find();
        return objectiveModelListe;
    }
	
}
