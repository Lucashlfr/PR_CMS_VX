package com.messdiener.cms.shared.table;

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
