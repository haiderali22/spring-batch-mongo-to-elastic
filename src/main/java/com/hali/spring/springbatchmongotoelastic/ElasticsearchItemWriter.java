package com.hali.spring.springbatchmongotoelastic;

import co.elastic.clients.elasticsearch.core.IndexRequest;
import lombok.extern.slf4j.Slf4j;
import static java.lang.String.valueOf;
import static java.util.UUID.randomUUID;
import static org.springframework.transaction.support.TransactionSynchronizationManager.bindResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.getResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.hasResource;
import static org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive;
import static org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization;
import static org.springframework.transaction.support.TransactionSynchronizationManager.unbindResource;
import static org.springframework.util.Assert.state;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;


@Slf4j
public class ElasticsearchItemWriter implements ItemWriter<IndexQuery>, InitializingBean {

	private final String dataKey;
	private final String index;
	private ElasticsearchOperations  elasticsearchTemplate;
	private boolean delete;

	public ElasticsearchItemWriter(ElasticsearchOperations elasticsearchTemplate , String index) {
		super();
		dataKey = valueOf(randomUUID());
		delete = false;
		this.index = index;
		this.elasticsearchTemplate = elasticsearchTemplate;
	}

	/**
	 * A flag for removing items given to the writer. Default value is set to false indicating that the items will be saved.
	 * otherwise, the items will be removed.
	 *
	 * @param delete flag
	 */
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		state(elasticsearchTemplate != null, "An ElasticsearchOperations implementation is required.");
	}

	@Override
	public void write(Chunk<? extends IndexQuery> chunk) throws Exception {

		if(isActualTransactionActive()) {
			addToBuffer(chunk.getItems());
		}else {
			writeItems(chunk.getItems());
		}
	}

	/**
	 * Writes to Elasticsearch via the template.
	 * This can be overridden by a subclass if required.
	 *
	 * @param items the list of items to be indexed.
	 */
	protected void writeItems(List<? extends IndexQuery> items) {

		if(isEmpty(items)) {
			log.warn("no items to write to elasticsearch. list is empty or null");
		}else {

			for(IndexQuery item : items) {

				if(delete) {
					String id = item.getId();
					log.debug("deleting item with id {}", id);
					elasticsearchTemplate.delete(id, IndexCoordinates.of(index));
				}else {
					String id = elasticsearchTemplate.index(item, IndexCoordinates.of(index));
					log.debug("added item to elasticsearch with id {}", id);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addToBuffer(List<? extends IndexQuery> items) {

		if(hasResource(dataKey)) {
			log.debug("appending items to buffer under key {}", dataKey);
			List<IndexQuery> buffer = (List<IndexQuery>) getResource(dataKey);
			buffer.addAll(items);
		}else {
			log.debug("adding items to buffer under key {}", dataKey);
			bindResource(dataKey, items);
			registerSynchronization(new TransactionSynchronizationCallbackImpl());
		}
	}

	private class TransactionSynchronizationCallbackImpl implements TransactionSynchronization {
		@SuppressWarnings("unchecked")
		@Override
		public void beforeCommit(boolean readOnly) {

			List<IndexQuery> items = (List<IndexQuery>) getResource(dataKey);
			if(!isEmpty(items)) {
				if(!readOnly) {
					writeItems(items);
				}else{
					log.warn("can not write items to elasticsearch as transaction is read only");
				}
			}else {
				log.warn("no items to write to elasticsearch. list is empty or null");
			}
		}

		@Override
		public void afterCompletion(int status) {
			if(hasResource(dataKey)) {
				log.debug("removing items from buffer under key {}", dataKey);
				unbindResource(dataKey);
			}
		}
	}
}