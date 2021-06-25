/**
 * Hibernate Database access
 */
package de.geobe.architecture.persist;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * @author: georg beier
 */
//@SuppressWarnings("deprecation")
@SuppressWarnings("deprecation")
public class DbHibernate {

	// Attribute Definitions

	private static Configuration configuration;

	private static SessionFactory sessionFactory;

	private Session activeSession;

	// Operations

	/**
	 */
	public static void closeDatabase() {
		sessionFactory.close();
	}

	// Implemented abstract operations

	/**
	 * open a session and associate it with a transaction
	 */
	private Session openSession() {
		activeSession = sessionFactory.openSession();
		return activeSession;
	}

	/**
	 * close active session and automatically commit open transactions
	 */
	public void closeSession() {
		if (activeSession != null && activeSession.isOpen()) {
			Transaction t = activeSession.getTransaction();
			if (t.isActive()) {
				t.commit();
			}
			activeSession.close();
		}
	}

	// Attribute Accessors

	/**
	 * create a new session if necessary. also start a transaction if necessary.
	 * 
	 * @return an open session
	 */
	public Session getActiveSession() {
		if (activeSession == null || !activeSession.isOpen()) {
			openSession();
		}
		Transaction t = activeSession.getTransaction();
		if (t == null || !t.isActive()) {
			activeSession.beginTransaction();
		}
		return activeSession;
	}

	public String toString() {
		return "(activeSession: " + getActiveSession() + ")";
	}

	static {
		try {
			configuration = new org.hibernate.cfg.Configuration();
			configuration.configure("hibernate.cfg.xml");
			sessionFactory = configuration.buildSessionFactory();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Hibernate Exception", ex);
		}
	}
}
