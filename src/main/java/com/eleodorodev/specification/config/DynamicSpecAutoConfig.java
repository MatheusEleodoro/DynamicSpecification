package com.eleodorodev.specification.config;

import com.eleodorodev.specification.web.QueryArgsHandlerMethodResolver;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration class for dynamic specification.
 *
 * <p>This class implements the {@link WebMvcConfigurer} interface to customize the
 * Spring MVC configuration. It adds a custom argument resolver to handle query arguments.</p>
 *
 * @author Matheus Eleodoro
 */
@Configuration
public class DynamicSpecAutoConfig implements WebMvcConfigurer {

    /**
     * Adds custom argument resolvers to the list of resolvers.
     *
     * <p>This method is overridden to add the {@link QueryArgsHandlerMethodResolver}
     * as the first argument resolver in the list. This resolver is responsible for
     * handling query arguments dynamically.</p>
     *
     * @param resolvers The list of argument resolvers to which the custom resolver will be added.
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addFirst(new QueryArgsHandlerMethodResolver());
    }
}