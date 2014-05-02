package info.agrueneberg.fhir.repositories;

import info.agrueneberg.fhir.models.MetadataResource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MetadataRepository extends MongoRepository<MetadataResource, String> {

    MetadataResource findByResource(String resource);

}