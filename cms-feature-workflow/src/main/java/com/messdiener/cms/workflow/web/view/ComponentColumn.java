// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\ComponentColumn.java
package com.messdiener.cms.workflow.web.view;

import com.messdiener.cms.workflow.web.view.ComponentView.MatrixType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ComponentColumn {
    private final String label;
    private final String name;
    private final MatrixType type;
}
