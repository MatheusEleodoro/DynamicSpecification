package br.com.evertec.sinqia.contabil.specification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * JPARepository paradrão para uso com o SinqiaFilter
 *
 * @author <a href="https://contacts.google.com/matheus.souza@.sinqia.com.br">Matheus Eleodoro</a>
 * @version 1.0
 * @apiNote <a href="https://tfs.seniorsolution.com.br/SQcwb/SQContabilidade/_git/sqct-contabilidade-rest?version=GBdevelop&_a=contents/">...</a>
 */
@NoRepositoryBean
public interface SQRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
