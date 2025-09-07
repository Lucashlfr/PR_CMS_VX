package com.messdiener.cms.v3.app.entities.table;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class CMSRow {

    private List<CMSCell> cells;

    public boolean check(){
        return true;
    }
}
