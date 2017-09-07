package fi.vm.yti.cls.api.api;

import fi.vm.yti.cls.common.model.Meta;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement
@XmlType(propOrder = { "meta", "results" })
public class ListResponseWrapper<T> {

    private Meta meta;

    private List<T> results;

    public ListResponseWrapper() {

    }

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
