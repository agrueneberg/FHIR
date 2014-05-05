package info.agrueneberg.fhir.controllers;

import info.agrueneberg.fhir.exceptions.AccessDeniedException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import info.agrueneberg.fhir.models.MetadataResource;
import info.agrueneberg.fhir.models.User;
import info.agrueneberg.fhir.services.MetadataService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@ResponseBody
public class MetadataController {

    private final MetadataService metadataService;

    @Autowired
    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @RequestMapping(value = "/.well-known/governance", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, String>> getAcls(@RequestParam(value = "resource") String resourcePath) throws NotFoundException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "govern");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        MetadataResource metadataResource = metadataService.getResource(resourcePath);
        List<Map<String, String>> acls = metadataResource.getAcls();
        if (acls == null) {
            acls = new ArrayList<Map<String, String>>(0);
        }
        return acls;
    }

    @RequestMapping(value = "/.well-known/governance", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void updateAcls(@RequestParam(value = "resource") String resourcePath, @RequestBody List<Map<String, String>> acls) throws NotFoundException, AccessDeniedException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAccess = metadataService.hasPermission(resourcePath, user, "govern");
        if (!hasAccess) {
            throw new AccessDeniedException();
        }
        MetadataResource metadataResource = metadataService.getResource(resourcePath);
        metadataResource.setAcls(acls);
        metadataService.updateResource(metadataResource);
    }

}