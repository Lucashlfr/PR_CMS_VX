package com.messdiener.cms.v3.web.request;

import lombok.Data;

import java.util.UUID;

@Data
public class StepForm {
    private UUID workflowId;
    private int stepNumber;
    private String name;
    private String description;
    private String userRole;
}