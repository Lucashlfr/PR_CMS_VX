package com.messdiener.cms.person.persistence.service;

import com.messdiener.cms.person.domain.entity.data.connection.PersonConnection;
import com.messdiener.cms.person.persistence.map.PersonConnectionMapper;
import com.messdiener.cms.person.persistence.repo.PersonConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonConnectionService {

    private final PersonConnectionRepository repo;

    public void createConnection(PersonConnection connection) {
        repo.save(PersonConnectionMapper.toEntity(connection));
    }

    public List<PersonConnection> getConnectionsByHost(UUID host) {
        return repo.findByHost(host).stream().map(PersonConnectionMapper::toDomain).toList();
    }

    public void deleteConnection(UUID id) {
        repo.deleteById(id);
    }
}
