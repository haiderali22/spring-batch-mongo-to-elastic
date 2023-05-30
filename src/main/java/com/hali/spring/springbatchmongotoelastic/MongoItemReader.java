package com.hali.spring.springbatchmongotoelastic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.util.json.ParameterBindingDocumentCodec;
import org.springframework.data.mongodb.util.json.ParameterBindingJsonReader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;


public class MongoItemReader<T> extends AbstractPaginatedDataItemReader<T> implements InitializingBean {
    private MongoOperations template;
    private Query query;
    private Sort sort  ;
    private String fields;
    private String collection;
    private String temporalField;

    private String queryString;

    private JobExecution jobExecution;

    private Class<T> type;

    public MongoItemReader() {
        this.setName(ClassUtils.getShortName(MongoItemReader.class));
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        jobExecution = stepExecution.getJobExecution();
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public void setQuery(String queryString) {
        this.queryString = queryString;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public void setTemporalField(String temporalField) {
        this.temporalField = temporalField;
    }

    public void setTemplate(MongoOperations template) {
        this.template = template;
    }


    public void setFields(String fields) {
        this.fields = fields;
    }

    public void setSort(Map<String, Sort.Direction> sorts) {
        Assert.notNull(sorts, "Sorts must not be null");
        this.sort = this.convertToSort(sorts);
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }


    protected Iterator<T> doPageRead() {
        Assert.state(this.collection != null, "An collection name is required.");

        PageRequest pageRequest;
        if (this.queryString != null) {

            if(this.sort != null)
                pageRequest = PageRequest.of(this.page, this.pageSize, this.sort);
            else
                pageRequest = PageRequest.of(this.page, this.pageSize);
            String populatedQuery = this.replacePlaceholders(this.queryString, new ArrayList<>());
            BasicQuery mongoQuery;
            if (StringUtils.hasText(this.fields)) {
                mongoQuery = new BasicQuery(populatedQuery, this.fields);
            } else {
                mongoQuery = new BasicQuery(populatedQuery);
            }

            mongoQuery.with(pageRequest);

            return this.template.find(mongoQuery, this.type, this.collection).iterator();
        } else {
            pageRequest = PageRequest.of(this.page, this.pageSize);
            this.query.with(pageRequest);
            return this.template.find(this.query, this.type, this.collection).iterator() ;
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(this.template != null, "An implementation of MongoOperations is required.");

    }

    private String replacePlaceholders(String input, List<Object> values) {
        ParameterBindingJsonReader reader = new ParameterBindingJsonReader(input, values.toArray());
        DecoderContext decoderContext = DecoderContext.builder().build();
        Document document = (new ParameterBindingDocumentCodec()).decode(reader, decoderContext);
        return document.toJson();
    }

    private Sort convertToSort(Map<String, Sort.Direction> sorts) {
        List<Sort.Order> sortValues = new ArrayList(sorts.size());
        Iterator var3 = sorts.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, Sort.Direction> curSort = (Map.Entry)var3.next();
            sortValues.add(new Sort.Order((Sort.Direction)curSort.getValue(), (String)curSort.getKey()));
        }

        return Sort.by(sortValues);
    }
}