package com.practo.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ExcelUtil that ALWAYS starts with a fresh workbook for the given path:
 * - If the file does not exist: creates a new workbook.
 * - If the file exists: resets it (removes all previous data) so only the current run's data is saved.
 *
 * Usage:
 *   ExcelUtil x = new ExcelUtil("output/PractoData.xlsx"); // starts fresh every run
 *   x.header("Hospitals", List.of("Name","Area","City","Rating","Phone"));
 *   x.append("Hospitals", List.of(name, area, city, rating, phone));
 *   ...
 *   x.saveAndClose();
 */
public class ExcelUtil {

    private final Path file;
    private final Workbook wb;

    public ExcelUtil(String path) throws IOException {
        this.file = Path.of(path).toAbsolutePath();

        // Always start fresh: if the file exists, delete it (we will recreate a new workbook)
        if (Files.exists(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                // If locked or delete fails, we will still write a fresh copy via .part + REPLACE below
            }
        }

        // Create a new empty workbook for this run
        this.wb = new XSSFWorkbook();
    }

    /** Create header row for a sheet if not present. */
    public synchronized void header(String sheetName, List<String> columns) {
        Sheet sh = wb.getSheet(sheetName);
        if (sh == null) {
            sh = wb.createSheet(sheetName);
            Row r = sh.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                r.createCell(i, CellType.STRING).setCellValue(columns.get(i));
                sh.autoSizeColumn(i);
            }
        } else {
            // If someone calls header twice on same sheet, ensure row 0 exists and matches size
            if (sh.getPhysicalNumberOfRows() == 0) {
                Row r = sh.createRow(0);
                for (int i = 0; i < columns.size(); i++) {
                    r.createCell(i, CellType.STRING).setCellValue(columns.get(i));
                    sh.autoSizeColumn(i);
                }
            }
        }
    }

    /** Append a row under the header (row 0). */
    public synchronized void append(String sheetName, List<String> values) {
        Sheet sh = wb.getSheet(sheetName);
        if (sh == null) {
            throw new IllegalStateException("Call header() first for sheet: " + sheetName);
        }
        int rowIndex = Math.max(1, sh.getLastRowNum() + 1);
        Row r = sh.createRow(rowIndex);
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i) == null ? "" : values.get(i);
            r.createCell(i, CellType.STRING).setCellValue(v);
            sh.autoSizeColumn(i);
        }
    }

    /**
     * Write once at end. Uses a .part temp file + atomic move.
     * If target is locked, falls back to timestamped alternative file in the same directory.
     */
    public synchronized void saveAndClose() throws IOException {
        // Ensure directory exists
        Path dir = file.getParent();
        if (dir != null) Files.createDirectories(dir);

        // Write to temp .part beside the final file
        Path tmp = file.resolveSibling(file.getFileName().toString() + ".part");
        try (OutputStream os = Files.newOutputStream(tmp,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            wb.write(os);
        }

        // Try atomic move to the final path
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException locked) {
            // If locked, write to timestamped alternative
            String base = file.getFileName().toString();
            String name = base.endsWith(".xlsx") ? base.substring(0, base.length() - 5) : base;
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Path alt = file.resolveSibling(name + "_" + ts + ".xlsx");
            Files.move(tmp, alt, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            wb.close();
        }
    }
}