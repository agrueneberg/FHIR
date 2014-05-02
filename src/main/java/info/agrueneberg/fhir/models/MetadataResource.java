package info.agrueneberg.fhir.models;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "metadata")
public class MetadataResource {

    @Id
    @Getter
    private String id;

    @Getter
    @Setter
    private String resource;

    @Getter
    @Setter
    private String creator;

    @Getter
    @Setter
    private String inheritFrom;

    @Getter
    @Setter
    private List<Map<String, String>> acls;

}