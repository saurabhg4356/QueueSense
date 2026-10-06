package com.queuesense.dto;

import java.util.ArrayList;
import java.util.List;

public class ChartDataDto {
    private List<String> labels = new ArrayList<>();
    private List<Number> data = new ArrayList<>();
    private String datasetLabel;

    public ChartDataDto() {}

    public ChartDataDto(String datasetLabel) {
        this.datasetLabel = datasetLabel;
    }

    public void addPoint(String label, Number value) {
        labels.add(label);
        data.add(value);
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
    }

    public String getDatasetLabel() {
        return datasetLabel;
    }

    public void setDatasetLabel(String datasetLabel) {
        this.datasetLabel = datasetLabel;
    }
}
