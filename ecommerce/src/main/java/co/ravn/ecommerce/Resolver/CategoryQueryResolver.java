package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CategoryQueryResolver {

    @Autowired
    private CategoryRepository categoryRepository;

    @QueryMapping
    public Category categoryById(@Argument int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryRepository.findAll();
    }

}
