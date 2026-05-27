package com.shashi.splitwise.group.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<ExpenseGroup, Long> {
}
