package org.swen.dms.repository.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.swen.dms.entity.DocumentSearch;

import java.util.List;

public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearch, String> {

    // This automagically creates a query to search inside the 'content' field
    List<DocumentSearch> findByContentContaining(String content);
}