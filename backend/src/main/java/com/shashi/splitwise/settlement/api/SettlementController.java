package com.shashi.splitwise.settlement.api;

import com.shashi.splitwise.common.security.AuthenticatedUser;
import com.shashi.splitwise.settlement.api.dto.RecordSettlementRequest;
import com.shashi.splitwise.settlement.api.dto.SettlementDto;
import com.shashi.splitwise.settlement.api.dto.TransferDto;
import com.shashi.splitwise.settlement.application.SettlementService;
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
@RequestMapping("/groups/{groupId}")
@Tag(name = "Settlements", description = "Suggest minimum-transfer settle-ups and record paid transfers.")
public class SettlementController {

    private final SettlementService service;

    public SettlementController(SettlementService service) {
        this.service = service;
    }

    @GetMapping("/settle-up")
    public List<TransferDto> suggest(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.suggestSettlements(groupId, principal.id());
    }

    @PostMapping("/settlements")
    public ResponseEntity<SettlementDto> record(
            @PathVariable Long groupId,
            @Valid @RequestBody RecordSettlementRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        SettlementDto saved = service.recordSettlement(groupId, principal.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/settlements")
    public List<SettlementDto> list(
            @PathVariable Long groupId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.listSettlements(groupId, principal.id());
    }
}
