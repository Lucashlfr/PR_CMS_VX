package com.messdiener.cms.v3.web.request;


import com.messdiener.cms.v3.shared.enums.ComponentType;
import lombok.Data;

import java.util.UUID;

@Data
public class ComponentForm {
    private UUID stepId;
    private ComponentType type;
    private String name;
    private String label;
    private boolean required;
    private String options; // z. B. "Option1,Option2" für SELECT
}