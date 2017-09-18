package fi.vm.yti.cls.api.api;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import fi.vm.yti.cls.common.model.Meta;

@XmlRootElement
@XmlType(propOrder = { "meta", "results" })
public class ListResponseWrapper<T> {

    private Meta meta;

    private List<T> results;

    public ListResponseWrapper() { }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(final Meta meta) {
        this.meta = meta;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(final List<T> results) {
        this.results = results;
    }

}
