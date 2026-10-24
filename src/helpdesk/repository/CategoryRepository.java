package helpdesk.repository;

import helpdesk.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(long id);

    List<Category> findAll();
}