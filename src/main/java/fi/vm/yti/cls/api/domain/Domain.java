package fi.vm.yti.cls.api.domain;

import fi.vm.yti.cls.common.model.BusinessId;
import fi.vm.yti.cls.common.model.BusinessServiceSubRegion;
import fi.vm.yti.cls.common.model.ElectoralDistrict;
import fi.vm.yti.cls.common.model.HealthCareDistrict;
import fi.vm.yti.cls.common.model.Magistrate;
import fi.vm.yti.cls.common.model.MagistrateServiceUnit;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Municipality;
import fi.vm.yti.cls.common.model.PostManagementDistrict;
import fi.vm.yti.cls.common.model.PostalCode;
import fi.vm.yti.cls.common.model.Region;
import fi.vm.yti.cls.common.model.Register;
import fi.vm.yti.cls.common.model.RegisterItem;
import fi.vm.yti.cls.common.model.StreetAddress;
import fi.vm.yti.cls.common.model.StreetNumber;

import java.util.Date;
import java.util.List;
import java.util.Set;


public interface Domain {

    Set<Register> getRegisters(final Integer pageSize,
                               final Integer from,
                               final String registerType,
                               final String registerName,
                               final Date after,
                               final Meta meta);

    Set<String> getRegisterTypes();


    RegisterItem getRegisterItem(final String registerCode,
                                 final String registerItemCode);

    List<RegisterItem> getRegisterItems(final Integer pageSize,
                                        final Integer from,
                                        final String registerCode,
                                        final String registerItemCode,
                                        final Date after,
                                        final Meta meta);


    PostalCode getPostalCode(final String code);

    PostalCode getPostalCodeWithId(final String id);

    List<PostalCode> getPostalCodes(final Integer pageSize,
                                    final Integer from,
                                    final String code,
                                    final String name,
                                    final Integer type,
                                    final String areaCode,
                                    final String areaName,
                                    final String municipalityCode,
                                    final String municipalityName,
                                    final Date after,
                                    final Meta meta);


    PostManagementDistrict getPostManagementDistrict(final String code);

    PostManagementDistrict getPostManagementDistrictWithId(final String id);

    List<PostalCode> getPostManagementDistrictPostalCodes(final Integer pageSize,
                                                          final Integer from,
                                                          final Date after,
                                                          final String code,
                                                          final Meta meta);

    List<PostManagementDistrict> getPostManagementDistricts(final Integer pageSize,
                                                            final Integer from,
                                                            final String code,
                                                            final String name,
                                                            final Date after,
                                                            final Meta meta);


    Municipality getMunicipality(final String code);

    Municipality getMunicipalityWithId(final String id);

    List<Municipality> getMunicipalities(final Integer pageSize,
                                         final Integer from,
                                         final String code,
                                         final String name,
                                         final Date after,
                                         final Meta meta);


    Magistrate getMagistrate(final String code);

    Magistrate getMagistrateWithId(final String id);

    List<Municipality> getMagistrateMunicipalities(final Integer pageSize,
                                                   final Integer from,
                                                   final Date after,
                                                   final String code,
                                                   final String municipalityName,
                                                   final String municipalityCode,
                                                   final Meta meta);

    List<Magistrate> getMagistrates(final Integer pageSize,
                                    final Integer from,
                                    final String code,
                                    final String name,
                                    final Date after,
                                    final Meta meta);


    Region getRegion(final String code);

    Region getRegionWithId(final String id);

    List<Municipality> getRegionMunicipalities(final Integer pageSize,
                                               final Integer from,
                                               final Date after,
                                               final String code,
                                               final String municipalityName,
                                               final String municipalityCode,
                                               final Meta meta);

    List<Region> getRegions(final Integer pageSize,
                            final Integer from,
                            final String code,
                            final String name,
                            final Date after,
                            final Meta meta);


    StreetNumber getStreetNumberWithId(final String id);


    StreetAddress getStreetAddressWithId(final String id);

    StreetAddress getStreetAddressWithMunicipalityAndStreetName(final String municipalityCode,
                                                                final String streetName);

    StreetNumber getStreetAddressWithMunicipalityAndStreetNameAndNumber(final String municipalityCode,
                                                                        final String streetName,
                                                                        final int streetNumber);

    List<Municipality> getStreetAddressMunicipalities(final Integer pageSize,
                                                      final Integer from,
                                                      final Date after,
                                                      final String code,
                                                      final String municipalityName,
                                                      final String municipalityCode,
                                                      final Meta meta);

    List<StreetAddress> getStreetAddressesWithMunicipality(final Integer pageSize,
                                                           final Integer from,
                                                           final String name,
                                                           final String municipalityCode,
                                                           final Date after,
                                                           final Meta meta);

    List<StreetAddress> getStreetAddresses(final Integer pageSize,
                                           final Integer from,
                                           final String name,
                                           final Date after,
                                           final Meta meta);


    MagistrateServiceUnit getMagistrateServiceUnit(final String code);

    MagistrateServiceUnit getMagistrateServiceUnitWithId(final String id);

    List<Municipality> getMagistrateServiceUnitMunicipalities(final Integer pageSize,
                                                              final Integer from,
                                                              final Date after,
                                                              final String code,
                                                              final String municipalityName,
                                                              final String municipalityCode,
                                                              final Meta meta);

    List<MagistrateServiceUnit> getMagistrateServiceUnits(final Integer pageSize,
                                                          final Integer from,
                                                          final String code,
                                                          final String name,
                                                          final Date after,
                                                          final Meta meta);


    ElectoralDistrict getElectoralDistrict(final String code);

    ElectoralDistrict getElectoralDistrictWithId(final String id);

    List<Municipality> getElectoralDistrictMunicipalities(final Integer pageSize,
                                                          final Integer from,
                                                          final Date after,
                                                          final String code,
                                                          final String municipalityName,
                                                          final String municipalityCode,
                                                          final Meta meta);

    List<ElectoralDistrict> getElectoralDistricts(final Integer pageSize,
                                                  final Integer from,
                                                  final String code,
                                                  final String name,
                                                  final Date after,
                                                  final Meta meta);


    HealthCareDistrict getHealthCareDistrict(final String code);

    HealthCareDistrict getHealthCareDistrictWithId(final String id);

    List<Municipality> getHealthCareDistrictMunicipalities(final Integer pageSize,
                                                           final Integer from,
                                                           final Date after,
                                                           final String code,
                                                           final String municipalityName,
                                                           final String municipalityCode,
                                                           final Meta meta);

    List<HealthCareDistrict> getHealthCareDistricts(final Integer pageSize,
                                                    final Integer from,
                                                    final String code,
                                                    final String name,
                                                    final Date after,
                                                    final Meta meta);


    BusinessServiceSubRegion getBusinessServiceSubRegion(final String code);

    BusinessServiceSubRegion getBusinessServiceSubRegionWithId(final String id);

    List<Municipality> getBusinessServiceSubRegionMunicipalities(final Integer pageSize,
                                                                 final Integer from,
                                                                 final Date after,
                                                                 final String code,
                                                                 final String municipalityName,
                                                                 final String municipalityCode,
                                                                 final Meta meta);

    List<BusinessServiceSubRegion> getBusinessServiceSubRegions(final Integer pageSize,
                                                                final Integer from,
                                                                final String code,
                                                                final String name,
                                                                final Date after,
                                                                final Meta meta);


    BusinessId getBusinessId(final String code);

    BusinessId getBusinessIdWithId(final String id);

    List<BusinessId> getBusinessIds(final Integer pageSize,
                                    final Integer from,
                                    final String code,
                                    final String name,
                                    final Date after,
                                    final Meta meta);

}
