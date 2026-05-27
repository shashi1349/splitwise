package com.shashi.splitwise.group.domain;

/**
 * Role of a user within a group. Stored as a string in the database so
 * adding new roles later (ADMIN, VIEWER...) doesn't require a migration.
 */
public enum MemberRole {
    OWNER,
    MEMBER
}
