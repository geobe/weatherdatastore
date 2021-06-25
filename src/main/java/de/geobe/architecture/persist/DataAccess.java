/**
 * Generic Dao interface
 */
package de.geobe.architecture.persist;

import java.io.Serializable;
import java.util.*; // always good to have ;-)

/**
 * @author: georg beier
 * 
 * @param <PersistType>
 *            Generic parameter for Dao
 */
public interface DataAccess<PersistType> {

	/**
	 * save object to persistent storage
	 * 
	 * @param obj
	 *            object to be saved
	 * @return true if successful, false if object is "stale" (i.e. object was
	 *         changed by an other thread or process)
	 */
	boolean save(PersistType obj);

	/**
	 * Fetch object from persistent storage
	 * 
	 * @param id
	 *            key property of object
	 * @return object, if found, else null
	 */
	PersistType fetch(Serializable id);

	/**
	 * Fetch all objects of PersistType in persistent storage
	 * 
	 * @return list of all objects
	 */
	List<PersistType> fetchAll();

	/**
	 * executes hibernate query <br>
	 * (e.g. select address from person p join p.address)
	 * 
	 * @param query
	 *            simple HQL query string
	 * @return list of returned objects
	 */
	List<PersistType> find(String query);

	/**
	 * executes hibernate query with parameters <br>
	 * (e.g. from person p with p.name = :name)
	 * 
	 * @param query
	 *            HQL query string containing named parameters in hibernate
	 *            style (e.g. :name)
	 * @param params
	 *            map of actual parameters with parameter name as key (without :)
	 *            and actual parameter value as value
	 * @return list of returned objects
	 */
	List<PersistType> find(String query, Map<String, Object> params);

	/**
	 * Query by example. Find objects that are "similar" to the sample object.<br>
	 * String properties are matched with <i>like</i>, so SQL wildcards (%) can
	 * be used.
	 * 
	 * @param sample
	 *            a sample object
	 * @return list of objects that conform to sample in all not null properties
	 */
	List<PersistType> findByExample(PersistType sample);

	/**
	 * Query by example. Find objects that are "similar" to the sample object.<br>
	 * String properties are matched with <i>like</i>, so SQL wildcards (%) can
	 * be used.
	 * 
	 * @param sample
	 *            a sample object
	 * @param excluded
	 *            properties not considered im matching
	 * @return list of objects that conform to sample in all not null properties
	 */
	List<PersistType> findByExample(PersistType sample,
			Collection<String> excluded);

	/**
	 * Delete object from persistent storage
	 * 
	 * @param obj
	 *            object to be deleted
	 */
	void delete(PersistType obj);

	/**
	 * Delete all objects of PersistType from persistent storage
	 */
	void deleteAll();
}
