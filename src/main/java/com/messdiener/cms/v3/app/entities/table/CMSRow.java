package com.messdiener.cms.v3.app.entities.table;

import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
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
