package org.swen.dms.service;

import org.swen.dms.entity.DocumentSearch;
import java.util.List;

public interface SearchService {
    List<DocumentSearch> searchDocuments(String query);
}
