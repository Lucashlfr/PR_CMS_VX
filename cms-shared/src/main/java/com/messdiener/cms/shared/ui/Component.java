package com.messdiener.cms.shared.ui;

import com.messdiener.cms.shared.enums.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Component {
    private int number;
    private ComponentType type; // z.B. "text", "textarea", "select", "checkbox", etc.
    private String name;
    private String label;
    private String value;
    private String options; // für select, checkbox-gruppen etc.
    private boolean required;
    private List<Component> columns;

    public static Component empty() {
        return new Component(99, ComponentType.TEXT, "#", "#", "#", "", true, new ArrayList<>());
    }

    public static Component of(int number, ComponentType type, String name, String label, String value, boolean required) {
        return new Component(number, type, name, label, value, "",  required, new ArrayList<>());
    }

    public static Component of(int number, ComponentType type, String name, String label, String options, String value, boolean required) {
        return new Component(number, type, name, label, value, options,  required, new ArrayList<>());
    }
}