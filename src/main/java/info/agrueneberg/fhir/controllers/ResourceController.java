package info.agrueneberg.fhir.controllers;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import info.agrueneberg.fhir.exceptions.DeletedException;
import info.agrueneberg.fhir.exceptions.IllegalTypeException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import info.agrueneberg.fhir.services.ResourceService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<String> search() {
        List<DBObject> docs = resourceService.search();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(docs.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/_history", method = RequestMethod.GET)
    public ResponseEntity<String> history() {
        List<DBObject> docs = resourceService.history();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(docs.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.GET)
    public ResponseEntity<String> searchForTypeImplicit(@PathVariable String type) {
        List<DBObject> docs = resourceService.search(type);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(docs.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> create(@PathVariable String type, @RequestBody String body) throws IllegalTypeException {
        DBObject doc = (DBObject) JSON.parse(body);
        String lid = resourceService.create(type, doc);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain");
        headers.add("Location", "/" + type + "/" + lid + "/_history/1");
        return new ResponseEntity<String>("Created", headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{type}/_search", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> searchForTypeExplicit(@PathVariable String type) {
        List<DBObject> docs = resourceService.search(type);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(docs.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/_history", method = RequestMethod.GET)
    public ResponseEntity<String> historyForType(@PathVariable String type) {
        List<DBObject> docs = resourceService.history(type);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(docs.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.GET)
    public ResponseEntity<String> read(@PathVariable String type, @PathVariable String lid) throws NotFoundException, DeletedException, IllegalTypeException {
        DBObject doc = resourceService.read(type, lid);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(doc.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity<String> update(@PathVariable String type, @PathVariable String lid, @RequestBody String body) throws IllegalTypeException {
        DBObject doc = (DBObject) JSON.parse(body);
        doc = resourceService.update(type, lid, doc);
        Long vid = ((Number) doc.get("_vid")).longValue();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain");
        headers.add("Location", "/" + type + "/" + lid + "/_history/" + vid);
        if (vid == 1) {
            return new ResponseEntity<String>("Created", headers, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<String>("Updated", headers, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable String type, @PathVariable String lid) throws NotFoundException, IllegalTypeException {
        resourceService.delete(type, lid);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain");
        return new ResponseEntity<String>("Deleted", headers, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/{type}/{lid}/_history", method = RequestMethod.GET)
    public ResponseEntity<String> historyForResource(@PathVariable String type, @PathVariable String lid) {
        List<DBObject> docs = resourceService.history(type, lid);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(docs.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/{lid}/_history/{vid}", method = RequestMethod.GET)
    public ResponseEntity<String> vread(@PathVariable String type, @PathVariable String lid, @PathVariable Long vid) throws NotFoundException, IllegalTypeException {
        DBObject doc = resourceService.vread(type, lid, vid);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<String>(doc.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/_validate", method = RequestMethod.POST)
    public ResponseEntity<String> validate(@PathVariable String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/plain");
        return new ResponseEntity<String>("OK", headers, HttpStatus.OK);
    }

}