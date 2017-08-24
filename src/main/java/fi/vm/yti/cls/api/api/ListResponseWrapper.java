package fi.vm.yti.cls.api.api;

import fi.vm.yti.cls.common.model.Meta;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;


@XmlRootElement
@XmlType(propOrder = { "meta", "results" })
public class ListResponseWrapper<T> {

    private Meta m_meta;

    private List<T> m_results;


    public ListResponseWrapper() {

    }


    public Meta getMeta() {
        return m_meta;
    }

    public void setMeta(final Meta meta) {
        m_meta = meta;
    }


    public List<T> getResults() {
        return m_results;
    }

    public void setResults(final List<T> results) {
        m_results = results;
    }

}
