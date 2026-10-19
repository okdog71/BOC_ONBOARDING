package com.grovesoft.boctracker.common.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 시트 1장짜리 .xlsx 를 외부 라이브러리 없이 만든다(머리글 굵게·고정, 자동 필터, 줄바꿈, 열 너비).
 * 모든 값은 문자열(inlineStr)이라 '=' 로 시작해도 수식으로 실행되지 않는다.
 */
public final class XlsxWriter {

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";

    private XlsxWriter() {
    }

    /**
     * @param sheetName 시트 이름(31자·금지 문자 정리)
     * @param headers   머리글
     * @param widths    열 너비(문자 수). headers 와 같은 길이
     * @param rows      각 행의 값(null 허용). 열 수는 headers 와 같아야 한다
     */
    public static byte[] write(String sheetName, List<String> headers, List<Integer> widths, List<List<String>> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml", XML
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>");
            put(zip, "_rels/.rels", XML
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            put(zip, "xl/workbook.xml", XML
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"" + esc(cleanSheetName(sheetName)) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            put(zip, "xl/_rels/workbook.xml.rels", XML
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>");
            // 스타일 0=기본(줄바꿈·위쪽 정렬), 1=머리글(굵게·회색 바탕·아래 테두리)
            put(zip, "xl/styles.xml", XML
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                    + "<fonts count=\"2\"><font><sz val=\"10\"/><name val=\"Malgun Gothic\"/></font>"
                    + "<font><b/><sz val=\"10\"/><name val=\"Malgun Gothic\"/></font></fonts>"
                    + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>"
                    + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFE8EDF7\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
                    + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>"
                    + "<border><left/><right/><top/><bottom style=\"thin\"><color rgb=\"FF8899BB\"/></bottom><diagonal/></border></borders>"
                    + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                    + "<cellXfs count=\"2\">"
                    + "<xf numFmtId=\"49\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyAlignment=\"1\"><alignment vertical=\"top\" wrapText=\"1\"/></xf>"
                    + "<xf numFmtId=\"49\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf>"
                    + "</cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>");
            put(zip, "xl/worksheets/sheet1.xml", sheet(headers, widths, rows));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static String sheet(List<String> headers, List<Integer> widths, List<List<String>> rows) {
        int cols = headers.size();
        StringBuilder sb = new StringBuilder(XML);
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sb.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
        sb.append("<sheetFormatPr defaultRowHeight=\"15\"/><cols>");
        for (int i = 0; i < cols; i++) {
            sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1).append("\" width=\"")
                    .append(widths.get(i)).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols><sheetData>");
        appendRow(sb, 1, headers, 1);
        for (int r = 0; r < rows.size(); r++) {
            appendRow(sb, r + 2, rows.get(r), 0);
        }
        sb.append("</sheetData>");
        sb.append("<autoFilter ref=\"A1:").append(colName(cols - 1)).append(Math.max(1, rows.size() + 1)).append("\"/>");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, int rowNo, List<String> values, int style) {
        sb.append("<row r=\"").append(rowNo).append("\">");
        for (int c = 0; c < values.size(); c++) {
            String v = values.get(c);
            if (v == null || v.isEmpty()) {
                continue;
            }
            sb.append("<c r=\"").append(colName(c)).append(rowNo).append("\" s=\"").append(style)
                    .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">").append(esc(v)).append("</t></is></c>");
        }
        sb.append("</row>");
    }

    static String colName(int index) {
        StringBuilder s = new StringBuilder();
        for (int i = index; i >= 0; i = i / 26 - 1) {
            s.insert(0, (char) ('A' + i % 26));
        }
        return s.toString();
    }

    private static String cleanSheetName(String n) {
        String s = n.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        if (s.isEmpty()) {
            s = "Sheet1";
        }
        return s.length() > 31 ? s.substring(0, 31) : s;
    }

    /** XML 특수문자 이스케이프 + XML 에서 허용되지 않는 제어문자 제거 */
    static String esc(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> {
                    if (ch == '\n' || ch == '\t' || ch == '\r' || ch >= 0x20) {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
