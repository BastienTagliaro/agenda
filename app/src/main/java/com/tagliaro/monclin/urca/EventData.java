package com.tagliaro.monclin.urca;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bastien on 13/03/18.
 */

public class EventData {
    private List<PropertyData> propertyDataList = new ArrayList<>();
    private int index;

    public void add(PropertyData propertyData) {
        this.propertyDataList.add(propertyData);
        index++;
    }

    public PropertyData get(int index) {
        return propertyDataList.get(index);
    }

    public int size() {
        return index;
    }
}
