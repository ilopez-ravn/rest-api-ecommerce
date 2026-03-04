package co.ravn.ecommerce.resolver;

import co.ravn.ecommerce.entities.inventory.Category;
import co.ravn.ecommerce.repositories.inventory.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class CategoryQueryResolver {

    private final CategoryRepository categoryRepository;

    @QueryMapping
    public Category categoryById(@Argument int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryRepository.findAll();
    }

}
