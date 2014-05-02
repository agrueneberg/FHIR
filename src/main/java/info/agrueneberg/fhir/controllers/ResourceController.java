package info.agrueneberg.fhir.controllers;

import info.agrueneberg.fhir.exceptions.AccessDeniedException;
import info.agrueneberg.fhir.exceptions.DeletedException;
import info.agrueneberg.fhir.exceptions.IllegalTypeException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import info.agrueneberg.fhir.models.MetadataResource;
import info.agrueneberg.fhir.models.User;
import info.agrueneberg.fhir.services.MetadataService;
import info.agrueneberg.fhir.services.ResourceService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final MetadataService metadataService;

    @Autowired
    public ResourceController(ResourceService resourceService, MetadataService metadataService) {
        this.resourceService = resourceService;
        this.metadataService = metadataService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> search(HttpServletRequest request) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(request.getServletPath(), user, "history");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        List<Map<String, Object>> resources = resourceService.search();
        Iterator<Map<String, Object>> itr = resources.iterator();
        while (itr.hasNext()) {
            Map<String, Object> embeddedResource = itr.next();
            String embeddedResourcePath = "/" + embeddedResource.get("_type") + "/" + embeddedResource.get("_lid");
            boolean includeEmbeddedResource = metadataService.hasPermission(embeddedResourcePath, user, "read");
            if (!includeEmbeddedResource) {
                itr.remove();
            }
        }
        return resources;
    }

    @RequestMapping(value = "/_history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> history() throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String resourcePath = "/";
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "history");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        List<Map<String, Object>> resources = resourceService.history();
        Iterator<Map<String, Object>> itr = resources.iterator();
        while (itr.hasNext()) {
            Map<String, Object> embeddedResource = itr.next();
            String embeddedResourcePath = "/" + embeddedResource.get("_type") + "/" + embeddedResource.get("_lid");
            boolean includeEmbeddedResource = metadataService.hasPermission(embeddedResourcePath, user, "read");
            if (!includeEmbeddedResource) {
                itr.remove();
            }
        }
        return resources;
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> searchForTypeImplicit(@PathVariable String type, HttpServletRequest request) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(request.getServletPath(), user, "list");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        List<Map<String, Object>> resources = resourceService.search(type);
        Iterator<Map<String, Object>> itr = resources.iterator();
        while (itr.hasNext()) {
            Map<String, Object> embeddedResource = itr.next();
            String embeddedResourcePath = "/" + embeddedResource.get("_type") + "/" + embeddedResource.get("_lid");
            boolean includeEmbeddedResource = metadataService.hasPermission(embeddedResourcePath, user, "read");
            if (!includeEmbeddedResource) {
                itr.remove();
            }
        }
        return resources;
    }

    @RequestMapping(value = "/{type}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable String type, @RequestBody Map<String, Object> entity, HttpServletRequest request, HttpServletResponse response) throws IllegalTypeException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(request.getServletPath(), user, "add");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        String lid = resourceService.create(type, entity);
        MetadataResource metadataResource = new MetadataResource();
        metadataResource.setResource("/" + type + "/" + lid);
        metadataResource.setCreator(user.getUsername());
        metadataResource.setInheritFrom("/" + type);
        metadataService.updateResource(metadataResource);
        response.addHeader("Location", "/" + type + "/" + lid + "/_history/1");
    }

    @RequestMapping(value = "/{type}/_search", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> searchForTypeExplicit(@PathVariable String type) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String resourcePath = "/" + type;
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "list");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        List<Map<String, Object>> resources = resourceService.search(type);
        Iterator<Map<String, Object>> itr = resources.iterator();
        while (itr.hasNext()) {
            Map<String, Object> embeddedResource = itr.next();
            String embeddedResourcePath = "/" + embeddedResource.get("_type") + "/" + embeddedResource.get("_lid");
            boolean includeEmbeddedResource = metadataService.hasPermission(embeddedResourcePath, user, "read");
            if (!includeEmbeddedResource) {
                itr.remove();
            }
        }
        return resources;
    }

    @RequestMapping(value = "/{type}/_history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> historyForType(@PathVariable String type) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String resourcePath = "/" + type;
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "history");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        List<Map<String, Object>> resources = resourceService.history(type);
        Iterator<Map<String, Object>> itr = resources.iterator();
        while (itr.hasNext()) {
            Map<String, Object> embeddedResource = itr.next();
            String embeddedResourcePath = "/" + embeddedResource.get("_type") + "/" + embeddedResource.get("_lid");
            boolean includeEmbeddedResource = metadataService.hasPermission(embeddedResourcePath, user, "read");
            if (!includeEmbeddedResource) {
                itr.remove();
            }
        }
        return resources;
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> read(@PathVariable String type, @PathVariable String lid, HttpServletRequest request) throws NotFoundException, DeletedException, IllegalTypeException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(request.getServletPath(), user, "read");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        return resourceService.read(type, lid);
    }

    @RequestMapping(value = "/{type}/{lid}", method = RequestMethod.PUT, consumes = "application/json")
    public void update(@PathVariable String type, @PathVariable String lid, @RequestBody Map<String, Object> entity, HttpServletRequest request, HttpServletResponse response) throws IllegalTypeException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(request.getServletPath(), user, "write");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
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
    public void delete(@PathVariable String type, @PathVariable String lid, HttpServletRequest request) throws NotFoundException, IllegalTypeException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(request.getServletPath(), user, "write");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        resourceService.delete(type, lid);
    }

    @RequestMapping(value = "/{type}/{lid}/_history", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> historyForResource(@PathVariable String type, @PathVariable String lid) throws AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String resourcePath = "/" + type + "/" + lid;
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "history");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        List<Map<String, Object>> resources = resourceService.history(type, lid);
        Iterator<Map<String, Object>> itr = resources.iterator();
        while (itr.hasNext()) {
            Map<String, Object> embeddedResource = itr.next();
            String embeddedResourcePath = "/" + embeddedResource.get("_type") + "/" + embeddedResource.get("_lid");
            boolean includeEmbeddedResource = metadataService.hasPermission(embeddedResourcePath, user, "read");
            if (!includeEmbeddedResource) {
                itr.remove();
            }
        }
        return resources;
    }

    @RequestMapping(value = "/{type}/{lid}/_history/{vid}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> vread(@PathVariable String type, @PathVariable String lid, @PathVariable Long vid) throws NotFoundException, IllegalTypeException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String resourcePath = "/" + type + "/" + lid;
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "history");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        return resourceService.vread(type, lid, vid);
    }

    @RequestMapping(value = "/{type}/_validate", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void validate(@PathVariable String type) {
    }

}