package com.hali.spring.springbatchmongotoelastic;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Bootstrap  implements CommandLineRunner {

    private final MongoOperations mongoTemplate;

    @Override
    public void run(String... args) throws Exception {

        if(mongoTemplate.getCollection("person").countDocuments() > 0)
            return;

        Map<String,Object> person = new HashMap<>();
        person.put("username","admin");
        person.put("age",12);

        mongoTemplate.getCollection("person").insertOne(new Document(person));

        Map<String,Object> car = new HashMap<>();
        car.put("manufacturer","Ford");
        car.put("year_build",2006);

        mongoTemplate.getCollection("car").insertOne(new Document(car));
    }
}
