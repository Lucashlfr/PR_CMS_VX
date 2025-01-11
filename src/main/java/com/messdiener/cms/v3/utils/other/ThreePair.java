package com.messdiener.cms.v3.utils.other;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThreePair<A,B,C> {

    private A a;
    private B b;
    private C c;

}
