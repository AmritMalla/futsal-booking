package com.amrit.futsal.config;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*@EnableJpaRepositories --> Spring will scan for repositories to add the Spring Application Context
 JpaRepositories extend:
	 org.springframework.data.repository.interface
	 Crudrepository<T,ID extends Serializable>.*/

/*
CRUD Repository
A CRUD (create, read, update, and delete) repository provides the following:
	1. Saves the given entity
	2. Returns the entity identified by the givevn id
	3. Returns all entities
	4. Returns the number of entities
	5. Deletes the given entity
	6. Indicates whether an entity with the given id exists
	*/

/*Java Entity
Implemented as a annotated Java class which has Object relational mapping to the database taables
Typically represents a table in the relational database
*/
/*
Enabling Transaction Management
@EnableTransactionManagement sets up transaction management.
It registers the necessary Spring components that powers annotation-driven transaction management such as
TransactionInterceptor or Aspect J based advice
*/
@EnableJpaRepositories("com.amrit.futsal.repository")
@EnableTransactionManagement
public class DatabaseConfig {


}
