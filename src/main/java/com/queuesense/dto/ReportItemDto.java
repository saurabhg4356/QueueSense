package com.queuesense.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Flexible DTO for reporting rows across different analytical queries.
 */
public class ReportItemDto {
    private String dimension1;
    private String dimension2;
    private String dimension3;
    private long metricCount1;
    private long metricCount2;
    private double metricDouble1;
    private double metricDouble2;
    private Map<String, Object> extraFields = new HashMap<>();

    public ReportItemDto() {}

    public String getDimension1() {
        return dimension1;
    }

    public void setDimension1(String dimension1) {
        this.dimension1 = dimension1;
    }

    public String getDimension2() {
        return dimension2;
    }

    public void setDimension2(String dimension2) {
        this.dimension2 = dimension2;
    }

    public String getDimension3() {
        return dimension3;
    }

    public void setDimension3(String dimension3) {
        this.dimension3 = dimension3;
    }

    public long getMetricCount1() {
        return metricCount1;
    }

    public void setMetricCount1(long metricCount1) {
        this.metricCount1 = metricCount1;
    }

    public long getMetricCount2() {
        return metricCount2;
    }

    public void setMetricCount2(long metricCount2) {
        this.metricCount2 = metricCount2;
    }

    public double getMetricDouble1() {
        return metricDouble1;
    }

    public void setMetricDouble1(double metricDouble1) {
        this.metricDouble1 = metricDouble1;
    }

    public double getMetricDouble2() {
        return metricDouble2;
    }

    public void setMetricDouble2(double metricDouble2) {
        this.metricDouble2 = metricDouble2;
    }

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }

    public void putExtra(String key, Object value) {
        this.extraFields.put(key, value);
    }
}
