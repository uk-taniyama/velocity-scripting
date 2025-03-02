/*
 *    Copyright 2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.scripting.doma;

import java.util.function.Function;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.session.Configuration;

@FunctionalInterface
public interface ParameterResolver extends Function<String, Object> {
  static final ParameterResolver nullResolver = (name) -> null;

  // TODO FIXME parameterTypeClass???

  public static ParameterResolver of(
      Configuration configuration, Object parameterObject, Class<?> parameterTypeClass) {
    if (parameterObject == null) {
      return nullResolver;
    }

    MetaObject metaObject = configuration.newMetaObject(parameterObject);
    boolean existsTypeHandler =
        configuration.getTypeHandlerRegistry().hasTypeHandler(parameterTypeClass);
    return new BeanResolver(metaObject, existsTypeHandler);
  }

  class BeanResolver implements ParameterResolver {
    private final MetaObject parameterMetaObject;
    private final boolean fallbackParameterObject;

    BeanResolver(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
      this.parameterMetaObject = parameterMetaObject;
      this.fallbackParameterObject = fallbackParameterObject;
    }

    @Override
    public Object apply(String key) {
      try {
        if (fallbackParameterObject && !parameterMetaObject.hasGetter(key)) {
          return parameterMetaObject.getOriginalObject();
        } else {
          return parameterMetaObject.getValue(key);
        }
      } catch (ReflectionException e) {
        return null;
      }
    }
  }
}
