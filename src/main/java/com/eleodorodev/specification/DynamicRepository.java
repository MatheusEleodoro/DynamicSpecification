package com.eleodorodev.specification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * DynamicRepository Repository
 *
 * @author <a href="https://github.com/MatheusEleodoro">Matheus Eleodoro</a>
 * @version 1.0.0
 * @apiNote A JPARepository compatible with Specification implementations
 * @see  <a href="https://github.com/MatheusEleodoro">...</a>
 */
@NoRepositoryBean
public interface DynamicRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
