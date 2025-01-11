package com.messdiener.cms.v3.utils.html;

import java.util.UUID;

public class HTMLClasses {

    public String classes(String step, String currentStep){
        if(step.equals(currentStep))
            return "tab-pane fade show active";
        return "tab-pane fade";
    }

    public String classesLink(String step, String currentStep){
        if(step.equals(currentStep))
            return "nav-link active";
        return "nav-link";
    }

    public String tenantButton(UUID tenantId, UUID cId){
        if(tenantId.equals(cId))
            return "btn btn-primary btn-block mb-1";
        return "btn btn-outline-blue btn-block mb-1";
    }

    public String monthButton(String i, String c){
        if(i.equals(c))
            return "btn btn-primary btn-block mb-1";
        return "btn btn-outline-blue btn-block mb-1";
    }

    public String financeHeader(String i, String c){
        if(i.equals(c)){
            return "";
        }
        return "";
    }

}
