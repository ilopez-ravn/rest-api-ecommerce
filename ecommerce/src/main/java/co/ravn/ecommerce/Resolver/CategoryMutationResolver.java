package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CategoryMutationResolver {

    @Autowired
    private CategoryRepository categoryRepository;

    @MutationMapping
    public Category addCategory(@Argument String name,
                                @Argument String description,
                                @Argument Integer userId) {
        Category category = new Category(name, description, userId);
        return categoryRepository.save(category);
    }

}
