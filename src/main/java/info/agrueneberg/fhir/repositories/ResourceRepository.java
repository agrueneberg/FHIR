package info.agrueneberg.fhir.repositories;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import info.agrueneberg.fhir.exceptions.DeletedException;
import info.agrueneberg.fhir.exceptions.IllegalTypeException;
import info.agrueneberg.fhir.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceRepository {

    private final DBCollection resourcesCollection;
    private final DBCollection versionsCollection;

    @Autowired
    public ResourceRepository(MongoTemplate mongoTemplate) {
        this.resourcesCollection = mongoTemplate.getCollection("resources");
        this.versionsCollection = mongoTemplate.getCollection("versions");
    }

    public Map<String, Object> read(String type, String lid) throws NotFoundException, DeletedException, IllegalTypeException {
        DBObject query = new BasicDBObject();
        query.put("_type", type);
        query.put("_lid", lid);
        DBObject doc = resourcesCollection.findOne(query);
        if (doc == null) {
            // Check if the document was deleted
            DBObject previousDoc = new BasicDBObject(getPrevious(lid));
            if (previousDoc.containsField("_deleted") && (Boolean) previousDoc.get("_deleted") == true) {
                throw new DeletedException();
            } else {
                throw new NotFoundException();
            }
        } else {
            doc.removeField("_id");
            return doc.toMap();
        }
    }

    public Map<String, Object> vread(String type, String lid, Long vid) throws NotFoundException, IllegalTypeException {
        DBObject query = new BasicDBObject();
        query.put("_type", type);
        query.put("_lid", lid);
        query.put("_vid", vid);
        DBObject doc = versionsCollection.findOne(query);
        if (doc == null || (doc.containsField("_deleted") && (Boolean) doc.get("_deleted") == true)) {
            throw new NotFoundException();
        } else {
            doc.removeField("_id");
            return doc.toMap();
        }
    }

    public String create(String type, Map<String, Object> entity) throws IllegalTypeException {
        DBObject doc = new BasicDBObject(entity);
        doc.removeField("_id");
        doc.removeField("_deleted");
        String lid = generateUUID();
        doc.put("_type", type);
        doc.put("_lid", lid);
        doc.put("_vid", new Long(1));
        resourcesCollection.insert(doc);
        versionsCollection.insert(doc);
        return lid;
    }

    public Long update(String type, String lid, Map<String, Object> entity) throws IllegalTypeException {
        DBObject doc = new BasicDBObject(entity);
        doc.removeField("_id");
        doc.removeField("_deleted");
        doc.put("_type", type);
        doc.put("_lid", lid);
        // Find previous doc
        DBObject query = new BasicDBObject();
        query.put("_lid", lid);
        DBObject previousDoc = resourcesCollection.findOne(query);
        if (previousDoc == null) {
            doc.put("_vid", new Long(1));
        } else {
            doc.put("_vid", ((Number) previousDoc.get("_vid")).longValue() + 1);
            resourcesCollection.remove(previousDoc);
        }
        resourcesCollection.insert(doc);
        versionsCollection.insert(doc);
        return ((Number) doc.get("_vid")).longValue();
    }

    public void delete(String type, String lid) throws NotFoundException, IllegalTypeException {
        DBObject query = new BasicDBObject();
        query.put("_lid", lid);
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_vid", -1);
        DBObject previousDoc = resourcesCollection.findOne(query, null, orderBy);
        if (previousDoc == null) {
            throw new NotFoundException();
        } else {
            DBObject doc = new BasicDBObject();
            doc.put("_type", type);
            doc.put("_lid", lid);
            doc.put("_vid", ((Number) previousDoc.get("_vid")).longValue() + 1);
            doc.put("_deleted", true);
            resourcesCollection.remove(previousDoc);
            versionsCollection.insert(doc);
        }
    }

    public List<Map<String, Object>> search() {
        DBCursor cursor = resourcesCollection.find();
        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>(cursor.count());
        try {
            while(cursor.hasNext()) {
                DBObject doc = cursor.next();
                doc.removeField("_id");
                docs.add(doc.toMap());
            }
        } finally {
            cursor.close();
        }
        return docs;
    }

    public List<Map<String, Object>> search(String type) {
        DBObject query = new BasicDBObject();
        query.put("_type", type);
        DBCursor cursor = resourcesCollection.find(query);
        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>(cursor.count());
        try {
            while(cursor.hasNext()) {
                DBObject doc = cursor.next();
                doc.removeField("_id");
                docs.add(doc.toMap());
            }
        } finally {
            cursor.close();
        }
        return docs;
    }

    public List<Map<String, Object>> history() {
        DBObject query = new BasicDBObject();
        query.put("_deleted", new BasicDBObject("$exists", false));
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_type", 1);
        orderBy.put("_lid", 1);
        orderBy.put("_vid", 1);
        DBCursor cursor = versionsCollection.find(query).sort(orderBy);
        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>(cursor.count());
        try {
            while(cursor.hasNext()) {
                DBObject doc = cursor.next();
                doc.removeField("_id");
                docs.add(doc.toMap());
            }
        } finally {
            cursor.close();
        }
        return docs;
    }

    public List<Map<String, Object>> history(String type) {
        DBObject query = new BasicDBObject();
        query.put("_type", type);
        query.put("_deleted", new BasicDBObject("$exists", false));
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_lid", 1);
        orderBy.put("_vid", 1);
        DBCursor cursor = versionsCollection.find(query).sort(orderBy);
        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>(cursor.count());
        try {
            while(cursor.hasNext()) {
                DBObject doc = cursor.next();
                doc.removeField("_id");
                docs.add(doc.toMap());
            }
        } finally {
            cursor.close();
        }
        return docs;
    }

    public List<Map<String, Object>> history(String type, String lid) {
        DBObject query = new BasicDBObject();
        query.put("_type", type);
        query.put("_lid", lid);
        query.put("_deleted", new BasicDBObject("$exists", false));
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_vid", 1);
        DBCursor cursor = versionsCollection.find(query).sort(orderBy);
        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>(cursor.count());
        try {
            while(cursor.hasNext()) {
                DBObject doc = cursor.next();
                doc.removeField("_id");
                docs.add(doc.toMap());
            }
        } finally {
            cursor.close();
        }
        return docs;
    }

    private Map<String, Object> getPrevious(String lid) throws NotFoundException {
        DBObject query = new BasicDBObject();
        query.put("_lid", lid);
        DBObject orderBy = new BasicDBObject();
        orderBy.put("_vid", -1);
        DBObject doc = versionsCollection.findOne(query, null, orderBy);
        if (doc == null) {
            throw new NotFoundException();
        } else {
            return doc.toMap();
        }
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}