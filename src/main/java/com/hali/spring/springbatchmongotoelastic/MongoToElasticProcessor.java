package com.hali.spring.springbatchmongotoelastic;

import org.bson.Document;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import java.util.HashMap;
import java.util.Map;

public class MongoToElasticProcessor implements ItemProcessor<Document, IndexQuery> {

    @Override
    public IndexQuery process(Document item) throws Exception {

        var data =  new HashMap<>(item);
        data.remove("_id");

        IndexQuery indexQuery = new IndexQueryBuilder()

                .withId(item.get("_id").toString())
                .withObject(data)
                .build();
        return indexQuery;
    }
}