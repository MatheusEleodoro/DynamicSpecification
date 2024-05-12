package com.eleodorodev.specification;

import com.eleodorodev.specification.params.QueryString;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.util.Pair;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * BaseDynamicFilter
 *
 * @author <a href="https://github.com/MatheusEleodoro">Matheus Eleodoro</a>
 * @version 1.0.0
 * @apiNote To standardize classes that will implement filters in the body
 * @see <a href="https://github.com/MatheusEleodoro">...</a>
 */

public abstract class BaseDynamicFilter implements Serializable {

    public QueryString toQueryString() {
        Map<String, Pair<Object, String>> map = new HashMap<>();
        BeanWrapper wrapper = new BeanWrapperImpl(this);

        for (PropertyDescriptor propertyDescriptor : wrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = wrapper.getPropertyValue(propertyName);
            if (propertyValue != null && !(propertyValue instanceof Class<?>)) {
                map.put(propertyName, Pair.of(propertyValue, ""));
            }
        }
        return new QueryString(map);
    }
}
