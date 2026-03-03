package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.Entities.Inventory.Category;
import co.ravn.ecommerce.Repositories.Inventory.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class CategoryMutationResolver {

    private final CategoryRepository categoryRepository;

    @MutationMapping
    public Category addCategory(@Argument String name,
                                @Argument String description,
                                @Argument Integer userId) {
        Category category = Category.builder().name(name).description(description).createdBy(userId).build();
        return categoryRepository.save(category);
    }

}
