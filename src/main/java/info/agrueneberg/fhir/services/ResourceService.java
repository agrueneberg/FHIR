package info.agrueneberg.fhir.services;

import info.agrueneberg.fhir.exceptions.DeletedException;
import info.agrueneberg.fhir.exceptions.IllegalTypeException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import info.agrueneberg.fhir.repositories.ResourceRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Map<String, Object> read(String type, String lid) throws NotFoundException, DeletedException, IllegalTypeException {
        return resourceRepository.read(type, lid);
    }

    public Map<String, Object> vread(String type, String lid, Long vid) throws NotFoundException, IllegalTypeException {
        return resourceRepository.vread(type, lid, vid);
    }

    public String create(String type, Map<String, Object> entity) throws IllegalTypeException {
        return resourceRepository.create(type, entity);
    }

    public Long update(String type, String lid, Map<String, Object> entity) throws IllegalTypeException {
        return resourceRepository.update(type, lid, entity);
    }

    public void delete(String type, String lid) throws NotFoundException, IllegalTypeException {
        resourceRepository.delete(type, lid);
    }

    public List<Map<String, Object>> search() {
        return resourceRepository.search(null, new HashMap<String, String[]>());
    }

    public List<Map<String, Object>> search(String type, Map<String, String[]> parameters) {
        return resourceRepository.search(type, parameters);
    }

    public List<Map<String, Object>> history() {
        return resourceRepository.history();
    }

    public List<Map<String, Object>> history(String type) {
        return resourceRepository.history(type);
    }

    public List<Map<String, Object>> history(String type, String lid) {
        return resourceRepository.history(type, lid);
    }

}