// X:\workspace\PR_CMS\cms-feature-workflow\src\main\java\com\messdiener\cms\workflow\web\view\builder\FormSchemaComponentBuilder.java
package com.messdiener.cms.workflow.web.view.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.shared.enums.ComponentType;
import com.messdiener.cms.workflow.web.view.ComponentColumn;
import com.messdiener.cms.workflow.web.view.ComponentView;
import com.messdiener.cms.workflow.web.view.ComponentView.MatrixType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
@RequiredArgsConstructor
public class FormSchemaComponentBuilder {

    private final ObjectMapper om = new ObjectMapper();

    public List<ComponentView> buildComponents(String jsonSchema, String uiSchema, Map<String, Object> payload) {
        try {
            if (!StringUtils.hasText(jsonSchema)) return List.of();

            JsonNode root = om.readTree(jsonSchema);
            JsonNode props = root.path("properties");
            if (props.isMissingNode() || !props.isObject()) return List.of();

            List<String> order = uiOrder(uiSchema, props);
            List<ComponentView> list = new ArrayList<>();

            for (String name : order) {
                JsonNode def = props.get(name);
                if (def == null) continue;

                String jsonType = def.path("type").asText("string");
                String title = def.path("title").asText(name);
                boolean required = isRequired(root, name);

                // options
                List<String> options = new ArrayList<>();
                JsonNode en = def.path("enum");
                if (en.isArray()) en.forEach(n -> options.add(n.asText()));

                // value (payload or default)
                String value = Optional.ofNullable(payload.get(name))
                        .map(Object::toString)
                        .orElse(def.path("default").asText(""));

                ComponentType type = mapType(jsonType, options, def);

                List<ComponentColumn> columns = List.of();
                if (type == ComponentType.MATRIX) {
                    columns = buildMatrixColumns(def);
                }

                list.add(new ComponentView(name, title, type, options, required, value, columns));
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive components from schema", e);
        }
    }

    private List<String> uiOrder(String uiSchema, JsonNode props) {
        List<String> order = new ArrayList<>();
        if (StringUtils.hasText(uiSchema)) {
            try {
                JsonNode ui = om.readTree(uiSchema);
                JsonNode ord = ui.path("ui:order");
                if (ord.isArray()) ord.forEach(n -> order.add(n.asText()));
            } catch (Exception ignore) { /* noop */ }
        }
        if (order.isEmpty()) props.fieldNames().forEachRemaining(order::add);
        return order;
    }

    private boolean isRequired(JsonNode rootSchema, String name) {
        JsonNode req = rootSchema.path("required");
        if (!req.isArray()) return false;
        for (JsonNode n : req) if (name.equals(n.asText())) return true;
        return false;
    }

    private ComponentType mapType(String jsonType, List<String> options, JsonNode def) {
        if (!options.isEmpty()) return ComponentType.SELECT;
        switch (jsonType) {
            case "string":
                int maxLen = def.path("maxLength").asInt(0);
                String format = def.path("format").asText("");
                if (maxLen > 200 || "textarea".equalsIgnoreCase(format)) return ComponentType.TEXTAREA;
                if ("email".equalsIgnoreCase(format)) return ComponentType.EMAIL;
                if ("date".equalsIgnoreCase(format)) return ComponentType.DATE;
                if ("time".equalsIgnoreCase(format)) return ComponentType.TIME;
                if ("signature".equalsIgnoreCase(format)) return ComponentType.SIGNATURE;
                if ("file".equalsIgnoreCase(format)) return ComponentType.FILE;
                return ComponentType.TEXT;
            case "number":
            case "integer":
                return ComponentType.TEXT;
            case "boolean":
                return ComponentType.CHECKBOX;
            case "array":
                JsonNode items = def.path("items");
                if (items.isObject() && items.path("properties").isObject()) {
                    return ComponentType.MATRIX;
                }
                return ComponentType.CHECKBOX; // Mehrfachauswahl
            default:
                return ComponentType.TEXT;
        }
    }

    private List<ComponentColumn> buildMatrixColumns(JsonNode def) {
        List<ComponentColumn> columns = new ArrayList<>();
        JsonNode cols = def.path("items").path("properties");
        if (cols.isObject()) {
            cols.fieldNames().forEachRemaining(colName -> {
                JsonNode colDef = cols.get(colName);
                String colLabel = colDef.path("title").asText(colName);
                String colType = colDef.path("type").asText("string");
                columns.add(new ComponentColumn(colLabel, colName, mapMatrixType(colType)));
            });
        }
        return columns;
    }

    private MatrixType mapMatrixType(String jsonType) {
        switch (jsonType) {
            case "string":
                return MatrixType.TEXT;
            default:
                return MatrixType.TEXT;
        }
    }
}
