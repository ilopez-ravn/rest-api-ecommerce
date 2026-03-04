package co.ravn.ecommerce.config;

import co.ravn.ecommerce.scalars.LocalDateTimeScalar;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {

    @Bean
    public GraphQLScalarType localDateTimeScalar() {
        return LocalDateTimeScalar.createLocalDateTimeScalar();
    }

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder.scalar(LocalDateTimeScalar.createLocalDateTimeScalar()).scalar(ExtendedScalars.Json);
    }

}