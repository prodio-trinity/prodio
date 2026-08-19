package com.prodio;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 모듈 간 직접 참조를 금지하는 경계 테스트.
 *
 * Spring Modulith가 런타임에 모듈 경계를 검증하지만,
 * 이 테스트는 빌드 시점에 잘못된 참조를 조기에 잡아준다.
 *
 * 모듈 간 통신은 반드시 도메인 이벤트(@ApplicationModuleListener)를 통해야 한다.
 */
@DisplayName("모듈 경계 검증")
class ModuleBoundaryTest {

    private static final JavaClasses classes =
            new ClassFileImporter().importPackages("com.prodio");

    @Test
    @DisplayName("catalog 모듈은 order/production/stat/infra를 직접 참조할 수 없다")
    void catalog_should_not_depend_on_other_modules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.prodio.catalog..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.prodio.order..",
                        "com.prodio.production..",
                        "com.prodio.stat..",
                        "com.prodio.infra.."
                );
        rule.check(classes);
    }

    @Test
    @DisplayName("order 모듈은 production/stat을 직접 참조할 수 없다")
    void order_should_not_depend_on_production_or_stat() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.prodio.order..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.prodio.production..",
                        "com.prodio.stat.."
                );
        rule.check(classes);
    }

    @Test
    @DisplayName("stat 모듈은 다른 도메인 모듈의 내부 패키지를 직접 참조할 수 없다"
            + " (order/production이 공개한 이벤트 패키지는 예외)")
    void stat_should_not_depend_on_domain_modules() {
        DescribedPredicate<JavaClass> forbidden = JavaClass.Predicates.resideInAnyPackage(
                        "com.prodio.catalog..", "com.prodio.order..", "com.prodio.production..")
                .and(DescribedPredicate.not(JavaClass.Predicates.resideInAnyPackage(
                        "com.prodio.order", "com.prodio.production.event")));

        ArchRule rule = noClasses()
                .that().resideInAPackage("com.prodio.stat..")
                .should().dependOnClassesThat(forbidden);
        rule.check(classes);
    }

    @Test
    @DisplayName("도메인 레이어는 presentation 레이어를 참조할 수 없다")
    void domain_should_not_depend_on_presentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..presentation..")
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    @DisplayName("domain 레이어는 infrastructure 레이어를 참조할 수 없다")
    void domain_should_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .allowEmptyShould(true);
        rule.check(classes);
    }
}
