package com.shashi.splitwise.expense.api;

import com.shashi.splitwise.common.api.PageResponse;
import com.shashi.splitwise.common.security.AuthenticatedUser;
import com.shashi.splitwise.expense.api.dto.CreateExpenseRequest;
import com.shashi.splitwise.expense.api.dto.ExpenseDto;
import com.shashi.splitwise.expense.application.ExpenseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups/{groupId}/expenses")
@Tag(name = "Expenses", description = "Group expenses with EQUAL, EXACT, or PERCENT splits.")
public class ExpenseController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExpenseDto> create(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest req,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        ExpenseDto created = service.createExpense(groupId, principal.id(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public PageResponse<ExpenseDto> list(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return PageResponse.from(service.listExpenses(groupId, principal.id(), pageable));
    }
}
