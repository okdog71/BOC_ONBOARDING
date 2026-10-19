package com.grovesoft.boctracker.common.attachment;

/** 첨부 파일을 가질 수 있는 항목의 종류와 다운로드 경로(/api/{path}/{id}/files/{fileId}). */
public enum AttachmentOwner {
    TECH_DEBT("tech-debts"),
    HANDOVER_QUESTION("handover-questions");

    private final String path;

    AttachmentOwner(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
