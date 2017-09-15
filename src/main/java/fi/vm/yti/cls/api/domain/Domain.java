package fi.vm.yti.cls.api.domain;

import fi.vm.yti.cls.common.model.Code;
import fi.vm.yti.cls.common.model.CodeRegistry;
import fi.vm.yti.cls.common.model.CodeScheme;
import fi.vm.yti.cls.common.model.Meta;

import java.util.Date;
import java.util.Set;

public interface Domain {

    CodeRegistry getCodeRegistry(final String codeRegistryCodeValue,
                                 final Boolean useId);

    Set<CodeRegistry> getCodeRegistries(final Integer pageSize,
                                        final Integer from,
                                        final String codeRegistryCodeValue,
                                        final String codeRegistryPrefLabel,
                                        final Date after,
                                        final Meta meta);

    CodeScheme getCodeScheme(final String codeRegistryCodeValue,
                             final String codeSchemeCodeValue,
                             final Boolean useId);

    Set<CodeScheme> getCodeSchemes(final Integer pageSize,
                                   final Integer from,
                                   final String codeRegistryCodeValue,
                                   final String codeRegistryPrefLabel,
                                   final String codeSchemeCodeValue,
                                   final String codeSchemeCodePrefLabel,
                                   final Date after,
                                   final Meta meta);

    Code getCode(final String codeRegistryCodeValue,
                 final String codeSchemeCodeValue,
                 final String codeCodeValue,
                 final Boolean useId);

    Set<Code> getCodes(final Integer pageSize,
                       final Integer from,
                       final String codeRegistryCodeValue,
                       final String codeSchemeCodeValue,
                       final String codeCodeValue,
                       final String prefLabel,
                       final Date after,
                       final Meta meta);

}
