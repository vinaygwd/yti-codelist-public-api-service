package fi.vm.yti.cls.api.api;

import fi.vm.yti.cls.common.model.Meta;

public class ErrorWrapper {

    public static final String SPECIFIC_STREET_ADDRESS_NOT_FOUND = "Specific street address not found!";

    public static final String STREET_NUMBER_NOT_FOUND = "Street number not found!";

    public static final String STREET_ADDRESS_NOT_FOUND = "Street address not found!";

    public static final String POSTALCODE_NOT_FOUND = "Postalcode not found!";

    public static final String POSTMANAGEMENTDISTRICT_NOT_FOUND = "Postmanagementdistrict not found!";

    public static final String MUNICIPALITY_NOT_FOUND = "Municipality not found!";


    private Meta m_meta;


    public ErrorWrapper() {

    }


    public Meta getMeta() {
        return m_meta;
    }

    public void setMeta(final Meta meta) {
        m_meta = meta;
    }

}
