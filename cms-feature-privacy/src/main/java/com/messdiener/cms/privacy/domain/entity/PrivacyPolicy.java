package com.messdiener.cms.privacy.domain.entity;

import com.messdiener.cms.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PrivacyPolicy {

    private UUID id;
    private CMSDate date;
    private String firstname;
    private String lastname;
    private String street;
    private String number;
    private String plz;
    private String city;
    private boolean check1;
    private boolean check2;
    private boolean check3;
    private boolean check4;
    private boolean check5;
    private boolean check6;
    private boolean check7;
    private String signature;
}
