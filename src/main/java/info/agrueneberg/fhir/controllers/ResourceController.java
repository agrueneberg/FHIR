package info.agrueneberg.fhir.controllers;

import info.agrueneberg.fhir.exceptions.DeletedException;
import info.agrueneberg.fhir.exceptions.IllegalTypeException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import info.agrueneberg.fhir.services.ResourceService;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(produces = "application/json")
@ResponseBody
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> search() {
        return resourceService.search();
    }

    @RequestMapping(value = "/_history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> history() {
        return resourceService.history();
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> searchForTypeImplicit(@PathVariable String type) {
        return resourceService.search(type);
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable String type, @RequestBody Map<String, Object> entity, HttpServletResponse response) throws IllegalTypeException {
        String lid = resourceService.create(type, entity);
        response.addHeader("Location", "/" + type + "/" + lid + "/_history/1");
    }

    @RequestMapping(value = "/{type}/_search", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> searchForTypeExplicit(@PathVariable String type) {
        return resourceService.search(type);
    }

    @RequestMapping(value = "/{type}/_history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> historyForType(@PathVariable String type) {
        return resourceService.history(type);
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> read(@PathVariable String type, @PathVariable String lid) throws NotFoundException, DeletedException, IllegalTypeException {
        return resourceService.read(type, lid);
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.PUT, consumes = "application/json")
    public void update(@PathVariable String type, @PathVariable String lid, @RequestBody Map<String, Object> entity, HttpServletResponse response) throws IllegalTypeException {
        Long vid = resourceService.update(type, lid, entity);
        response.addHeader("Location", "/" + type + "/" + lid + "/_history/" + vid);
        if (vid == 1) {
            response.setStatus(201);
        } else {
            response.setStatus(200);
        }
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String type, @PathVariable String lid) throws NotFoundException, IllegalTypeException {
        resourceService.delete(type, lid);
    }

    @RequestMapping(value = "/{type}/{lid}/_history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> historyForResource(@PathVariable String type, @PathVariable String lid) {
        return resourceService.history(type, lid);
    }

    @RequestMapping(value = "/{type}/{lid}/_history/{vid}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> vread(@PathVariable String type, @PathVariable String lid, @PathVariable Long vid) throws NotFoundException, IllegalTypeException {
        return resourceService.vread(type, lid, vid);
    }

    @RequestMapping(value = "/{type}/_validate", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void validate(@PathVariable String type) {
    }

}