package org.cardanofoundation.metabus.unittest.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetadataServiceTestHelper {

    Map<String, String> prepareMultiGroupJobData1() {
        Map<String, String> data1 = new LinkedHashMap<>();
        data1.put("total_bottle", "1000");
        data1.put("harvest_date", "01/10/2022");
        data1.put("harvest_location", "bolnisi1");
        data1.put("laboratory", "data1");
        data1.put("wineName", "data1");
        data1.put("wineType", "data1");
        data1.put("nominate", "data1");
        data1.put("type", "data1");
        data1.put("quantity", "data1");
        data1.put("bottleVolume", "data1");
        data1.put("totalVolume", "data1");
        data1.put("pricePerBottle", "data1");
        return data1;
    }

    Map<String, String> prepareMultiGroupJobData2() {
        Map<String, String> data2 = new LinkedHashMap<>();
        data2.put("total_bottle", "2000");
        data2.put("harvest_date", "2/10/2022");
        data2.put("harvest_location", "bolnisi2");
        data2.put("laboratory", "data2");
        data2.put("wineName", "data2");
        data2.put("wineType", "data2");
        data2.put("nominate", "data2");
        data2.put("type", "data2");
        data2.put("quantity", "data2");
        data2.put("bottleVolume", "data2");
        data2.put("totalVolume", "data2");
        data2.put("pricePerBottle", "data2");
        return data2;
    }

    Map<String, String> prepareMultiGroupJobData5() {
        Map<String, String> data5 = new LinkedHashMap<>();
        data5.put("total_bottle", "5000");
        data5.put("harvest_date", "5/10/2022");
        data5.put("harvest_location", "bolnisi5");
        data5.put("laboratory", "data5");
        data5.put("wineName", "data5");
        data5.put("wineType", "data5");
        data5.put("nominate", "data5");
        data5.put("type", "data5");
        data5.put("quantity", "data5");
        data5.put("bottleVolume", "data5");
        data5.put("totalVolume", "data5");
        data5.put("pricePerBottle", "data5");
        return data5;
    }

    Map<String, String> prepareMultiGroupJobData6() {
        Map<String, String> data6 = new LinkedHashMap<>();
        data6.put("total_bottle", "6000");
        data6.put("harvest_date", "6/10/2022");
        data6.put("harvest_location", "bolnisi6");
        data6.put("laboratory", "data6");
        data6.put("wineName", "data6");
        data6.put("wineType", "data6");
        data6.put("nominate", "data6");
        data6.put("type", "data6");
        data6.put("quantity", "data6");
        data6.put("bottleVolume", "data6");
        data6.put("totalVolume", "data6");
        data6.put("pricePerBottle", "data6");
        return data6;
    }

    String expectMultiGroupOffchainJson() {
        return """
                {"1234":[{"total_bottle":"1000","harvest_date":"01/10/2022","harvest_location":"bolnisi1","laboratory":"data1","wineName":"data1","wineType":"data1","nominate":"data1","type":"data1","quantity":"data1","bottleVolume":"data1","totalVolume":"data1","pricePerBottle":"data1"},{"total_bottle":"2000","harvest_date":"2/10/2022","harvest_location":"bolnisi2","laboratory":"data2","wineName":"data2","wineType":"data2","nominate":"data2","type":"data2","quantity":"data2","bottleVolume":"data2","totalVolume":"data2","pricePerBottle":"data2"}],"5678":[{"total_bottle":"5000","harvest_date":"5/10/2022","harvest_location":"bolnisi5","laboratory":"data5","wineName":"data5","wineType":"data5","nominate":"data5","type":"data5","quantity":"data5","bottleVolume":"data5","totalVolume":"data5","pricePerBottle":"data5"},{"total_bottle":"6000","harvest_date":"6/10/2022","harvest_location":"bolnisi6","laboratory":"data6","wineName":"data6","wineType":"data6","nominate":"data6","type":"data6","quantity":"data6","bottleVolume":"data6","totalVolume":"data6","pricePerBottle":"data6"}]}""";
    }

    Map<String, Object> prepareSingleGroupData1() {
        Map<String, String> lot_data = new LinkedHashMap<>();
        lot_data.put("laboratory", "data1");
        lot_data.put("wine_name", "data1");
        lot_data.put("wine_type", "data1");
        lot_data.put("nominate", "data1");
        lot_data.put("type", "data1");
        lot_data.put("quantity", "data1");
        lot_data.put("bottle_volume", "data1");
        lot_data.put("total_volume", "data1");
        lot_data.put("price_per_bottle", "data1");

        Map<String, Object> data1 = new LinkedHashMap<>();
        data1.put("date", "data1");
        data1.put("company_name", "data1");
        data1.put("country", "data1");
        data1.put("address", "data1");
        data1.put("certificate_type", "data1");
        data1.put("lot_entries", lot_data);

        return data1;
    }

    Map<String, Object> prepareSingleGroupData2() {
        Map<String, String> lot_data = new LinkedHashMap<>();
        lot_data.put("laboratory", "data2");
        lot_data.put("wine_name", "data2");
        lot_data.put("wine_type", "data2");
        lot_data.put("nominate", "data2");
        lot_data.put("type", "data2");
        lot_data.put("quantity", "data2");
        lot_data.put("bottle_volume", "data2");
        lot_data.put("total_volume", "data2");
        lot_data.put("price_per_bottle", "data2");

        Map<String, Object> data2 = new LinkedHashMap<>();
        data2.put("date", "data2");
        data2.put("company_name", "data2");
        data2.put("country", "data2");
        data2.put("address", "data2");
        data2.put("certificate_type", "data2");
        data2.put("lot_entries", lot_data);

        return data2;
    }

    String expectSingleGroupOffchainJson() {
        return """
                [{"date":"data1","company_name":"data1","country":"data1","address":"data1","certificate_type":"data1","lot_entries":{"laboratory":"data1","wine_name":"data1","wine_type":"data1","nominate":"data1","type":"data1","quantity":"data1","bottle_volume":"data1","total_volume":"data1","price_per_bottle":"data1"}},{"date":"data2","company_name":"data2","country":"data2","address":"data2","certificate_type":"data2","lot_entries":{"laboratory":"data2","wine_name":"data2","wine_type":"data2","nominate":"data2","type":"data2","quantity":"data2","bottle_volume":"data2","total_volume":"data2","price_per_bottle":"data2"}}]""";
    }
}
