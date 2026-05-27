package com.shashi.splitwise.settlement.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @EntityGraph(attributePaths = {"fromUser", "toUser"})
    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId ORDER BY s.settledAt DESC, s.id DESC")
    List<Settlement> findAllByGroupId(@Param("groupId") Long groupId);
}
