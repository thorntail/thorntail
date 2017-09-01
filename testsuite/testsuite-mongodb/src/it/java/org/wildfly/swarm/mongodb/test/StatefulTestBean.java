/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.swarm.mongodb.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.json.Json;
import javax.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * StatefulTestBean for the MongoDB document database
 *
 * @author Scott Marlow
 */
@Stateful
public class StatefulTestBean {

    //@Resource(lookup = "java:jboss/mongodb/test")
    @Inject @Named("mongodbtestprofile")
    MongoDatabase database;

    @Resource(lookup = "java:jboss/mongodb/test")
    MongoDatabase databaseJNDI;

    public String addUserComment() {
        MongoCollection collection = null;
        Document query = null;
        try {
            // add a comment from user Melanie
            String who = "Melanie";
            Document comment = new Document("_id", who)
                    .append("name", who)
                    .append("address", new BasicDBObject("street", "123 Main Street")
                            .append("city", "Fastville")
                            .append("state", "MA")
                            .append("zip", 18180))
                    .append("comment", "I really love your new website but I have a lot of questions about using NoSQL versus a traditional RDBMS.  " +
                            "I would like to sign up for your 'MongoDB Is Web Scale' training session.");
            // save the comment
            collection = database.getCollection("comments");
            collection.insertOne(comment);

            // look up the comment from Melanie
            query = new Document("_id", who);
            FindIterable cursor = collection.find(query);
            Object userComment = cursor.first();
            return userComment.toString();
        } finally {
            collection.drop();
        }
    }

    public String addProduct() {
        MongoCollection collection = null;
        Document query = null;
        try {
            collection = database.getCollection("company");
            String companyName = "Acme products";
            JsonObject object = Json.createObjectBuilder()
                    .add("companyName", companyName)
                    .add("street", "999 Flow Lane")
                    .add("city", "Indiville")
                    .add("_id", companyName)
                    .build();
            Document document = Document.parse(object.toString());
            collection.insertOne(document);
            query = new Document("_id", companyName);
            FindIterable cursor = collection.find(query);
            Object dbObject = cursor.first();
            return dbObject.toString();
        } finally {
            if (query != null) {
                collection.drop();
            }
        }
    }
}
