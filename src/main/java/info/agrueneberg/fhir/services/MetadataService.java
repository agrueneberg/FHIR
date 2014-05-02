package info.agrueneberg.fhir.services;

import info.agrueneberg.fhir.exceptions.NotFoundException;
import info.agrueneberg.fhir.models.MetadataResource;
import info.agrueneberg.fhir.models.User;
import info.agrueneberg.fhir.repositories.MetadataRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetadataService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    private final MetadataRepository metadataRepository;

    @Autowired
    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public MetadataResource getResource(String resource) throws NotFoundException {
        MetadataResource metadataResource = metadataRepository.findByResource(resource);
        if (metadataResource == null) {
            throw new NotFoundException();
        }
        return metadataResource;
    }

    public void updateResource(MetadataResource resource) {
        metadataRepository.save(resource);
    }

    public boolean hasPermission(String resource, User user, String operator) {        MetadataResource metadataResource;
        String originalResource = resource;
        String originalCreator = null;
        Map<String, String> matchingRule = null;
        L: while (matchingRule == null) {
            try {
                metadataResource = getResource(resource);
            } catch (NotFoundException ex) {
                logger.warn("No metadata found for " + resource);
                return false;
            }
            // Capture original creator.
            if (originalResource.equals(resource)) {
                originalCreator = metadataResource.getCreator();
            }
            // Find matching rule in ACLs.
            if (metadataResource.getAcls() != null) {
                for (Map<String, String> rule : metadataResource.getAcls()) {
                    if (rule.get("operator").equals(operator) && ((rule.containsKey("username") && rule.get("username").equals(user.getUsername())) || (rule.containsKey("role") && rule.get("role").equals("authenticated")))) {
                        matchingRule = rule;
                        break L;
                    }
                }
            }
            if (metadataResource.getInheritFrom() == null) {
                if (!resource.equals("/")) {
                    logger.warn("Inheritance chain broken at " + resource);
                }
                return false;
            }
            resource = metadataResource.getInheritFrom();
        }
        if (matchingRule.get("state").equals("all") || (matchingRule.get("state").equals("self") && originalCreator.equals(user.getUsername()))) {
            return true;
        }
        return false;
    }

}