package org.swen.dms.repository.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.swen.dms.entity.DocumentSearch;

import java.util.List;

public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentSearch, String> {


    // Handles sentences correctly ("build error" -> finds "build" OR "error")
    @Query("{\"match\": {\"content\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<DocumentSearch> searchByContent(String query);
}