package com.messdiener.cms.v3.app.entities.worship;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class HeatmapDto {

    private List<String> weekdays;
    private List<Integer> hours;
    private List<HeatCell> matrix;
    private long maxValue;

    @Data
    public static class HeatCell {
        private int x;
        private int y;
        private long v;
    }
}
