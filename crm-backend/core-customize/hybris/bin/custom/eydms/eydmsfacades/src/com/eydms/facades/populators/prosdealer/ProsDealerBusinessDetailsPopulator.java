package com.eydms.facades.populators.prosdealer;

import com.eydms.core.model.BrandWiseSaleModel;
import com.eydms.core.model.NominationModel;
import com.eydms.core.model.ProspectiveDealerModel;
import com.eydms.core.model.StorageModel;
import com.eydms.facades.prosdealer.data.*;
import de.hybris.platform.converters.Populator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProsDealerBusinessDetailsPopulator implements Populator<ProspectiveDealerModel, ProsDealerData> {

    @Override
    public void populate(ProspectiveDealerModel source, ProsDealerData target) {

        List<StorageModel> storages = new ArrayList<>(source.getStorages());
        List<StorageAndInfraStructureData> storageAndInfraStructureDataList = new ArrayList<>();

        List<BrandWiseSaleModel> brandWiseSales = new ArrayList<>(source.getBrandWiseSale());
        List<BrandWiseSaleData> brandWiseSaleDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(brandWiseSales)){
            for(BrandWiseSaleModel brandWiseSale : brandWiseSales){
                BrandWiseSaleData  brandWiseSaleData =  new BrandWiseSaleData();

                brandWiseSaleData.setBrandCode(null != brandWiseSale.getBrand() ? brandWiseSale.getBrand().getIsocode(): StringUtils.EMPTY);
                brandWiseSaleData.setBrandWiseSaleID(brandWiseSale.getBrandWiseSaleID());
                brandWiseSaleData.setSaleInMT(brandWiseSale.getSaleInMT());
                brandWiseSaleDataList.add(brandWiseSaleData);
            }
        }
        if(CollectionUtils.isNotEmpty(storages))
            for(StorageModel storage : storages){
                StorageAndInfraStructureData storageAndInfraStructureData = new StorageAndInfraStructureData();
                storageAndInfraStructureData.setVehicleType(null != storage.getVehicleType()? storage.getVehicleType().getCode() : StringUtils.EMPTY);
                storageAndInfraStructureData.setNoOfVehicles(storage.getNoOfVehicles());
                storageAndInfraStructureData.setStorageID(storage.getStorageID());

                storageAndInfraStructureDataList.add(storageAndInfraStructureData);
            }

        DealerBusinessDetailsData dealerBusinessDetailsData = new DealerBusinessDetailsData();
        //TODO:: UPDATE CODE TO SET PROPER CODE
        dealerBusinessDetailsData.setCode(source.getUid());
        dealerBusinessDetailsData.setWarehouseSpace(source.getWarehouseSpace());
        dealerBusinessDetailsData.setStorage(storageAndInfraStructureDataList);
        dealerBusinessDetailsData.setBrandWiseSale(brandWiseSaleDataList);

        target.setBusinessDetails(dealerBusinessDetailsData);
    }
}