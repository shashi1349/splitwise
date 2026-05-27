package com.shashi.splitwise.group.application;

import com.shashi.splitwise.common.error.ConflictException;
import com.shashi.splitwise.common.error.NotFoundException;
import com.shashi.splitwise.group.api.dto.CreateGroupRequest;
import com.shashi.splitwise.group.api.dto.GroupDetail;
import com.shashi.splitwise.group.api.dto.GroupSummary;
import com.shashi.splitwise.group.api.dto.InviteMemberRequest;
import com.shashi.splitwise.group.api.dto.MemberDto;
import com.shashi.splitwise.group.domain.ExpenseGroup;
import com.shashi.splitwise.group.domain.GroupMember;
import com.shashi.splitwise.group.domain.GroupMemberRepository;
import com.shashi.splitwise.group.domain.GroupRepository;
import com.shashi.splitwise.group.domain.MemberRole;
import com.shashi.splitwise.user.domain.User;
import com.shashi.splitwise.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private static final String DEFAULT_CURRENCY = "INR";

    private final GroupRepository groups;
    private final GroupMemberRepository members;
    private final UserRepository users;

    public GroupService(GroupRepository groups, GroupMemberRepository members, UserRepository users) {
        this.groups = groups;
        this.members = members;
        this.users = users;
    }

    @Transactional
    public GroupDetail createGroup(CreateGroupRequest req, Long actingUserId) {
        User owner = users.findById(actingUserId)
            .orElseThrow(() -> new NotFoundException("User not found."));
        String currency = (req.currencyCode() == null || req.currencyCode().isBlank())
            ? DEFAULT_CURRENCY
            : req.currencyCode().toUpperCase();
        ExpenseGroup group = groups.save(new ExpenseGroup(req.name().trim(), currency, owner));
        GroupMember ownerMembership = members.save(new GroupMember(group, owner, MemberRole.OWNER));
        return GroupDetail.of(group, List.of(MemberDto.from(ownerMembership)));
    }

    @Transactional(readOnly = true)
    public List<GroupSummary> listMyGroups(Long actingUserId) {
        return members.findSummariesForUser(actingUserId);
    }

    @Transactional(readOnly = true)
    public GroupDetail getGroupDetail(Long groupId, Long actingUserId) {
        ExpenseGroup group = requireMembership(groupId, actingUserId);
        List<MemberDto> dtos = members.findAllForGroup(groupId).stream()
            .map(MemberDto::from)
            .toList();
        return GroupDetail.of(group, dtos);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> listMembers(Long groupId, Long actingUserId) {
        requireMembership(groupId, actingUserId);
        return members.findAllForGroup(groupId).stream().map(MemberDto::from).toList();
    }

    @Transactional
    public MemberDto inviteMember(Long groupId, InviteMemberRequest req, Long actingUserId) {
        ExpenseGroup group = requireMembership(groupId, actingUserId);
        String email = req.email().trim().toLowerCase();
        User invitee = users.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(
                "No user is registered with that email yet. Ask them to sign up first."));
        if (members.existsByGroupIdAndUserId(groupId, invitee.getId())) {
            throw new ConflictException("That user is already a member of this group.");
        }
        GroupMember saved = members.save(new GroupMember(group, invitee, MemberRole.MEMBER));
        return MemberDto.from(saved);
    }

    /**
     * Returns the group iff the acting user is a member. We deliberately
     * return {@code 404} (not {@code 403}) when the caller isn't a member
     * to avoid leaking which group ids exist.
     */
    public ExpenseGroup requireMembership(Long groupId, Long actingUserId) {
        ExpenseGroup group = groups.findById(groupId)
            .orElseThrow(() -> new NotFoundException("Group not found."));
        if (!members.existsByGroupIdAndUserId(groupId, actingUserId)) {
            throw new NotFoundException("Group not found.");
        }
        return group;
    }
}
