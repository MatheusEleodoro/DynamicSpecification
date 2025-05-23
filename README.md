# Dynamic Specification! `1.0.1`

## O que é? / What is it?
É um conjunto de funcionalidades que permite configurar e habilitar filtros dinâmicos em APIs Java Spring. As implementações contidas aqui permitem a criação simplificada de consultas e queries complexas com uma implementação muito fácil.
- - -
It is a set of functionalities that allows configuring and enabling dynamic filters in Java Spring APIs. The implementations contained here enable simplified creation of complex queries with a very easy implementation.


## Recursos / Features
- 🔎 **DynamicArgs:** Um recurso bastante comum em serviços na internet, agora habilitado para uso em suas APIs Java.
- 💻 **DynamicFilter:** É possível criar filtros dinâmicos para consultas sem a necessidade de múltiplas queries ou condições de ifs para atender possibilidades de parâmetros.
- ♾️ **DynamicRepository:** Repositório unificado do JPARepository e JpaSpecificationExecutor.<br>
- - -
- 🔎 **DynamicArgs:** A commonly used feature in web services, now enabled for use in your Java APIs.
- 💻 **DynamicFilter:** It's possible to create dynamic filters for queries without the need for multiple queries or if conditions to handle parameter possibilities.
- ♾️ **DynamicRepository:** Unified repository of JPARepository and JpaSpecificationExecutor.



## Como usar / How to Use
Para começar a utilizar o Dynamic Specification em seu projeto, adicione a seguinte dependência ao seu arquivo `pom.xml`:

To start using the Dynamic Specification in your project, add the following dependency to your `pom.xml` file:


```xml
<dependency>
    <groupId>com.eleodorodev</groupId>
    <artifactId>dynamic-specification</artifactId>
    <version>1.0.1</version>
</dependency>
```
### Crie seu repositório de uma das formas abaixo <br> Create your repository in one of the ways below


```java
// Utilizando o DynamicRepository / Using DynamicRepository
import com.eleodorodev.specification.DynamicRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountriesRepository extends DynamicRepository<Countries, Long> {
}
```
```java
// Utilizando o JpaRepository e JpaSpecificationExecutor / Using o JpaRepository e JpaSpecificationExecutor
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CountriesRepository extends JpaRepository<Countries, Long>, JpaSpecificationExecutor<Countries> {
}
```


## Exemplos de Uso / Usage Examples

Para utilizar o Dynamic Specification em seu código Java Spring, siga os passos abaixo:\
To use the Dynamic Specification in your Java Spring code, follow the steps below:

### Filtros Dinâmicos / Dynamic Filters
- Os métodos possuem os seguintes parametros principais;
- - `compare` - Representa o objeto com valor a ser comparado 
- - `attribute` - Representa o nome do atributo da `@Entity` que sera comparado Exemplo:"id"
- - `parents` - <`Opcional`> Lista com o caminho onde está seu attribute, por exemplo caso seu atributo id seja o id de um outro objeto.<br> você deve especificar o nome do objeto onde id se encontra.
___
- The methods have the following main parameters;
- - `compare` - Represents the object with value to be compared
- - `attribute` - Represents the name of the attribute of the `@Entity` that will be compared Example: "id"
- - `parents` - <`Optional`> List with the path where your attribute is, for example if your id attribute is the id of another object.<br> you must specify the name of the object where id is located.


Veremos 3 formas de implementação\
We will see 3 ways of implementation

Forma 1:\
Option 1:
```java 
/** 
 * Implementação com filtros pré definidos pelo desenvolvedor
 * Nesse exemplo criamos uma consulta que pode ser filtras por id, pelo nome ou pela população
 * Dessa forma podemos informar ou não algum desses paremtros e a consulta sera feita com base neles ou não
 * Isso remove a necessidade de tratar com if-else cada possibilidade ou varias querys 
 */

/**
 * Implementation with filters predefined by the developer
 * In this example we create a query that can be filtered by id, name or population
 * This way we can inform or not any of these parameters and the query will be made based on them or not
 * This removes the need to use if-else for each possibility or multiple queries
 */
public Collection<Countries> exemple(@RequestBody(required = false) Form form) {
    return repository.findAll(DynamicSpecification.<Countries>
                    where(DynamicFilter.toEquals(form.id(), "id"))
            .and(DynamicFilter.toLike(form.name(), "name"))
            .and(DynamicFilter.toGreater(form.population(), "population")));
}
```
Forma 2:\
Option 2
```java
// Na sua Entity ou DTO use a anotação @DynamicSpecAttr para configurar os as propriedades do filtro
// In your Entity or DTO, use the @DynamicSpecAttr annotation to configure the filter properties
@Entity
@Table(name = "countries")
public class Countries {

    @Id
    @DynamicSpecAttr(property = "id")
    private long id;

    @DynamicSpecAttr(property = "name", conditional = Conditional.LK, conjunction = Conjunction.AND)
    @Column(name = "name")
    private String name;

    @DynamicSpecAttr(property = "population", conditional = Conditional.GT)
    @Column(name = "population")
    private String population;
}
```
```java
// Na sua classe que receberá os parametros da requisição extenda a classe BaseDynamicFilter
// In your class that will receive the request parameters, extend the BaseDynamicFilter class
public class Form extends BaseDynamicFilter {
}
```
```java
// Chame o DynamicSpecification.bind e passar como parametro a classe onde foi configurado seus @DynamicSpecAttr 
// E passe a classe que extendeu BaseDynamicFilter chamando o método toDynamicArgs()

// Call DynamicSpecification.bind and pass as a parameter the class where your @DynamicSpecAttr 
// And pass the class that extended BaseDynamicFilter by calling the toDynamicArgs() method
public Collection<Countries> exemple(@RequestBody(required = false) Form form) {
    return repository.findAll(DynamicSpecification.bind(Countries.class,form.toDynamicArgs()));
}
```
```json5
{
  // Exemplo de um input
  // Example of an input
    "id": null,
    "name":"USA",
    "population": null
}
```
Forma 3:\
Option 3
> ✅ **Observação:** Essa forma de implementação trás a facilidade flexibilidade de permitir ao usuario fazer pesquisa conforme suas necessides aplicando seus proprios filtros.<br><br>
**Note:** This form of implementation brings the flexibility of allowing the user to search according to their needs by applying their own filters.

```java
// Na sua classe que receberá os parametros da requisição extenda a classe BaseDynamicFilter
// In your Entity or DTO use the @DynamicSpecAttr annotation to configure the filter properties
@Entity
@Table(name = "countries")
public class Countries {

    @Id
    @DynamicSpecAttr(property = "id")
    private long id;

    @DynamicSpecAttr(property = "name", conditional = Conditional.LK, conjunction = Conjunction.AND)
    @Column(name = "name")
    private String name;

    @DynamicSpecAttr(property = "population", conditional = Conditional.GT)
    @Column(name = "population")
    private String population;
}
```

```java
// Faça o seguinte import
// Do the following import
```

```java
// Em seguida ao invés de usar um @RequestBody usaremos um @RequestParam do tipo DynamicArgs
// Chame o DynamicSpecification.bind e passe como parametro a classe onde foi configurado seus @DynamicSpecAttr
// E a dynamicArgs

// Then instead of using a @RequestBody we will use a @RequestParam of type DynamicArgs
// Call DynamicSpecification.bind and pass as a parameter the class where your @DynamicSpecAttr was configured
// And the dynamicArgs
public Collection<Countries> exemple(@DynamicArgsParam DynamicArgs dynamicArgs) {
    return repository.findAll(DynamicSpecification.bind(Countries.class,dynamicArgs));
}
```
Dessa forma agora podemos usar os filtros por parametros de url da seguinte forma\
This way, we can now use the filters by url parameters as follows
```http request
// Buscando Countries com id igual a 1 e nome que contenha Usa
// Searching for Countries with id equal to 1 and name that contains Usa
localhost:8082/test?q=id=1&name=Usa
```
Podemos dar ainda mais liberdade e dinamismo na consulta permitindo que o endpoint aceite a pesquisa completa via url
dessa forma o usuario poderar chamar a url e filtro da forma que quiser, usando os filtros and,or, equals, between, contains, etc.<br><br>
We can provide even more freedom and dynamism in the query by allowing the endpoint to accept the complete search via url
This way, the user can call the url and filter however they want, using the and, or, equals, between, contains, etc. filters.

```java
// Basta chamar o dynamicArgs.searchURL(true) para habilitar 
public Collection<Countries> exemple(@DynamicParam(search = true) DynamicArgs dynamicArgs) {
    return repository.findAll(DynamicSpecification.bind(Countries.class,dynamicArgs));
}
```
```http request
// Buscando Countries com id entre (bw) 1 e 10  e população maior ou igual (gte) que 212000000
// Searching for Countries with id between (bw) 1 and 10 and population greater than or equal (gte) than 212000000
localhost:8082/test?q=id=1,10;bw&population=212000000;gte
```
## Descrição de atributos / Attribute description

### @DynamicSpecAttr
Anotação para configurar as colunas que farão parte do filtro
Annotation to configure the columns that will be part of the filter

- `property` - nome da propriedade da entidade ou dto que ela esta anotada <br>
- `alias` - apelido para a propriedade
- `parents` - <`Opcional`> Lista com o caminho onde está seu attribute Ex. {"objA",objB} significa que o property está dentro do objB que por vez se encontra dentro do objA,
- `conjunction` - tipo de conjunção que a o atributo padrão é AND
- `conditional` - condicional que a consulta por aquele atributo será feita padrão é EQ
- `negate` - booleano indica se será uma consulta de negação ou não
- 
---
- `property` - name of the entity's property or what it is annotated with <br>
- `alias` - nickname for the property
- `parents` - <`Optional`> List with the path where your attribute is Ex. {"objA",objB} means that the property is inside objB which in turn is inside objA,
- `conjunction` - type of conjunction whose default attribute is AND
- `conditional` - conditional that the query for that attribute will be done, default is EQ
- `negate` - boolean indicates whether it will be a deny query or not



### Conditional(Enum)
Representa operadores lógicos de SQL
Represents logical SQL operators
- `EQ("Equals")`: "="
- `LK ("Like")`: LIKE %valor%
- `CT ("Contains")`: IN (1,2,3)
- `BW ("Between")`: BETWEEN 1 and 3
- `GT ("GreaterThan")`: > 3
- `LT ("LessThan")`: < 3
- `LTE ("LessThanEqualTo")`: <= 3
- `GTE ("GreaterThanEqualTo")`: >= 3
- `NOT ("Negate")`: NOT 


## Conjunction
Representa os operadores de junção de clausulas WHERE AND,OR
Represents the clause joining operators WHERE AND,OR
- `AND("And")`,
- `OR("Or")`;


## Youtube
[![Youtube](https://img.youtube.com/vi/OmacOzLMkmM/0.jpg)](https://www.youtube.com/watch?v=OmacOzLMkmM)
