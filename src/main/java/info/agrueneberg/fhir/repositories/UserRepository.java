package info.agrueneberg.fhir.repositories;

import info.agrueneberg.fhir.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

}