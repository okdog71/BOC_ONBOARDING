package com.grovesoft.boctracker.common.meta;

import static org.assertj.core.api.Assertions.assertThat;

import com.grovesoft.boctracker.access.dto.AccessItemRequest;
import com.grovesoft.boctracker.access.dto.AccessItemStatusRequest;
import com.grovesoft.boctracker.analysiscard.dto.AnalysisCardRequest;
import com.grovesoft.boctracker.analysiscard.dto.AnalysisCardStatusRequest;
import com.grovesoft.boctracker.catchup.dto.CatchupActionRequest;
import com.grovesoft.boctracker.catchup.dto.CatchupActionStatusRequest;
import com.grovesoft.boctracker.common.validation.EnumValue;
import com.grovesoft.boctracker.docreliability.dto.DocReliabilityRequest;
import com.grovesoft.boctracker.handover.dto.HandoverSessionRequest;
import com.grovesoft.boctracker.handover.dto.HandoverSessionStatusRequest;
import com.grovesoft.boctracker.interfacelink.dto.InterfaceLinkRequest;
import com.grovesoft.boctracker.issue.dto.IssueQueueRequest;
import com.grovesoft.boctracker.issue.dto.IssueQueueStatusRequest;
import com.grovesoft.boctracker.menu.dto.MenuInventoryRequest;
import com.grovesoft.boctracker.menu.dto.MenuInventoryStatusRequest;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentRequest;
import com.grovesoft.boctracker.raci.dto.RaciAssignmentStatusRequest;
import com.grovesoft.boctracker.review.dto.ReviewLogRequest;
import com.grovesoft.boctracker.review.dto.ReviewLogStatusRequest;
import com.grovesoft.boctracker.handoverquestion.dto.HandoverQuestionRequest;
import com.grovesoft.boctracker.handoverquestion.dto.HandoverQuestionStatusRequest;
import com.grovesoft.boctracker.techdebt.dto.TechDebtRequest;
import com.grovesoft.boctracker.techdebt.dto.TechDebtStatusRequest;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocRequest;
import com.grovesoft.boctracker.supplydoc.dto.SupplyDocStatusRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneRequest;
import com.grovesoft.boctracker.milestone.dto.MilestoneStatusRequest;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

/**
 * DTO 의 @EnumValue 리터럴이 CodeValues(단일 출처)와 순서까지 정확히 일치하는지 검증한다.
 * 새 도메인을 추가하면 {@link #bindings()} 에 해당 필드를 한 줄씩 추가할 것.
 */
class CodeValuesDriftTest {

    record Binding(Class<? extends Record> dto, String field, List<String> expected) {
        @Override
        public String toString() {
            return dto.getSimpleName() + "." + field;
        }
    }

    static Stream<Binding> bindings() {
        return Stream.of(
                // Milestone
                new Binding(MilestoneRequest.class, "status", CodeValues.MILESTONE_STATUS),
                new Binding(MilestoneStatusRequest.class, "status", CodeValues.MILESTONE_STATUS),
                // HandoverSession
                new Binding(HandoverSessionRequest.class, "week", CodeValues.WEEK),
                new Binding(HandoverSessionRequest.class, "category", CodeValues.CATEGORY),
                new Binding(HandoverSessionRequest.class, "ktStage", CodeValues.KT_STAGE),
                new Binding(HandoverSessionRequest.class, "status", CodeValues.HANDOVER_STATUS),
                new Binding(HandoverSessionStatusRequest.class, "status", CodeValues.HANDOVER_STATUS),
                // CatchupAction
                new Binding(CatchupActionRequest.class, "timing", CodeValues.CATCHUP_TIMING),
                new Binding(CatchupActionRequest.class, "status", CodeValues.MILESTONE_STATUS),
                new Binding(CatchupActionStatusRequest.class, "status", CodeValues.MILESTONE_STATUS),
                // SupplyDoc
                new Binding(SupplyDocRequest.class, "existence", CodeValues.EXISTENCE),
                new Binding(SupplyDocRequest.class, "freshness", CodeValues.FRESHNESS),
                new Binding(SupplyDocRequest.class, "supplyStatus", CodeValues.SUPPLY_STATUS),
                new Binding(SupplyDocStatusRequest.class, "status", CodeValues.SUPPLY_STATUS),
                // AccessItem
                new Binding(AccessItemRequest.class, "status", CodeValues.ACCESS_STATUS),
                new Binding(AccessItemStatusRequest.class, "status", CodeValues.ACCESS_STATUS),
                // InterfaceLink
                new Binding(InterfaceLinkRequest.class, "devCheck", CodeValues.DEV_CHECK),
                // DocReliability
                new Binding(DocReliabilityRequest.class, "freshness", CodeValues.FRESHNESS),
                new Binding(DocReliabilityRequest.class, "codeFit", CodeValues.CODE_FIT),
                new Binding(DocReliabilityRequest.class, "grade", CodeValues.GRADE),
                // MenuInventory (status 는 analysisStatus)
                new Binding(MenuInventoryRequest.class, "generation", CodeValues.GENERATION),
                new Binding(MenuInventoryRequest.class, "priority", CodeValues.PRIORITY),
                new Binding(MenuInventoryRequest.class, "devCheck", CodeValues.DEV_CHECK),
                new Binding(MenuInventoryRequest.class, "analysisStatus", CodeValues.MILESTONE_STATUS),
                new Binding(MenuInventoryStatusRequest.class, "status", CodeValues.MILESTONE_STATUS),
                // IssueQueue
                new Binding(IssueQueueRequest.class, "type", CodeValues.ISSUE_TYPE),
                new Binding(IssueQueueRequest.class, "status", CodeValues.ISSUE_STATUS),
                new Binding(IssueQueueStatusRequest.class, "status", CodeValues.ISSUE_STATUS),
                // RaciAssignment
                new Binding(RaciAssignmentRequest.class, "complexity", CodeValues.PRIORITY),
                new Binding(RaciAssignmentRequest.class, "scale", CodeValues.PRIORITY),
                new Binding(RaciAssignmentRequest.class, "generation", CodeValues.GENERATION),
                new Binding(RaciAssignmentRequest.class, "status", CodeValues.MILESTONE_STATUS),
                new Binding(RaciAssignmentStatusRequest.class, "status", CodeValues.MILESTONE_STATUS),
                // ReviewLog
                new Binding(ReviewLogRequest.class, "status", CodeValues.MILESTONE_STATUS),
                new Binding(ReviewLogStatusRequest.class, "status", CodeValues.MILESTONE_STATUS),
                // AnalysisCard
                new Binding(AnalysisCardRequest.class, "generation", CodeValues.GENERATION),
                new Binding(AnalysisCardRequest.class, "devCheck", CodeValues.DEV_CHECK),
                new Binding(AnalysisCardRequest.class, "status", CodeValues.MILESTONE_STATUS),
                new Binding(AnalysisCardStatusRequest.class, "status", CodeValues.MILESTONE_STATUS),
                // TechDebt
                new Binding(TechDebtRequest.class, "priority", CodeValues.TECH_DEBT_PRIORITY),
                new Binding(TechDebtRequest.class, "system", CodeValues.TECH_DEBT_SYSTEM),
                new Binding(TechDebtRequest.class, "status", CodeValues.TECH_DEBT_STATUS),
                new Binding(TechDebtStatusRequest.class, "status", CodeValues.TECH_DEBT_STATUS),
                // HandoverQuestion
                new Binding(HandoverQuestionRequest.class, "priority", CodeValues.QUESTION_PRIORITY),
                new Binding(HandoverQuestionRequest.class, "status", CodeValues.QUESTION_STATUS),
                new Binding(HandoverQuestionStatusRequest.class, "status", CodeValues.QUESTION_STATUS));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bindings")
    void enumValue_리터럴이_CodeValues와_정확히_일치한다(Binding b) throws Exception {
        RecordComponent rc = Arrays.stream(b.dto().getRecordComponents())
                .filter(c -> c.getName().equals(b.field()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("필드 없음: " + b));

        // 레코드 컴포넌트의 애너테이션은 필드에 전파된다.
        EnumValue ann = b.dto().getDeclaredField(rc.getName()).getAnnotation(EnumValue.class);
        assertThat(ann).as("%s 에 @EnumValue 가 없음", b).isNotNull();
        assertThat(List.of(ann.allowed()))
                .as("%s 의 @EnumValue 허용값이 CodeValues 와 다름", b)
                .containsExactlyElementsOf(b.expected());
    }

    /** 새 도메인의 @EnumValue 필드를 bindings() 에 등록하는 것을 잊으면 실패한다. */
    @Test
    void 모든_EnumValue_필드가_bindings에_등록되어_있다() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Record.class));

        Set<String> registered = bindings().map(Binding::toString).collect(Collectors.toSet());
        Set<String> found = new TreeSet<>();
        for (BeanDefinition bd : scanner.findCandidateComponents("com.grovesoft.boctracker")) {
            Class<?> c = Class.forName(bd.getBeanClassName());
            if (!c.getPackageName().endsWith(".dto")) {
                continue;
            }
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(EnumValue.class)) {
                    found.add(c.getSimpleName() + "." + f.getName());
                }
            }
        }
        assertThat(found).isNotEmpty();
        assertThat(registered).as("bindings() 에 등록되지 않은 @EnumValue 필드").containsAll(found);
    }

    @Test
    void ktStage_구분자는_CodeValues와_일치한다() throws Exception {
        EnumValue ann = HandoverSessionRequest.class.getDeclaredField("ktStage").getAnnotation(EnumValue.class);
        assertThat(ann.delimiter()).isEqualTo(CodeValues.KT_STAGE_DELIMITER);
    }

    @Test
    void 모든_코드값_그룹은_비어있지_않고_중복이_없다() {
        CodeValues.all().forEach((group, values) -> {
            assertThat(values).as(group).isNotEmpty();
            assertThat(new HashSet<>(values)).as(group + " 중복").hasSameSizeAs(values);
        });
    }

    @Test
    void 메타_API가_노출하는_그룹_키_목록() {
        assertThat(CodeValues.all().keySet()).containsExactly(
                "milestoneStatus", "handoverStatus", "ktStage", "week", "category", "catchupTiming",
                "supplyStatus", "existence", "freshness", "accessStatus", "grade", "codeFit",
                "priority", "generation", "devCheck", "issueType", "issueStatus",
                "techDebtPriority", "techDebtStatus", "techDebtSystem", "questionPriority", "questionStatus");
    }
}
