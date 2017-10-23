package fi.vm.yti.codelist.api.domain;

import fi.vm.yti.codelist.common.model.Code;
import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.common.model.CodeScheme;
import fi.vm.yti.codelist.common.model.Meta;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface Domain {

    CodeRegistry getCodeRegistry(final String codeRegistryCodeValue);

    Set<CodeRegistry> getCodeRegistries();

    Set<CodeRegistry> getCodeRegistries(final Integer pageSize,
                                        final Integer from,
                                        final String codeRegistryCodeValue,
                                        final String codeRegistryPrefLabel,
                                        final Date after,
                                        final Meta meta);

    CodeScheme getCodeScheme(final String codeRegistryCodeValue,
                             final String codeSchemeCodeValue);

    Set<CodeScheme> getCodeSchemes();

    Set<CodeScheme> getCodeSchemes(final Integer pageSize,
                                   final Integer from,
                                   final String codeRegistryCodeValue,
                                   final String codeRegistryPrefLabel,
                                   final String codeSchemeCodeValue,
                                   final String codeSchemeCodePrefLabel,
                                   final List<String> statuses,
                                   final Date after,
                                   final Meta meta);

    Code getCode(final String codeRegistryCodeValue,
                 final String codeSchemeCodeValue,
                 final String codeCodeValue);

    Set<Code> getCodes(final Integer pageSize,
                       final Integer from,
                       final String codeRegistryCodeValue,
                       final String codeSchemeCodeValue,
                       final String codeCodeValue,
                       final String prefLabel,
                       final List<String> statuses,
                       final Date after,
                       final Meta meta);
}
