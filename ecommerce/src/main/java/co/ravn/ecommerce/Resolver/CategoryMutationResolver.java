package co.ravn.ecommerce.Resolver;

import co.ravn.ecommerce.Entities.Category;
import co.ravn.ecommerce.Entities.SysUser;
import co.ravn.ecommerce.Repositories.CategoryRepository;
import co.ravn.ecommerce.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class CategoryMutationResolver {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @MutationMapping
    public Category addCategory(@Argument String name,
                               @Argument String description,
                               @Argument Integer userId) {
        SysUser sysUser = userRepository.getReferenceById(userId);
        Category category = new Category(name, description, sysUser);
        return categoryRepository.save(category);
    }

}
