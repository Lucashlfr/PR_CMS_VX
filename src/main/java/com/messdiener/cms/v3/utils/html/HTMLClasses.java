package com.messdiener.cms.v3.utils.html;

import org.springframework.stereotype.Component;

@Component
public class HTMLClasses {

    public String classes(String step, String currentStep) {
        return step.equals(currentStep) ? "tab-pane fade show active" : "tab-pane fade";
    }

    public String classesLink(String step, String currentStep) {
        return step.equals(currentStep) ? "nav-link active" : "nav-link";
    }
}
