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
        if(cells == null){
            return false;
        }
        if(cells.size() < 5){
            return false;
        }

        if((new CMSDate(cells.getFirst().getContent(), DateUtils.DateType.GERMAN).toLong() < 0) || (new CMSDate(cells.getFirst().getContent(), DateUtils.DateType.SIMPLE_GERMAN).toLong() < 0)) return false;
        if(new CMSDate(cells.get(1).getContent(), DateUtils.DateType.TIME).toLong() < 0) return false;
        if(!(cells.get(2).getContent().equals("Knittelsheim") || cells.get(2).getContent().equals("Bellheim") || cells.get(2).getContent().equals("Ottersheim")))return false;

        return true;

    }
}
