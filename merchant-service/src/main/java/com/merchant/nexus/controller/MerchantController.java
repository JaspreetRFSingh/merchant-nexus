package com.merchant.nexus.controller;

import com.merchant.nexus.dto.MerchantDTO;
import com.merchant.nexus.dto.MerchantDTO.CreateMerchantRequest;
import com.merchant.nexus.dto.MerchantDTO.MerchantResponse;
import com.merchant.nexus.exception.ErrorResponse;
import com.merchant.nexus.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Merchant operations.
 * Demonstrates: RESTful API design, OpenAPI documentation, validation
 */
@RestController
@RequestMapping("${api.base-path}/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchants", description = "Merchant management APIs")
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    @Operation(summary = "Create a new merchant", description = "Register a new merchant on the platform")
    @ApiResponse(responseCode = "201", description = "Merchant created successfully",
                 content = @Content(schema = @Schema(implementation = MerchantResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<MerchantResponse> createMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {
        MerchantResponse response = merchantService.createMerchant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get merchant by ID", description = "Retrieve merchant details by unique identifier")
    @ApiResponse(responseCode = "200", description = "Merchant found",
                 content = @Content(schema = @Schema(implementation = MerchantResponse.class)))
    @ApiResponse(responseCode = "404", description = "Merchant not found",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<MerchantResponse> getMerchant(
            @Parameter(description = "Merchant ID") @PathVariable String id) {
        return ResponseEntity.ok(merchantService.getMerchantById(id));
    }

    @GetMapping
    @Operation(summary = "List all merchants", description = "Retrieve paginated list of merchants")
    @ApiResponse(responseCode = "200", description = "Merchants retrieved successfully")
    public ResponseEntity<List<MerchantResponse>> listMerchants(
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction") 
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.fromString(direction), sort));
        return ResponseEntity.ok(merchantService.getAllMerchants(pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get merchants by status", description = "Filter merchants by their verification status")
    @ApiResponse(responseCode = "200", description = "Merchants retrieved successfully")
    public ResponseEntity<List<MerchantResponse>> getMerchantsByStatus(
            @Parameter(description = "Merchant status") @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                merchantService.getMerchantsByStatus(
                        com.coupang.merchant.model.Merchant.MerchantStatus.valueOf(status), 
                        pageable));
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify a merchant", description = "Approve a pending merchant application")
    @ApiResponse(responseCode = "200", description = "Merchant verified successfully")
    @ApiResponse(responseCode = "400", description = "Merchant not in pending status")
    public ResponseEntity<MerchantResponse> verifyMerchant(
            @PathVariable String id,
            @RequestParam String verifiedBy) {
        return ResponseEntity.ok(merchantService.verifyMerchant(id, verifiedBy));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend a merchant", description = "Temporarily suspend merchant operations")
    @ApiResponse(responseCode = "200", description = "Merchant suspended successfully")
    public ResponseEntity<MerchantResponse> suspendMerchant(
            @PathVariable String id,
            @RequestParam String reason,
            @RequestParam String suspendedBy) {
        return ResponseEntity.ok(merchantService.suspendMerchant(id, reason, suspendedBy));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a suspended merchant", description = "Reactivate a previously suspended merchant")
    @ApiResponse(responseCode = "200", description = "Merchant activated successfully")
    public ResponseEntity<MerchantResponse> activateMerchant(
            @PathVariable String id,
            @RequestParam String activatedBy) {
        return ResponseEntity.ok(merchantService.activateMerchant(id, activatedBy));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a merchant", description = "Permanently deactivate a merchant account")
    @ApiResponse(responseCode = "200", description = "Merchant deactivated successfully")
    public ResponseEntity<MerchantResponse> deactivateMerchant(
            @PathVariable String id,
            @RequestParam String reason,
            @RequestParam String deactivatedBy) {
        return ResponseEntity.ok(merchantService.deactivateMerchant(id, reason, deactivatedBy));
    }

    @GetMapping("/stats/count-by-status")
    @Operation(summary = "Get merchant count by status", description = "Get statistics on merchant counts per status")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<MerchantStatusStats> getMerchantCountByStatus() {
        MerchantStatusStats stats = new MerchantStatusStats();
        stats.setPendingVerification(
                merchantService.countByStatus(com.coupang.merchant.model.Merchant.MerchantStatus.PENDING_VERIFICATION));
        stats.setActive(
                merchantService.countByStatus(com.coupang.merchant.model.Merchant.MerchantStatus.ACTIVE));
        stats.setSuspended(
                merchantService.countByStatus(com.coupang.merchant.model.Merchant.MerchantStatus.SUSPENDED));
        stats.setDeactivated(
                merchantService.countByStatus(com.coupang.merchant.model.Merchant.MerchantStatus.DEACTIVATED));
        return ResponseEntity.ok(stats);
    }

    @lombok.Data
    public static class MerchantStatusStats {
        private long pendingVerification;
        private long active;
        private long suspended;
        private long deactivated;
    }
}
