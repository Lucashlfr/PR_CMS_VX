// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\ComponentView.java
package com.messdiener.cms.workflow.web.view;

import com.messdiener.cms.shared.enums.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter @AllArgsConstructor
public class ComponentView {
    public enum MatrixType { TEXT, FILE }

    private final String name;
    private final String label;
    private final ComponentType type;
    private final List<String> options;
    private final boolean required;
    private final String value;
    private final List<ComponentColumn> columns;
}
