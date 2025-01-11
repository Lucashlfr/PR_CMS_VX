package com.messdiener.cms.v3.app.entities.workflows.request;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowRequest {
    private String type;
    private String startDate;
    private String endDate;
    private List<String> uuids;
}
