package com.grovesoft.boctracker.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** 내보낸 .xlsx 에서 첫 시트의 XML 을 꺼낸다(내용 검증용). */
public final class XlsxTestUtil {

    private XlsxTestUtil() {
    }

    public static String sheetXml(byte[] xlsx) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(xlsx), StandardCharsets.UTF_8)) {
            for (ZipEntry e = zip.getNextEntry(); e != null; e = zip.getNextEntry()) {
                if (e.getName().equals("xl/worksheets/sheet1.xml")) {
                    return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new AssertionError("sheet1.xml 없음");
    }
}
