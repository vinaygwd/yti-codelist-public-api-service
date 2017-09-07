package fi.vm.yti.cls.api.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import fi.vm.yti.cls.common.model.BusinessId;
import fi.vm.yti.cls.common.model.BusinessServiceSubRegion;
import fi.vm.yti.cls.common.model.Code;
import fi.vm.yti.cls.common.model.CodeRegistry;
import fi.vm.yti.cls.common.model.CodeScheme;
import fi.vm.yti.cls.common.model.ElectoralDistrict;
import fi.vm.yti.cls.common.model.HealthCareDistrict;
import fi.vm.yti.cls.common.model.Magistrate;
import fi.vm.yti.cls.common.model.MagistrateServiceUnit;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Municipality;
import fi.vm.yti.cls.common.model.PostManagementDistrict;
import fi.vm.yti.cls.common.model.PostalCode;
import fi.vm.yti.cls.common.model.Region;
import fi.vm.yti.cls.common.model.StreetAddress;
import fi.vm.yti.cls.common.model.StreetNumber;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.toIntExact;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;


@Singleton
@Service
public class DomainImpl implements Domain {

    private static final Logger LOG = LoggerFactory.getLogger(DomainImpl.class);

    private Client m_client;

    private static int MAX_SIZE = 10000;


    @Autowired
    private DomainImpl(final Client client) {
        m_client = client;
    }


    public Set<CodeRegistry> getCodeRegistries(final Integer pageSize,
                                               final Integer from,
                                               final String codeRegistryCodeValue,
                                               final String codeRegistryPrefLabel,
                                               final Date after,
                                               final Meta meta) {

        final Set<CodeRegistry> codeRegistries = new HashSet<>();

        final boolean exists = m_client.admin().indices().prepareExists(DomainConstants.ELASTIC_INDEX_CODEREGISTRIES).execute().actionGet().isExists();

        if (exists) {

            final ObjectMapper mapper = new ObjectMapper();

            final SearchRequestBuilder searchRequest = m_client
                    .prepareSearch(DomainConstants.ELASTIC_INDEX_CODEREGISTRIES)
                    .setTypes(DomainConstants.ELASTIC_TYPE_CODEREGISTRY)
                    .addSort("codeValue.keyword", SortOrder.ASC)
                    .setSize(pageSize != null ? pageSize : MAX_SIZE)
                    .setFrom(from != null ? from : 0);

            final BoolQueryBuilder builder = constructSearchQuery(codeRegistryCodeValue, codeRegistryPrefLabel, after);
            searchRequest.setQuery(builder);

            final SearchResponse response = searchRequest.execute().actionGet();
            setResultCounts(meta, response);

            Arrays.stream(response.getHits().hits()).forEach(hit -> {
                try {
                    final CodeRegistry codeRegistry = mapper.readValue(hit.getSourceAsString(), CodeRegistry.class);
                    codeRegistries.add(codeRegistry);
                } catch (IOException e) {
                    LOG.error("getCodeRegistries reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
                }
            });

        }

        return codeRegistries;

    }


    public Set<CodeScheme> getCodeSchemes(final Integer pageSize,
                                          final Integer from,
                                          final String codeRegistryCodeValue,
                                          final String codeSchemeCodeValue,
                                          final String codeSchemePrefLabel,
                                          final String codeSchemeType,
                                          final Date after,
                                          final Meta meta) {

        final Set<CodeScheme> codeSchemes = new HashSet<>();

        final boolean exists = m_client.admin().indices().prepareExists(DomainConstants.ELASTIC_INDEX_CODESCHEMES).execute().actionGet().isExists();

        if (exists) {

            final ObjectMapper mapper = new ObjectMapper();

            final SearchRequestBuilder searchRequest = m_client
                    .prepareSearch(DomainConstants.ELASTIC_INDEX_CODESCHEMES)
                    .setTypes(DomainConstants.ELASTIC_TYPE_CODESCHEME)
                    .addSort("codeValue.keyword", SortOrder.ASC)
                    .setSize(pageSize != null ? pageSize : MAX_SIZE)
                    .setFrom(from != null ? from : 0);

            final BoolQueryBuilder builder = constructSearchQuery(codeSchemeCodeValue, codeSchemePrefLabel, after);
            builder.must(QueryBuilders.matchQuery("codeRegistry.codeValue.keyword", codeRegistryCodeValue.toLowerCase()));
            searchRequest.setQuery(builder);

            final SearchResponse response = searchRequest.execute().actionGet();
            setResultCounts(meta, response);

            Arrays.stream(response.getHits().hits()).forEach(hit -> {
                try {
                    final CodeScheme codeScheme = mapper.readValue(hit.getSourceAsString(), CodeScheme.class);
                    codeSchemes.add(codeScheme);
                } catch (IOException e) {
                    LOG.error("getCodeSchemes reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
                }
            });

        }

        return codeSchemes;

    }



    public Set<String> getCodeSchemeTypes() {

        final Set<String> types = new HashSet<>();

        final boolean exists = m_client.admin().indices().prepareExists(DomainConstants.ELASTIC_INDEX_CODES).execute().actionGet().isExists();

        if (exists) {
            final AggregationBuilder aggregation = AggregationBuilders.terms("typesAgg").field("_type").size(1000).minDocCount(0);

            final SearchRequestBuilder searchRequest = m_client
                    .prepareSearch(DomainConstants.ELASTIC_INDEX_CODES)
                    .addAggregation(aggregation)
                    .setSize(0);

            final SearchResponse response = searchRequest.execute().actionGet();

            final Terms agg = response.getAggregations().get("typesAgg");

            for (final Terms.Bucket entry : agg.getBuckets()) {
                final String key = entry.getKeyAsString();
                types.add(key);
            }
        }

        return types;

    }


    public Code getCode(final String codeRegistryCodeValue,
                        final String codeSchemeCodeValue,
                        final String codeCodeValue) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CODES)
                .setTypes(codeRegistryCodeValue + "_" + codeSchemeCodeValue)
                .setQuery(QueryBuilders.termQuery("codeValue", codeCodeValue.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getCode found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Code code = mapper.readValue(hit.getSourceAsString(), Code.class);
                    return code;
                }
            } catch (IOException e) {
                LOG.error("getCode reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Code> getCodes(final Integer pageSize,
                               final Integer from,
                               final String codeRegistryCodeValue,
                               final String codeSchemeCodeValue,
                               final String codeCodeValue,
                               final String prefLabel,
                               final Date after,
                               final Meta meta) {

        final boolean exists = m_client.admin().indices().prepareExists(DomainConstants.ELASTIC_INDEX_CODES).execute().actionGet().isExists();

        if (exists) {

            final ObjectMapper mapper = new ObjectMapper();

            final List<Code> codes = new ArrayList<>();

            final SearchRequestBuilder searchRequest = m_client
                    .prepareSearch(DomainConstants.ELASTIC_INDEX_CODES)
                    .setTypes(codeRegistryCodeValue + "_" + codeSchemeCodeValue)
                    .addSort("codeValue.keyword", SortOrder.ASC)
                    .setSize(pageSize != null ? pageSize : MAX_SIZE)
                    .setFrom(from != null ? from : 0);

            final BoolQueryBuilder builder = constructSearchQuery(codeCodeValue, prefLabel, after);
            searchRequest.setQuery(builder);

            final SearchResponse response = searchRequest.execute().actionGet();
            setResultCounts(meta, response);

            Arrays.stream(response.getHits().hits()).forEach(hit -> {
                try {
                    final Code code = mapper.readValue(hit.getSourceAsString(), Code.class);
                    codes.add(code);
                } catch (IOException e) {
                    LOG.error("getCodes reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
                }
            });

            return codes;
        }

        return null;

    }



    public PostalCode getPostalCode(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTALCODE)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getPostalCode found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final PostalCode postalCode = mapper.readValue(hit.getSourceAsString(), PostalCode.class);
                    return postalCode;
                }
            } catch (IOException e) {
                LOG.error("getPostalCode reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public PostalCode getPostalCodeWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTALCODE)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getPostalCodeWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final PostalCode postalCode = mapper.readValue(hit.getSourceAsString(), PostalCode.class);
                    return postalCode;
                }
            } catch (IOException e) {
                LOG.error("getPostalCodeWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<PostalCode> getPostalCodes(final Integer pageSize,
                                           final Integer from,
                                           final String codeValue,
                                           final String prefLabel,
                                           final Integer codeType,
                                           final String areaCode,
                                           final String areaName,
                                           final String municipalityCode,
                                           final String municipalityName,
                                           final Date after,
                                           final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<PostalCode> postalCodes = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTALCODE)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final PostalCode postalCode = mapper.readValue(hit.getSourceAsString(), PostalCode.class);
                postalCodes.add(postalCode);
            } catch (IOException e) {
                LOG.error("getPostalCodes reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return postalCodes;

    }


    public Municipality getMunicipality(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getMunicipality found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                    return municipality;
                }
            } catch (IOException e) {
                LOG.error("getMunicipality reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public Municipality getMunicipalityWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getMunicipalityWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                    return municipality;
                }
            } catch (IOException e) {
                LOG.error("getMunicipalityWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getMunicipalities(final Integer pageSize,
                                                final Integer from,
                                                final String codeValue,
                                                final String prefLabel,
                                                final Date after,
                                                final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }


    public PostManagementDistrict getPostManagementDistrict(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTMANAGEMENTDISTRICT)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getPostManagementDistrict found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final PostManagementDistrict postManagementDistrict = mapper.readValue(hit.getSourceAsString(), PostManagementDistrict.class);
                    return postManagementDistrict;
                }
            } catch (IOException e) {
                LOG.error("getPostManagementDistrict reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public PostManagementDistrict getPostManagementDistrictWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTMANAGEMENTDISTRICT)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getPostManagementDistrictWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final PostManagementDistrict postManagementDistrict = mapper.readValue(hit.getSourceAsString(), PostManagementDistrict.class);
                    return postManagementDistrict;
                }
            } catch (IOException e) {
                LOG.error("getPostManagementDistrictWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }


    public List<PostalCode> getPostManagementDistrictPostalCodes(final Integer pageSize,
                                                                 final Integer from,
                                                                 final Date after,
                                                                 final String codeValue,
                                                                 final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<PostalCode> postalCodes = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTALCODE)
                .setQuery(QueryBuilders.termQuery("postManagementDistrict.codeValue", codeValue.toLowerCase()))
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        addDateFiltersToRequest(searchRequest, after);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final PostalCode postalCode = mapper.readValue(hit.getSourceAsString(), PostalCode.class);
                postalCodes.add(postalCode);
            } catch (IOException e) {
                LOG.error("getPostManagementDistrictPostalCodes reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return postalCodes;

    }

    public List<PostManagementDistrict> getPostManagementDistricts(final Integer pageSize,
                                                                   final Integer from,
                                                                   final String codeValue,
                                                                   final String prefLabel,
                                                                   final Date after,
                                                                   final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<PostManagementDistrict> postManagementDistricts = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_POSTMANAGEMENTDISTRICT)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final PostManagementDistrict postManagementDistrict = mapper.readValue(hit.getSourceAsString(), PostManagementDistrict.class);
                postManagementDistricts.add(postManagementDistrict);
            } catch (IOException e) {
                LOG.error("getPostManagementDistricts reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return postManagementDistricts;

    }

    public Magistrate getMagistrate(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MAGISTRATE)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getMagistrate found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Magistrate magistrate = mapper.readValue(hit.getSourceAsString(), Magistrate.class);
                    return magistrate;
                }
            } catch (IOException e) {
                LOG.error("getMagistrate reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public Magistrate getMagistrateWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MAGISTRATE)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getMagistrateWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Magistrate magistrate = mapper.readValue(hit.getSourceAsString(), Magistrate.class);
                    return magistrate;
                }
            } catch (IOException e) {
                LOG.error("getMagistrateWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getMagistrateMunicipalities(final Integer pageSize,
                                                          final Integer from,
                                                          final Date after,
                                                          final String codeValue,
                                                          final String municipalityCode,
                                                          final String municipalityName,
                                                          final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("magistrate.codeValue.keyword", codeValue.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getMagistrateMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }

    public List<Magistrate> getMagistrates(final Integer pageSize,
                                           final Integer from,
                                           final String codeValue,
                                           final String prefLabel,
                                           final Date after,
                                           final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Magistrate> magistrates = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MAGISTRATE)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Magistrate magistrate = mapper.readValue(hit.getSourceAsString(), Magistrate.class);
                magistrates.add(magistrate);
            } catch (IOException e) {
                LOG.error("getMagistrates reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return magistrates;

    }


    public Region getRegion(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_REGION)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getRegion found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Region region = mapper.readValue(hit.getSourceAsString(), Region.class);
                    return region;
                }
            } catch (IOException e) {
                LOG.error("getRegion reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public Region getRegionWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_REGION)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getRegionWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final Region region = mapper.readValue(hit.getSourceAsString(), Region.class);
                    return region;
                }
            } catch (IOException e) {
                LOG.error("getRegionWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getRegionMunicipalities(final Integer pageSize,
                                                      final Integer from,
                                                      final Date after,
                                                      final String codeValue,
                                                      final String municipalityCode,
                                                      final String municipalityName,
                                                      final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("region.codeValue", codeValue.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getRegionMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }

    public List<Region> getRegions(final Integer pageSize,
                                   final Integer from,
                                   final String codeValue,
                                   final String prefLabel,
                                   final Date after,
                                   final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Region> regions = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_REGION)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Region region = mapper.readValue(hit.getSourceAsString(), Region.class);
                regions.add(region);
            } catch (IOException e) {
                LOG.error("getRegions reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return regions;

    }


    public StreetNumber getStreetNumberWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_STREETADDRESS)
                .setQuery(QueryBuilders.termQuery("streetNumbers.id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getStreetNumberWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final StreetAddress streetAddress = mapper.readValue(hit.getSourceAsString(), StreetAddress.class);
                    final List<StreetNumber> streetNumbers = streetAddress.getStreetNumbers();
                    for (final StreetNumber streetNumber : streetNumbers) {
                        if (streetNumber.getId().equalsIgnoreCase(id)) {
                            streetAddress.setStreetNumbers(null);
                            streetNumber.setStreetAddress(streetAddress);
                            return streetNumber;
                        }
                    }
                    return null;
                }
            } catch (IOException e) {
                LOG.error("getStreetNumberWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }


    public StreetAddress getStreetAddressWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_STREETADDRESS)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getStreetAddressWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final StreetAddress streetAddress = mapper.readValue(hit.getSourceAsString(), StreetAddress.class);
                    return streetAddress;
                }
            } catch (IOException e) {
                LOG.error("getStreetAddressWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public StreetAddress getStreetAddressWithMunicipalityAndStreetName(final String municipalityCode,
                                                                       final String streetName) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_STREETADDRESS);

        final BoolQueryBuilder builder = boolQuery();

        if (municipalityCode != null) {
            builder.must(QueryBuilders.prefixQuery("municipality.codeValue.keyword", municipalityCode.toLowerCase()));
        }

        if (streetName != null) {
            builder.must(QueryBuilders.nestedQuery("names", QueryBuilders.multiMatchQuery(streetName.toLowerCase() + "*", "names.fi", "names.se").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX), ScoreMode.None));
        }

        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getStreetAddressWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final StreetAddress streetAddress = mapper.readValue(hit.getSourceAsString(), StreetAddress.class);
                    return streetAddress;
                }
            } catch (IOException e) {
                LOG.error("getStreetAddressWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public StreetNumber getStreetAddressWithMunicipalityAndStreetNameAndNumber(final String municipalityCode,
                                                                               final String streetName,
                                                                               final int number) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_STREETADDRESS);

        final BoolQueryBuilder builder = boolQuery();

        if (municipalityCode != null) {
            builder.must(QueryBuilders.prefixQuery("municipality.codeValue.keyword", municipalityCode.toLowerCase()));
        }

        if (streetName != null) {
            builder.must(QueryBuilders.nestedQuery("names", QueryBuilders.multiMatchQuery(streetName.toLowerCase() + "*", "names.fi", "names.se").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX), ScoreMode.None));
        }

        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getStreetAddressWithMunicipalityAndStreetNameAndNumber found: " + response.getHits().getTotalHits() + " hits.");

        StreetNumber streetNumber = null;

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final StreetAddress streetAddress = mapper.readValue(hit.getSourceAsString(), StreetAddress.class);
                    final List<StreetNumber> streetNumbers = streetAddress.getStreetNumbers();
                    for (final StreetNumber sn : streetNumbers) {
                        if (sn.hasNumber(number)) {
                            streetAddress.setStreetNumbers(null);
                            sn.setStreetAddress(streetAddress);
                            streetNumber = sn;
                            break;
                        }

                    }
                }
            } catch (IOException e) {
                LOG.error("getStreetAddressWithMunicipalityAndStreetNameAndNumber reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return streetNumber;

    }


    public List<Municipality> getStreetAddressMunicipalities(final Integer pageSize,
                                                             final Integer from,
                                                             final Date after,
                                                             final String id,
                                                             final String municipalityCode,
                                                             final String municipalityName,
                                                             final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("streetaddress.id", id.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getStreetAddressMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }

    public List<StreetAddress> getStreetAddresses(final Integer pageSize,
                                                  final Integer from,
                                                  final String prefLabel,
                                                  final Date after,
                                                  final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<StreetAddress> streetAddresses = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_STREETADDRESS)
                .addSort("names.fi.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(null, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final StreetAddress streetAddress = mapper.readValue(hit.getSourceAsString(), StreetAddress.class);
                streetAddresses.add(streetAddress);
            } catch (IOException e) {
                LOG.error("getStreetAddresses reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return streetAddresses;

    }

    public List<StreetAddress> getStreetAddressesWithMunicipality(final Integer pageSize,
                                                                  final Integer from,
                                                                  final String prefLabel,
                                                                  final String municipalityCode,
                                                                  final Date after,
                                                                  final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<StreetAddress> streetAddresses = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_STREETADDRESS)
                .addSort("names.fi.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(null, prefLabel, after);
        builder.must(QueryBuilders.termQuery("municipality.codeValue.keyword", municipalityCode.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final StreetAddress streetAddress = mapper.readValue(hit.getSourceAsString(), StreetAddress.class);
                streetAddresses.add(streetAddress);
            } catch (IOException e) {
                LOG.error("getStreetAddressesWithMunicipality reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return streetAddresses;

    }


    public HealthCareDistrict getHealthCareDistrict(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_HEALTHCAREDISTRICT)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getHealthCareDistrict found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final HealthCareDistrict healthCareDistrict = mapper.readValue(hit.getSourceAsString(), HealthCareDistrict.class);
                    return healthCareDistrict;
                }
            } catch (IOException e) {
                LOG.error("getHealthCareDistrict reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public HealthCareDistrict getHealthCareDistrictWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_HEALTHCAREDISTRICT)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getHealthCareDistrictWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final HealthCareDistrict healthCareDistrict = mapper.readValue(hit.getSourceAsString(), HealthCareDistrict.class);
                    return healthCareDistrict;
                }
            } catch (IOException e) {
                LOG.error("getHealthCareDistrictWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getHealthCareDistrictMunicipalities(final Integer pageSize,
                                                                  final Integer from,
                                                                  final Date after,
                                                                  final String codeValue,
                                                                  final String municipalityCode,
                                                                  final String municipalityName,
                                                                  final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("healthCareDistrict.codeValue.keyword", codeValue.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getHealthCareDistrictMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }

    public List<HealthCareDistrict> getHealthCareDistricts(final Integer pageSize,
                                                           final Integer from,
                                                           final String codeValue,
                                                           final String prefLabel,
                                                           final Date after,
                                                           final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<HealthCareDistrict> healthCareDistricts = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_HEALTHCAREDISTRICT)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final HealthCareDistrict healthCareDistrict = mapper.readValue(hit.getSourceAsString(), HealthCareDistrict.class);
                healthCareDistricts.add(healthCareDistrict);
            } catch (IOException e) {
                LOG.error("getHealthCareDistricts reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return healthCareDistricts;

    }


    public MagistrateServiceUnit getMagistrateServiceUnit(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MAGISTRATESERVICEUNIT)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getMagistrateServiceUnit found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final MagistrateServiceUnit magistrateServiceUnit = mapper.readValue(hit.getSourceAsString(), MagistrateServiceUnit.class);
                    return magistrateServiceUnit;
                }
            } catch (IOException e) {
                LOG.error("getMagistrateServiceUnit reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public MagistrateServiceUnit getMagistrateServiceUnitWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MAGISTRATESERVICEUNIT)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getMagistrateServiceUnitWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final MagistrateServiceUnit magistrateServiceUnit = mapper.readValue(hit.getSourceAsString(), MagistrateServiceUnit.class);
                    return magistrateServiceUnit;
                }
            } catch (IOException e) {
                LOG.error("getMagistrateServiceUnitWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getMagistrateServiceUnitMunicipalities(final Integer pageSize,
                                                                     final Integer from,
                                                                     final Date after,
                                                                     final String codeValue,
                                                                     final String municipalityName,
                                                                     final String municipalityCode,
                                                                     final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("magistrateServiceUnit.codeValue", codeValue.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getMagistrateServiceUnitMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }

    public List<MagistrateServiceUnit> getMagistrateServiceUnits(final Integer pageSize,
                                                                 final Integer from,
                                                                 final String codeValue,
                                                                 final String prefLabel,
                                                                 final Date after,
                                                                 final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<MagistrateServiceUnit> magistrateServiceUnits = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MAGISTRATESERVICEUNIT)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final MagistrateServiceUnit magistrateServiceUnit = mapper.readValue(hit.getSourceAsString(), MagistrateServiceUnit.class);
                magistrateServiceUnits.add(magistrateServiceUnit);
            } catch (IOException e) {
                LOG.error("getMagistrativeServiceUnits reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return magistrateServiceUnits;

    }


    public ElectoralDistrict getElectoralDistrict(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_ELECTORALDISTRICT)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getElectoralDistrict found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final ElectoralDistrict electoralDistrict = mapper.readValue(hit.getSourceAsString(), ElectoralDistrict.class);
                    return electoralDistrict;
                }
            } catch (IOException e) {
                LOG.error("getElectoralDistrict reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public ElectoralDistrict getElectoralDistrictWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_ELECTORALDISTRICT)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getElectoralDistrictWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final ElectoralDistrict electoralDistrict = mapper.readValue(hit.getSourceAsString(), ElectoralDistrict.class);
                    return electoralDistrict;
                }
            } catch (IOException e) {
                LOG.error("getElectoralDistrictWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getElectoralDistrictMunicipalities(final Integer pageSize,
                                                                 final Integer from,
                                                                 final Date after,
                                                                 final String codeValue,
                                                                 final String municipalityCode,
                                                                 final String municipalityName,
                                                                 final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("electoralDistrict.codeValue", codeValue.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getElectoralDistrictMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }


    public List<ElectoralDistrict> getElectoralDistricts(final Integer pageSize,
                                                         final Integer from,
                                                         final String codeValue,
                                                         final String prefLabel,
                                                         final Date after,
                                                         final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<ElectoralDistrict> electoralDistricts = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_ELECTORALDISTRICT)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final ElectoralDistrict electoralDistrict = mapper.readValue(hit.getSourceAsString(), ElectoralDistrict.class);
                electoralDistricts.add(electoralDistrict);
            } catch (IOException e) {
                LOG.error("getElectoralDistricts reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return electoralDistricts;

    }


    public BusinessServiceSubRegion getBusinessServiceSubRegion(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_BUSINESSSERVICESUBREGION)
                .setQuery(QueryBuilders.termQuery("codeValue", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getBusinessServiceSubRegion found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final BusinessServiceSubRegion businessServiceSubRegion = mapper.readValue(hit.getSourceAsString(), BusinessServiceSubRegion.class);
                    return businessServiceSubRegion;
                }
            } catch (IOException e) {
                LOG.error("getBusinessServiceSubRegion reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public BusinessServiceSubRegion getBusinessServiceSubRegionWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_BUSINESSSERVICESUBREGION)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getBusinessServiceSubRegionWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final BusinessServiceSubRegion businessServiceSubRegion = mapper.readValue(hit.getSourceAsString(), BusinessServiceSubRegion.class);
                    return businessServiceSubRegion;
                }
            } catch (IOException e) {
                LOG.error("getBusinessServiceSubRegionWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<Municipality> getBusinessServiceSubRegionMunicipalities(final Integer pageSize,
                                                                        final Integer from,
                                                                        final Date after,
                                                                        final String codeValue,
                                                                        final String municipalityCode,
                                                                        final String municipalityName,
                                                                        final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<Municipality> municipalities = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_MUNICIPALITY)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(municipalityCode, municipalityName, after);
        builder.must(QueryBuilders.termQuery("businessServiceSubRegion.codeValue", codeValue.toLowerCase()));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final Municipality municipality = mapper.readValue(hit.getSourceAsString(), Municipality.class);
                municipalities.add(municipality);
            } catch (IOException e) {
                LOG.error("getBusinessServiceSubRegionMunicipalities reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return municipalities;

    }

    public List<BusinessServiceSubRegion> getBusinessServiceSubRegions(final Integer pageSize,
                                                                       final Integer from,
                                                                       final String codeValue,
                                                                       final String prefLabel,
                                                                       final Date after,
                                                                       final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<BusinessServiceSubRegion> businessServiceSubRegions = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_BUSINESSSERVICESUBREGION)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final BusinessServiceSubRegion businessServiceSubRegion = mapper.readValue(hit.getSourceAsString(), BusinessServiceSubRegion.class);
                businessServiceSubRegions.add(businessServiceSubRegion);
            } catch (IOException e) {
                LOG.error("getBusinessServiceSubRegions reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return businessServiceSubRegions;

    }


    public BusinessId getBusinessId(final String code) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_BUSINESSID)
                .setQuery(QueryBuilders.termQuery("codeValue.keyword", code.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getBusinessId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final BusinessId businessId = mapper.readValue(hit.getSourceAsString(), BusinessId.class);
                    return businessId;
                }
            } catch (IOException e) {
                LOG.error("getBusinessId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public BusinessId getBusinessIdWithId(final String id) {

        final ObjectMapper mapper = new ObjectMapper();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_BUSINESSID)
                .setQuery(QueryBuilders.termQuery("id.keyword", id.toLowerCase()));

        final SearchResponse response = searchRequest.execute().actionGet();

        LOG.info("getBusinessIdWithId found: " + response.getHits().getTotalHits() + " hits.");

        if (response.getHits().getTotalHits() > 0) {
            final SearchHit hit = response.getHits().getAt(0);
            try {
                if (hit != null) {
                    final BusinessId businessId = mapper.readValue(hit.getSourceAsString(), BusinessId.class);
                    return businessId;
                }
            } catch (IOException e) {
                LOG.error("getBusinessIdWithId reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        }

        return null;

    }

    public List<BusinessId> getBusinessIds(final Integer pageSize,
                                           final Integer from,
                                           final String codeValue,
                                           final String prefLabel,
                                           final Date after,
                                           final Meta meta) {

        final ObjectMapper mapper = new ObjectMapper();

        final List<BusinessId> businessIds = new ArrayList<>();

        final SearchRequestBuilder searchRequest = m_client
                .prepareSearch(DomainConstants.ELASTIC_INDEX_CUSTOMCODES)
                .setTypes(DomainConstants.ELASTIC_TYPE_BUSINESSID)
                .addSort("codeValue.keyword", SortOrder.ASC)
                .setSize(pageSize != null ? pageSize : MAX_SIZE)
                .setFrom(from != null ? from : 0);

        final BoolQueryBuilder builder = constructSearchQuery(codeValue, prefLabel, after);
        builder.must(QueryBuilders.nestedQuery("names", QueryBuilders.regexpQuery("names.fi", "~(null)"), ScoreMode.None));
        searchRequest.setQuery(builder);

        final SearchResponse response = searchRequest.execute().actionGet();
        setResultCounts(meta, response);

        Arrays.stream(response.getHits().hits()).forEach(hit -> {
            try {
                final BusinessId businessId = mapper.readValue(hit.getSourceAsString(), BusinessId.class);
                businessIds.add(businessId);
            } catch (IOException e) {
                LOG.error("getBusinessIds reading value from JSON string failed: " + hit.getSourceAsString() + ", message: " + e.getMessage());
            }
        });

        return businessIds;

    }


    private BoolQueryBuilder constructSearchQuery(final String codeValue,
                                                  final String prefLabel,
                                                  final Date after) {

        final BoolQueryBuilder builder = boolQuery();

        if (codeValue != null) {
            builder.must(QueryBuilders.prefixQuery("codeValue.keyword", codeValue.toLowerCase()));
        }

        if (prefLabel != null) {
            builder.must(QueryBuilders.nestedQuery("prefLabels", QueryBuilders.multiMatchQuery(prefLabel.toLowerCase() + "*", "prefLabels.fi", "prefLabels.se", "prefLabels.en").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX), ScoreMode.None));
        }

        if (after != null) {
            final ISO8601DateFormat dateFormat = new ISO8601DateFormat();
            final String afterString = dateFormat.format(after);

            builder.must(boolQuery()
                    .should(QueryBuilders.rangeQuery("created").gt(afterString))
                    .should(QueryBuilders.rangeQuery("modified").gt(afterString))
                    .minimumShouldMatch(1));
        }

        return builder;

    }


    private void addDateFiltersToRequest(final SearchRequestBuilder searchRequest,
                                         final Date after) {

        if (after != null) {
            final ISO8601DateFormat dateFormat = new ISO8601DateFormat();
            final String afterString = dateFormat.format(after);

            final QueryBuilder qb = boolQuery()
                    .should(QueryBuilders.rangeQuery("created").gt(afterString))
                    .should(QueryBuilders.rangeQuery("modified").gt(afterString))
                    .minimumShouldMatch(1);
            searchRequest.setQuery(qb);
        }

    }


    private void setResultCounts(final Meta meta,
                                 final SearchResponse response) {

        final Integer totalResults = toIntExact(response.getHits().getTotalHits());
        meta.setTotalResults(totalResults);

        final Integer resultCount = toIntExact(response.getHits().hits().length);
        meta.setResultCount(resultCount);

        LOG.info("Search found: " + totalResults + " total hits.");

    }

}
