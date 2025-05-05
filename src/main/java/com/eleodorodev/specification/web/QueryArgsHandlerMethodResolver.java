package com.eleodorodev.specification.web;

import com.eleodorodev.specification.params.QueryString;
import com.eleodorodev.specification.params.QueryStringConverter;
import com.eleodorodev.specification.params.annotation.DynamicArgsParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

@Component
public class QueryArgsHandlerMethodResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(@NonNull MethodParameter parameter) {
    return parameter.hasParameterAnnotation(DynamicArgsParam.class) &&
        parameter.getParameterType().equals(QueryString.class);
  }

  @Override
  public Object resolveArgument(@NonNull MethodParameter parameter,
                                ModelAndViewContainer mavContainer,
                                @NonNull NativeWebRequest webRequest,
                                WebDataBinderFactory binderFactory) {

    var annotation = parameter.getParameterAnnotation(DynamicArgsParam.class);
    if(annotation == null) {
      return null;
    }
    boolean searchable = annotation.search();

    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    assert request != null;

    Map<String, Pair<Object, String>> params = QueryStringConverter.apply(request, annotation);

    return new QueryString(params).search(searchable);
  }
}
