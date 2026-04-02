package com.erp.system.accounting.service;

import com.erp.system.accounting.dto.display.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LedgerService ledgerService;
    private final AccountingReportService reportService;

    // ===================== LEDGER EXCEL =====================

    public byte[] exportLedgerExcel(Long accountId, LocalDate fromDate, LocalDate toDate) throws IOException {
        LedgerDisplayDto ledger = ledgerService.getLedger(accountId, fromDate, toDate);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Ledger");

            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle currencyStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            int rowIdx = 0;
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("General Ledger — " + ledger.getAccountCode() + " " + ledger.getAccountName());

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Period: " +
                    (fromDate != null ? DATE_FMT.format(fromDate) : "Beginning") + " to " +
                    (toDate != null ? DATE_FMT.format(toDate) : "Present"));

            Row obRow = sheet.createRow(rowIdx++);
            obRow.createCell(0).setCellValue("Opening Balance:");
            org.apache.poi.ss.usermodel.Cell obCell = obRow.createCell(1);
            obCell.setCellValue(ledger.getOpeningBalance().doubleValue());
            obCell.setCellStyle(currencyStyle);

            rowIdx++;
            String[] headers = {"Date", "Reference", "Description", "Debit", "Credit", "Balance"};
            Row hRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = hRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (LedgerLineDisplayDto line : ledger.getLines()) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(DATE_FMT.format(line.getEntryDate()));
                r.createCell(1).setCellValue(line.getJournalReference());
                r.createCell(2).setCellValue(line.getDescription() != null ? line.getDescription() : "");
                setCurrency(r.createCell(3), line.getDebit(), currencyStyle);
                setCurrency(r.createCell(4), line.getCredit(), currencyStyle);
                setCurrency(r.createCell(5), line.getRunningBalance(), currencyStyle);
            }

            rowIdx++;
            Row cbRow = sheet.createRow(rowIdx);
            cbRow.createCell(0).setCellValue("Closing Balance:");
            setCurrency(cbRow.createCell(5), ledger.getClosingBalance(), currencyStyle);

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ===================== LEDGER PDF =====================

    public byte[] exportLedgerPdf(Long accountId, LocalDate fromDate, LocalDate toDate) {
        LedgerDisplayDto ledger = ledgerService.getLedger(accountId, fromDate, toDate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font cellFont = new Font(Font.HELVETICA, 8);
        Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD);

        doc.add(new Paragraph("General Ledger", titleFont));
        doc.add(new Paragraph("Account: " + ledger.getAccountCode() + " — " + ledger.getAccountName(), boldFont));
        doc.add(new Paragraph("Period: " +
                (fromDate != null ? DATE_FMT.format(fromDate) : "Beginning") + " to " +
                (toDate != null ? DATE_FMT.format(toDate) : "Present"), cellFont));
        doc.add(new Paragraph("Opening Balance: " + ledger.getOpeningBalance(), boldFont));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(new float[]{12, 14, 30, 14, 14, 16});
        table.setWidthPercentage(100);
        for (String h : new String[]{"Date", "Reference", "Description", "Debit", "Credit", "Balance"}) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
            hCell.setBackgroundColor(new Color(44, 62, 80));
            hCell.setPadding(5);
            table.addCell(hCell);
        }

        for (LedgerLineDisplayDto line : ledger.getLines()) {
            table.addCell(new Phrase(DATE_FMT.format(line.getEntryDate()), cellFont));
            table.addCell(new Phrase(line.getJournalReference(), cellFont));
            table.addCell(new Phrase(line.getDescription() != null ? line.getDescription() : "", cellFont));
            table.addCell(rightAligned(fmtAmount(line.getDebit()), cellFont));
            table.addCell(rightAligned(fmtAmount(line.getCredit()), cellFont));
            table.addCell(rightAligned(fmtAmount(line.getRunningBalance()), cellFont));
        }

        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setBorder(0);
        for (int i = 0; i < 4; i++) table.addCell(emptyCell);
        table.addCell(new Phrase("Closing:", boldFont));
        table.addCell(rightAligned(fmtAmount(ledger.getClosingBalance()), boldFont));

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    // ===================== P&L EXCEL =====================

    public byte[] exportProfitLossExcel(LocalDate fromDate, LocalDate toDate) throws IOException {
        ProfitLossReportDto report = reportService.getProfitLoss(fromDate, toDate);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Profit & Loss");
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle currencyStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            int rowIdx = 0;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Profit & Loss Statement");
            sheet.createRow(rowIdx++).createCell(0).setCellValue(
                    "Period: " + DATE_FMT.format(fromDate) + " to " + DATE_FMT.format(toDate));
            rowIdx++;

            rowIdx = writeSection(sheet, rowIdx, "REVENUES", new String[]{"Code", "Account", "Amount"},
                    report.getRevenues(), headerStyle, currencyStyle, true);
            Row trRow = sheet.createRow(rowIdx++);
            trRow.createCell(0).setCellValue("Total Revenue");
            setCurrency(trRow.createCell(2), report.getTotalRevenue(), currencyStyle);
            rowIdx++;

            rowIdx = writeSection(sheet, rowIdx, "EXPENSES", new String[]{"Code", "Account", "Amount"},
                    report.getExpenses(), headerStyle, currencyStyle, true);
            Row teRow = sheet.createRow(rowIdx++);
            teRow.createCell(0).setCellValue("Total Expenses");
            setCurrency(teRow.createCell(2), report.getTotalExpenses(), currencyStyle);
            rowIdx++;

            Row npRow = sheet.createRow(rowIdx);
            npRow.createCell(0).setCellValue("NET PROFIT");
            setCurrency(npRow.createCell(2), report.getNetProfit(), currencyStyle);

            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ===================== P&L PDF =====================

    public byte[] exportProfitLossPdf(LocalDate fromDate, LocalDate toDate) {
        ProfitLossReportDto report = reportService.getProfitLoss(fromDate, toDate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font cellFont = new Font(Font.HELVETICA, 9);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        doc.add(new Paragraph("Profit & Loss Statement", titleFont));
        doc.add(new Paragraph("Period: " + DATE_FMT.format(fromDate) + " to " + DATE_FMT.format(toDate), cellFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Revenues", boldFont));
        doc.add(buildPLTable(report.getRevenues(), headerFont, cellFont));
        doc.add(new Paragraph("Total Revenue: " + fmtAmount(report.getTotalRevenue()), boldFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Expenses", boldFont));
        doc.add(buildPLTable(report.getExpenses(), headerFont, cellFont));
        doc.add(new Paragraph("Total Expenses: " + fmtAmount(report.getTotalExpenses()), boldFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Net Profit: " + fmtAmount(report.getNetProfit()), titleFont));

        doc.close();
        return out.toByteArray();
    }

    // ===================== BALANCE SHEET EXCEL =====================

    public byte[] exportBalanceSheetExcel(LocalDate asOfDate) throws IOException {
        BalanceSheetReportDto report = reportService.getBalanceSheet(asOfDate);
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Balance Sheet");
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle currencyStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            currencyStyle.setDataFormat(df.getFormat("#,##0.00"));

            int rowIdx = 0;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Balance Sheet");
            sheet.createRow(rowIdx++).createCell(0).setCellValue("As of: " + DATE_FMT.format(asOfDate));
            rowIdx++;

            rowIdx = writeBSSection(sheet, rowIdx, "ASSETS", report.getAssets(), headerStyle, currencyStyle);
            Row taRow = sheet.createRow(rowIdx++);
            taRow.createCell(0).setCellValue("Total Assets");
            setCurrency(taRow.createCell(2), report.getTotalAssets(), currencyStyle);
            rowIdx++;

            rowIdx = writeBSSection(sheet, rowIdx, "LIABILITIES", report.getLiabilities(), headerStyle, currencyStyle);
            Row tlRow = sheet.createRow(rowIdx++);
            tlRow.createCell(0).setCellValue("Total Liabilities");
            setCurrency(tlRow.createCell(2), report.getTotalLiabilities(), currencyStyle);
            rowIdx++;

            rowIdx = writeBSSection(sheet, rowIdx, "EQUITY", report.getEquity(), headerStyle, currencyStyle);
            Row eRow = sheet.createRow(rowIdx++);
            eRow.createCell(0).setCellValue("Total Equity");
            setCurrency(eRow.createCell(2), report.getTotalEquity(), currencyStyle);
            rowIdx++;

            Row leRow = sheet.createRow(rowIdx++);
            leRow.createCell(0).setCellValue("LIABILITIES + EQUITY");
            setCurrency(leRow.createCell(2), report.getLiabilitiesAndEquity(), currencyStyle);

            Row bRow = sheet.createRow(rowIdx);
            bRow.createCell(0).setCellValue("Balanced: " + (report.isBalanced() ? "YES" : "NO"));

            for (int i = 0; i < 3; i++) sheet.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        }
    }

    // ===================== BALANCE SHEET PDF =====================

    public byte[] exportBalanceSheetPdf(LocalDate asOfDate) {
        BalanceSheetReportDto report = reportService.getBalanceSheet(asOfDate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        Font cellFont = new Font(Font.HELVETICA, 9);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        doc.add(new Paragraph("Balance Sheet", titleFont));
        doc.add(new Paragraph("As of: " + DATE_FMT.format(asOfDate), cellFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Assets", boldFont));
        doc.add(buildBSTable(report.getAssets(), headerFont, cellFont));
        doc.add(new Paragraph("Total Assets: " + fmtAmount(report.getTotalAssets()), boldFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Liabilities", boldFont));
        doc.add(buildBSTable(report.getLiabilities(), headerFont, cellFont));
        doc.add(new Paragraph("Total Liabilities: " + fmtAmount(report.getTotalLiabilities()), boldFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Equity", boldFont));
        doc.add(buildBSTable(report.getEquity(), headerFont, cellFont));
        doc.add(new Paragraph("Total Equity: " + fmtAmount(report.getTotalEquity()), boldFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Liabilities + Equity: " + fmtAmount(report.getLiabilitiesAndEquity()), boldFont));
        doc.add(new Paragraph("Balanced: " + (report.isBalanced() ? "YES" : "NO"), titleFont));

        doc.close();
        return out.toByteArray();
    }

    // ===================== HELPERS =====================

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void setCurrency(org.apache.poi.ss.usermodel.Cell cell, BigDecimal value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value.doubleValue());
            cell.setCellStyle(style);
        }
    }

    private int writeSection(Sheet sheet, int rowIdx, String sectionTitle, String[] headers,
                             List<ProfitLossLineDto> lines, CellStyle headerStyle, CellStyle currencyStyle,
                             @SuppressWarnings("unused") boolean isPL) {
        sheet.createRow(rowIdx++).createCell(0).setCellValue(sectionTitle);
        Row hRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = hRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        for (ProfitLossLineDto line : lines) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(line.getAccountCode());
            r.createCell(1).setCellValue(line.getAccountNameEn());
            setCurrency(r.createCell(2), line.getAmount(), currencyStyle);
        }
        return rowIdx;
    }

    private int writeBSSection(Sheet sheet, int rowIdx, String sectionTitle,
                               List<BalanceSheetLineDto> lines, CellStyle headerStyle, CellStyle currencyStyle) {
        sheet.createRow(rowIdx++).createCell(0).setCellValue(sectionTitle);
        Row hRow = sheet.createRow(rowIdx++);
        String[] headers = {"Code", "Account", "Balance"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = hRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        for (BalanceSheetLineDto line : lines) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(line.getAccountCode());
            r.createCell(1).setCellValue(line.getAccountNameEn());
            setCurrency(r.createCell(2), line.getBalance(), currencyStyle);
        }
        return rowIdx;
    }

    private PdfPTable buildPLTable(List<ProfitLossLineDto> lines, Font headerFont, Font cellFont) {
        PdfPTable table = new PdfPTable(new float[]{15, 55, 30});
        table.setWidthPercentage(100);
        for (String h : new String[]{"Code", "Account", "Amount"}) {
            PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
            hc.setBackgroundColor(new Color(44, 62, 80));
            hc.setPadding(4);
            table.addCell(hc);
        }
        for (ProfitLossLineDto line : lines) {
            table.addCell(new Phrase(line.getAccountCode(), cellFont));
            table.addCell(new Phrase(line.getAccountNameEn(), cellFont));
            table.addCell(rightAligned(fmtAmount(line.getAmount()), cellFont));
        }
        return table;
    }

    private PdfPTable buildBSTable(List<BalanceSheetLineDto> lines, Font headerFont, Font cellFont) {
        PdfPTable table = new PdfPTable(new float[]{15, 55, 30});
        table.setWidthPercentage(100);
        for (String h : new String[]{"Code", "Account", "Balance"}) {
            PdfPCell hc = new PdfPCell(new Phrase(h, headerFont));
            hc.setBackgroundColor(new Color(44, 62, 80));
            hc.setPadding(4);
            table.addCell(hc);
        }
        for (BalanceSheetLineDto line : lines) {
            table.addCell(new Phrase(line.getAccountCode(), cellFont));
            table.addCell(new Phrase(line.getAccountNameEn(), cellFont));
            table.addCell(rightAligned(fmtAmount(line.getBalance()), cellFont));
        }
        return table;
    }

    private PdfPCell rightAligned(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    private String fmtAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
