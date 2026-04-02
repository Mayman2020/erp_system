package com.erp.system.accounting.controller;

import com.erp.system.accounting.service.AccountingExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/accounting/export")
@RequiredArgsConstructor
public class AccountingExportController {

    private final AccountingExportService exportService;

    @GetMapping("/ledger/excel")
    public ResponseEntity<byte[]> exportLedgerExcel(
            @RequestParam Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) throws IOException {
        byte[] data = exportService.exportLedgerExcel(accountId, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ledger.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/ledger/pdf")
    public ResponseEntity<byte[]> exportLedgerPdf(
            @RequestParam Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        byte[] data = exportService.exportLedgerPdf(accountId, fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ledger.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/profit-loss/excel")
    public ResponseEntity<byte[]> exportProfitLossExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) throws IOException {
        byte[] data = exportService.exportProfitLossExcel(fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profit-loss.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/profit-loss/pdf")
    public ResponseEntity<byte[]> exportProfitLossPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        byte[] data = exportService.exportProfitLossPdf(fromDate, toDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=profit-loss.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/balance-sheet/excel")
    public ResponseEntity<byte[]> exportBalanceSheetExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) throws IOException {
        byte[] data = exportService.exportBalanceSheetExcel(asOfDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/balance-sheet/pdf")
    public ResponseEntity<byte[]> exportBalanceSheetPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        byte[] data = exportService.exportBalanceSheetPdf(asOfDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=balance-sheet.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
