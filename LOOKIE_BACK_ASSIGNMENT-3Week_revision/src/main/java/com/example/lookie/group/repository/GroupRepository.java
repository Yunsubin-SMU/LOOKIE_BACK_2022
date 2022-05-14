package com.example.lookie.group.repository;

import com.example.lookie.group.domain.Group;
import com.example.lookie.group.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Question> findAllQuestion(Group group);
    Optional<Group> findByName(String name);
    Optional<Group> findByOwnerEmail(String OwnerEmail);
    Group findOneByName(String name);
}
