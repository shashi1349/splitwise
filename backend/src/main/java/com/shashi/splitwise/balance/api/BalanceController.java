package com.shashi.splitwise.balance.api;

import com.shashi.splitwise.balance.api.dto.BalanceDto;
import com.shashi.splitwise.balance.application.BalanceService;
import com.shashi.splitwise.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/balances")
@Tag(name = "Balances", description = "Per-member net balance within a group.")
public class BalanceController {

    private final BalanceService service;

    public BalanceController(BalanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<BalanceDto> get(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.computeBalances(groupId, principal.id());
    }
}
