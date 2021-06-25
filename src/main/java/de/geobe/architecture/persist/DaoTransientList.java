/**
 * 
 */
package de.geobe.architecture.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author georg beier
 * 
 */
public class DaoTransientList<PersistType> implements DataAccess<PersistType> {

	/**
	 * transient store for domain objects
	 */
	private static Map<Class<? extends Object>, LinkedHashSet<Object>> objects =
			new HashMap<Class<? extends Object>, LinkedHashSet<Object>>();

	public static Set<Object> getObjects(Class<? extends Object> cl) {
		return objects.get(cl);
	}

	public static void clearObjectStore() {
		objects = new HashMap<Class<? extends Object>, LinkedHashSet<Object>>();
	}
	
	public static void clear() {
		for (Set<Object> set : objects.values()) {
			set.clear();
		}
	}

	private Class<? extends Object> key;

	public DaoTransientList(Class<? extends Object> clazz) {
		key = clazz;
		if (!objects.containsKey(key)) {
			objects.put(key, new LinkedHashSet<Object>());
		}
	}

	@Override
	public boolean save(PersistType obj) {
		return getStore().add(obj);
	}

	@Override
	public PersistType fetch(Serializable id) {
		if (id instanceof Number) {
			int hash = ((Number) id).intValue();
			for (PersistType p : getStore()) {
				if (p.hashCode() == hash)
					return p;
			}
		}
		return null;
	}

	@Override
	public List<PersistType> fetchAll() {
		return new ArrayList<PersistType>(getStore());
	}

	@Override
	public List<PersistType> find(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PersistType> find(String query, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PersistType> findByExample(PersistType sample) {
		ArrayList<PersistType> found = new ArrayList<PersistType>();
		return found;
	}

	@Override
	public List<PersistType> findByExample(PersistType sample,
			Collection<String> excluded) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(PersistType obj) {
		getStore().remove(obj);
	}

	@Override
	public void deleteAll() {
		getStore().clear();
	}

	@SuppressWarnings("unchecked")
	private Set<PersistType> getStore() {
		return (Set<PersistType>) objects.get(key);
	}

}
