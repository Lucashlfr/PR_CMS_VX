// X:\workspace\PR_CMS\cms-feature-privacy\src\main\java\com\messdiener\cms\privacy\persistence\map\PrivacyPolicyMapper.java
package com.messdiener.cms.privacy.persistence.map;

import com.messdiener.cms.privacy.domain.entity.PrivacyPolicy;
import com.messdiener.cms.privacy.persistence.entity.PrivacyPolicyEntity;
import com.messdiener.cms.utils.time.CMSDate;

public final class PrivacyPolicyMapper {

    private PrivacyPolicyMapper(){}

    public static PrivacyPolicy toDomain(PrivacyPolicyEntity e){
        return new PrivacyPolicy(
                e.getId(),
                new CMSDate(e.getDate() == null ? 0L : e.getDate()),
                e.getFirstname(),
                e.getLastname(),
                e.getStreet(),
                e.getNumber(),
                e.getPlz(),
                e.getCity(),
                safeBool(e.getCheck1()),
                safeBool(e.getCheck2()),
                safeBool(e.getCheck3()),
                safeBool(e.getCheck4()),
                safeBool(e.getCheck5()),
                safeBool(e.getCheck6()),
                safeBool(e.getCheck7()),
                e.getSignature()
        );
    }

    public static PrivacyPolicyEntity toEntity(PrivacyPolicy d){
        return PrivacyPolicyEntity.builder()
                .id(d.getId())
                .date(d.getDate() != null ? d.getDate().toLong() : System.currentTimeMillis())
                .firstname(d.getFirstname())
                .lastname(d.getLastname())
                .street(d.getStreet())
                .number(d.getNumber())
                .plz(d.getPlz())
                .city(d.getCity())
                .check1(d.isCheck1())
                .check2(d.isCheck2())
                .check3(d.isCheck3())
                .check4(d.isCheck4())
                .check5(d.isCheck5())
                .check6(d.isCheck6())
                .check7(d.isCheck7())
                .signature(d.getSignature())
                .build();
    }

    private static boolean safeBool(Boolean b){ return b != null && b; }
}
