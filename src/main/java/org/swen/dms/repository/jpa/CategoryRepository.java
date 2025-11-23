package org.swen.dms.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swen.dms.entity.Category;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}