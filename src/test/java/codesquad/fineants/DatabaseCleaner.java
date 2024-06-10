package codesquad.fineants;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
@Profile("test")
public class DatabaseCleaner implements InitializingBean {

	@PersistenceContext
	private EntityManager entityManager;

	private final List<String> tableNames = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void findDatabaseTableNames() {
		List<Object> tableInfos = entityManager.createNativeQuery("SHOW TABLES").getResultList();
		for (Object tableInfo : tableInfos) {
			String tableName = (String)tableInfo;
			tableNames.add(tableName);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		findDatabaseTableNames();
	}

	@Transactional
	public void clear() {
		entityManager.clear();
		truncate();
	}

	private void truncate() {
		entityManager.createNativeQuery(String.format("SET FOREIGN_KEY_CHECKS = %d", 0)).executeUpdate();
		for (String tableName : tableNames) {
			entityManager.createNativeQuery(String.format("TRUNCATE TABLE %s", tableName)).executeUpdate();
		}
		entityManager.createNativeQuery(String.format("SET FOREIGN_KEY_CHECKS = %d", 1)).executeUpdate();
	}
}
