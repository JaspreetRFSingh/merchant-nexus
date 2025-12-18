package com.merchant.nexus.controller;

import com.merchant.nexus.dto.SettlementDTO;
import com.merchant.nexus.exception.ErrorResponse;
import com.merchant.nexus.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Settlement operations.
 */
@RestController
@RequestMapping("${api.base-path}/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlements", description = "Merchant settlement and payout APIs")
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    @Operation(summary = "Create a settlement request", description = "Request a new settlement payout for a merchant")
    @ApiResponse(responseCode = "201", description = "Settlement created successfully",
                 content = @Content(schema = @Schema(implementation = SettlementDTO.SettlementResponse.class)))
    public ResponseEntity<SettlementDTO.SettlementResponse> createSettlement(
            @RequestParam String merchantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd,
            @RequestParam BigDecimal grossAmount,
            @RequestParam(defaultValue = "0.03") BigDecimal commissionRate,
            @RequestParam String bankAccountNumber,
            @RequestParam String bankName) {
        
        SettlementDTO.SettlementResponse response = settlementService.createSettlement(
                merchantId, periodStart, periodEnd, grossAmount, commissionRate,
                bankAccountNumber, bankName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get settlement by ID", description = "Retrieve settlement details")
    @ApiResponse(responseCode = "200", description = "Settlement found")
    public ResponseEntity<SettlementDTO.SettlementResponse> getSettlement(
            @Parameter(description = "Settlement ID") @PathVariable String id) {
        return ResponseEntity.ok(settlementService.getSettlement(id));
    }

    @GetMapping("/merchant/{merchantId}")
    @Operation(summary = "List settlements by merchant", description = "Get all settlements for a merchant")
    @ApiResponse(responseCode = "200", description = "Settlements retrieved successfully")
    public ResponseEntity<List<SettlementDTO.SettlementResponse>> getSettlementsByMerchant(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(settlementService.getSettlementsByMerchant(merchantId, page, size));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a settlement", description = "Approve a pending settlement for processing")
    @ApiResponse(responseCode = "200", description = "Settlement approved successfully")
    public ResponseEntity<SettlementDTO.SettlementResponse> approveSettlement(
            @PathVariable String id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(settlementService.approveSettlement(id, approvedBy));
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Process a settlement", description = "Execute the payment for an approved settlement")
    @ApiResponse(responseCode = "200", description = "Settlement processed successfully")
    @ApiResponse(responseCode = "503", description = "Payment gateway unavailable",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<SettlementDTO.SettlementResponse> processSettlement(
            @PathVariable String id) {
        return ResponseEntity.ok(settlementService.processSettlement(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a settlement", description = "Reject a settlement with a reason")
    @ApiResponse(responseCode = "200", description = "Settlement rejected successfully")
    public ResponseEntity<SettlementDTO.SettlementResponse> rejectSettlement(
            @PathVariable String id,
            @RequestParam String reason,
            @RequestParam String rejectedBy) {
        return ResponseEntity.ok(settlementService.rejectSettlement(id, reason, rejectedBy));
    }

    @PostMapping("/{id}/adjust")
    @Operation(summary = "Apply adjustment to settlement", description = "Add an adjustment amount to a pending settlement")
    @ApiResponse(responseCode = "200", description = "Adjustment applied successfully")
    public ResponseEntity<SettlementDTO.SettlementResponse> applyAdjustment(
            @PathVariable String id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(settlementService.applyAdjustment(id, amount, reason));
    }

    @GetMapping("/merchant/{merchantId}/summary")
    @Operation(summary = "Get settlement summary", description = "Get aggregated settlement statistics for a merchant")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    public ResponseEntity<SettlementDTO.SettlementSummary> getSettlementSummary(
            @PathVariable String merchantId) {
        return ResponseEntity.ok(settlementService.getSettlementSummary(merchantId));
    }
}
