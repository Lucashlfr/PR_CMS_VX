package com.messdiener.cms.domain.person;

import java.util.List;

public interface PersonLoginQueryPort {
    List<PersonLoginDTO> getPersonsByLogin();
}
