package com.shashi.splitwise.group.api;

import com.shashi.splitwise.common.security.AuthenticatedUser;
import com.shashi.splitwise.group.api.dto.CreateGroupRequest;
import com.shashi.splitwise.group.api.dto.GroupDetail;
import com.shashi.splitwise.group.api.dto.GroupSummary;
import com.shashi.splitwise.group.api.dto.InviteMemberRequest;
import com.shashi.splitwise.group.api.dto.MemberDto;
import com.shashi.splitwise.group.application.GroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/groups")
@Tag(name = "Groups", description = "Create groups, invite members, list memberships.")
public class GroupController {

    private final GroupService service;

    public GroupController(GroupService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GroupDetail> create(
            @Valid @RequestBody CreateGroupRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        GroupDetail created = service.createGroup(req, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<GroupSummary> listMine(@AuthenticationPrincipal AuthenticatedUser principal) {
        return service.listMyGroups(principal.id());
    }

    @GetMapping("/{groupId}")
    public GroupDetail get(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.getGroupDetail(groupId, principal.id());
    }

    @GetMapping("/{groupId}/members")
    public List<MemberDto> listMembers(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.listMembers(groupId, principal.id());
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<MemberDto> invite(
            @PathVariable Long groupId,
            @Valid @RequestBody InviteMemberRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        MemberDto added = service.inviteMember(groupId, req, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }
}
