package com.grovesoft.boctracker.seed;

import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * seed 파일 1개 ↔ 도메인 1개 매핑.
 *
 * @param domain      로그/결과 키로 쓰는 도메인명
 * @param file        seed 디렉터리 기준 파일명
 * @param requestType JSON 배열의 원소를 역직렬화할 Request DTO
 * @param counter     해당 도메인 테이블의 현재 행 수(repository.count())
 * @param creator     한 건 저장(service.create)
 */
public record SeedTarget<R>(String domain, String file, Class<R> requestType,
                            LongSupplier counter, Consumer<R> creator) {
}
