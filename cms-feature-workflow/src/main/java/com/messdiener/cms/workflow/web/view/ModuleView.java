// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\ModuleView.java
package com.messdiener.cms.workflow.web.view;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ModuleView {
    private final String name;
    private final String description;
    private final String moduleId; // String fürs Template
}
