package com.practo.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelUtil {
    private final Path file;
    private final Workbook wb;

    public ExcelUtil(String path) throws IOException {
        this.file = Path.of(path);
        if (Files.exists(file)) {
            try (InputStream is = Files.newInputStream(file)) {
                wb = new XSSFWorkbook(is);
            }
        } else {
            wb = new XSSFWorkbook();
        }
    }

    public synchronized void header(String sheet, List<String> cols) {
        Sheet sh = wb.getSheet(sheet);
        if (sh == null) {
            sh = wb.createSheet(sheet);
            Row r = sh.createRow(0);
            for (int i = 0; i < cols.size(); i++) {
                r.createCell(i, CellType.STRING).setCellValue(cols.get(i));
                sh.autoSizeColumn(i);
            }
        }
    }

    public synchronized void append(String sheet, List<String> vals) {
        Sheet sh = wb.getSheet(sheet);
        if (sh == null) throw new IllegalStateException("Create header first for " + sheet);
        Row r = sh.createRow(Math.max(1, sh.getLastRowNum() + 1));
        for (int i = 0; i < vals.size(); i++) {
            r.createCell(i, CellType.STRING).setCellValue(vals.get(i) == null ? "" : vals.get(i));
            sh.autoSizeColumn(i);
        }
    }

    /** Write once at end. If locked, writes to a timestamped filename. */
    public synchronized void saveAndClose() throws IOException {
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Path tmp = file.resolveSibling(file.getFileName().toString() + ".part");
        try (OutputStream os = Files.newOutputStream(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            wb.write(os);
        }
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException locked) {
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