/**
 * Dao implementation for Hibernate 3.x persistence layer
 */
package de.geobe.architecture.persist;

// Insert any imports here

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Encapsulate all database operations with hibernate persistence layer
 * 
 * @author: georg beier
 * 
 * @param <PersistType>
 *            a persistet type that is handled by this dao
 */
@SuppressWarnings("deprecated")
public class DaoHibernate<PersistType> implements DataAccess<PersistType> {

	// Attribute Definitions

	@SuppressWarnings("rawtypes")
	private Class accessedType;

	private DbHibernate dbAccess;

	// Constructors

	/**
	 * create new Dao object
	 * 
	 * @param persistType
	 *            The class of the PersistType. Must be given because Java
	 *            eliminates generic type parameters at compile time and
	 *            Hibernate needs type information at runtime!
	 * @param dbac
	 *            The Hibernate database access object
	 */
	@SuppressWarnings("rawtypes")
	public DaoHibernate(Class persistType, DbHibernate dbac) {
		accessedType = persistType;
		dbAccess = dbac;
	}

	// Operations

	// Hibernate specific (?) operations

	/**
	 * commits a transaction on all daos in this thread that are linked to the
	 * same dbAccess
	 */
	public void commit() {
		Session s = dbAccess.getActiveSession();
		Transaction t = s.getTransaction();
		if (t != null) {
			t.commit();
		}
	}

	/**
	 * performs a rollback on all daos in this thread that are linked to the
	 * same dbAccess
	 */
	public void rollback() {
		Session s = dbAccess.getActiveSession();
		Transaction t = s.getTransaction();
		if (t != null) {
			t.rollback();
		}
	}

	/**
	 * closes the database session on this dao's connection to the database.
	 * <br>
	 * affects all daos in the same thread that are linked to the same database
	 * connection. <br>
	 * closing the session will commit open transaction, 
	 * @see de.geobe.architecture.persist.DbHibernate#closeSession()
	 */
	public void closeSession() {
		dbAccess.closeSession();
	}

	// Implemented abstract operations

	/**
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#save(Object)
	 * also starts a transaction, if none is active
	 */
	public boolean save(PersistType obj) {
		Session s = dbAccess.getActiveSession();
		try {
			s.saveOrUpdate(obj);
			return true;
		} catch (StaleObjectStateException stex) {
			s.refresh(obj);
			return false;
		} catch (RuntimeException rex) {
			Transaction t = s.getTransaction();
			if (t != null) {
				t.rollback();
			}
			s.close();
			throw rex;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#fetch(java.io.Serializable)
	 * also starts a transaction, if none is active
	 */
	@SuppressWarnings("unchecked")
	public PersistType fetch(Serializable id) {
		Session s = dbAccess.getActiveSession();
		PersistType obj = (PersistType) s.get(accessedType, id);
		return obj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#fetchAll()
	 * also starts a transaction, if none is active
	 */
	@SuppressWarnings("unchecked")
	public List<PersistType> fetchAll() {
		Session s = dbAccess.getActiveSession();
		List<PersistType> result = (List<PersistType>) s.createQuery(
				"from " + accessedType.getCanonicalName()).list();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#find(java.lang.String)
	 * also starts a transaction, if none is active
	 */
	@SuppressWarnings("unchecked")
	public List<PersistType> find(String query) {
		Session s = dbAccess.getActiveSession();
		List<PersistType> result = (List<PersistType>) s.createQuery(query)
				.list();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#find(java.lang.String,
	 *      java.util.Map)
	 * also starts a transaction, if none is active
	 */
	@SuppressWarnings("unchecked")
	public List<PersistType> find(String query, Map<String, Object> params) {
		Session s = dbAccess.getActiveSession();
		Query hibernateQuery = s.createQuery(query);
		for (String pname : params.keySet()) {
			hibernateQuery.setParameter(pname, params.get(pname));
		}
		List<PersistType> result = (List<PersistType>) hibernateQuery.list();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#findByExample(java.lang.Object)
	 * also starts a transaction, if none is active
	 */
	public List<PersistType> findByExample(PersistType sample) {
		return findByExample(sample, new ArrayList<String>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#findByExample(java.lang.Object,
	 *      java.util.Collection)
	 * also starts a transaction, if none is active<br>
	 * We are using the deprecated hibernate criteria as the recommended JPA criteria solution
	 * is really arcane and unhandy for Query By Example implementation.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public List<PersistType> findByExample(PersistType sample,
			Collection<String> excluded) {
		Example qbe = Example.create(sample).ignoreCase().excludeZeroes()
				.enableLike(MatchMode.ANYWHERE);
		for (String excludedProperty : excluded) {
			qbe.excludeProperty(excludedProperty);
		}
		Session s = dbAccess.getActiveSession();
//		CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
//		CriteriaQuery<PersistType> criteria = criteriaBuilder.createQuery(accessedType);
//		Root<PersistType> root = criteria.from(accessedType);
		return (List<PersistType>) s.createCriteria(accessedType).add(qbe)
				.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#delete(java.lang.Object)
	 * also starts a transaction, if none is active
	 */
	public void delete(PersistType obj) {
		Session s = dbAccess.getActiveSession();
		s.delete(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.geobe.architecture.persist.DataAccess#deleteAll()
	 * also starts a transaction, if none is active
	 */
	public void deleteAll() {
		Session s = dbAccess.getActiveSession();
		s.createQuery("delete " + accessedType.getCanonicalName())
				.executeUpdate();
	}

	// Attribute Accessors

	@SuppressWarnings("rawtypes")
	public Class getAccessedType() {
		return accessedType;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	private void setAccessedType(Class pAccessedType) {
		accessedType = pAccessedType;
	}

	public DbHibernate getDbAccess() {
		return dbAccess;
	}

	@SuppressWarnings("unused")
	private void setDbAccess(DbHibernate pDbAccess) {
		dbAccess = pDbAccess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(accessedType: " + getAccessedType() + ")" + "(dbAccess: "
				+ getDbAccess() + ")";
	}

}
