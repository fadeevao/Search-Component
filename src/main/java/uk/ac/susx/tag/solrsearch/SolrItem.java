package uk.ac.susx.tag.solrsearch;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.susx.tag.inputData.InputData;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;


public class SolrItem  {

    @Field("id")
    private String id;

    @Field("content")
    private String content;

    /*
    Creates a SolrItem from a Datum object - specifically to integrate with Method52
     */
    public SolrItem(Datum datum, String bodyKey, String idKey) {
        Key<String> mainBody = Key.of(bodyKey, RuntimeType.STRING);
        Key<String> documentId = Key.of(idKey, RuntimeType.STRING);
        this.id = datum.get(documentId);
        this.content = datum.get(mainBody);
    }

    /*
    Creates a SolrItem from a parsed csv file
     */
    public SolrItem(InputData message) {
        this.id = message.getId();
        this.content = message.getMessage();
    }

    /*
    Constructor for testing purposes mostly
     */
    public SolrItem(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
