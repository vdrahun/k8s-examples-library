package com.xpinjection.library;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.xpinjection.library.config.LibrarySettings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.xpinjection.library",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class, ImportOption.DoNotIncludeArchives.class}
)
public class HexagonalDesignRules {
    static final String DOMAIN = "domain";
    static final String SERVICE = "service";
    static final String SERVICE_IMPL = "service-impl";

    static final String ADAPTOR_API = "api-adapter";
    static final String ADAPTOR_UI = "ui-adapter";
    static final String ADAPTOR_PERSISTENCE = "persistence-adapter";

    static final String CONFIG = "config";

    @ArchTest
    ArchRule packages_should_follow_hexagonal_design = layeredArchitecture()
            .as("Packages structure should match hexagonal design rules")
            .layer(DOMAIN).definedBy("..domain..")
            .layer(SERVICE).definedBy("..service", "..service.dto..", "..service.exception..")
            .layer(SERVICE_IMPL).definedBy("..service.impl..")
            .layer(ADAPTOR_API).definedBy("..adaptors.api..")
            .layer(ADAPTOR_UI).definedBy("..adaptors.ui..")
            .layer(ADAPTOR_PERSISTENCE).definedBy("..adaptors.persistence..")
            .layer(CONFIG).definedBy("..config..")
            .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(SERVICE_IMPL, ADAPTOR_PERSISTENCE)
            .whereLayer(SERVICE).mayOnlyBeAccessedByLayers(SERVICE_IMPL, ADAPTOR_UI, ADAPTOR_API, CONFIG)
            .whereLayer(SERVICE_IMPL).mayNotBeAccessedByAnyLayer()
            .whereLayer(ADAPTOR_API).mayNotBeAccessedByAnyLayer()
            .whereLayer(ADAPTOR_UI).mayNotBeAccessedByAnyLayer()
            .whereLayer(ADAPTOR_PERSISTENCE).mayOnlyBeAccessedByLayers(SERVICE_IMPL)
            .whereLayer(CONFIG).mayNotBeAccessedByAnyLayer()
            .ignoreDependency(LibraryApplication.class, LibrarySettings.class);

    @ArchTest
    ArchRule entities_should_be_located_in_domain_or_persistence_adaptor =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().resideInAnyPackage("..domain..", "..adaptors.persistence.entity..")
                    .as("Entities should reside in a domain or persistence entity packages");

    @ArchTest
    ArchRule dao_should_be_located_in_persistence_adaptor =
            classes().that().areInterfaces().and().areAssignableTo(CrudRepository.class)
                    .should().resideInAPackage("..adaptors.persistence")
                    .andShould().haveNameMatching(".*Dao")
                    .as("Repositories should reside in a persistence adaptor package and have corresponding name suffix");

    @ArchTest
    ArchRule rest_controllers_should_be_located_in_api_adaptor =
            classes().that().areAnnotatedWith(RestController.class)
                    .should().resideInAPackage("..adaptors.api")
                    .andShould().haveNameMatching(".*RestController")
                    .as("Rest controllers should reside in a api adaptor package and have corresponding name suffix");

    @ArchTest
    ArchRule controllers_should_be_located_in_ui_adaptor =
            classes().that().areAnnotatedWith(Controller.class)
                    .should().resideInAPackage("..adaptors.ui")
                    .andShould().haveNameMatching(".*Controller")
                    .as("Controllers should reside in a ui adaptor package and have corresponding name suffix");

    @ArchTest
    ArchRule service_implementations_should_be_located_in_service_impl =
            classes().that().areAnnotatedWith(Service.class)
                    .should().resideInAPackage("..service.impl")
                    .andShould().haveNameMatching(".*ServiceImpl")
                    .as("Service implementations should reside in a service impl package and have corresponding name suffix");

    @ArchTest
    ArchRule services_should_be_located_in_service =
            classes().that().resideInAPackage("..service")
                    .should().beInterfaces()
                    .andShould().haveNameMatching(".*Service")
                    .as("Services should reside in a service package and have corresponding name suffix");
}
