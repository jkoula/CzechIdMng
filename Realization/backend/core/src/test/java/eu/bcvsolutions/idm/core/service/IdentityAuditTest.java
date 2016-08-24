package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Test audit configuration on identity entity
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class IdentityAuditTest extends AbstractIntegrationTest {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	private IdmIdentityWorkingPositionRepository identityWorkingPositionRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	private TransactionTemplate template;
	private IdmIdentity identity;
	private IdmRole role = null;

	@Before
	public void transactionTemplate() {
		template = new TransactionTemplate(platformTransactionManager);
		identity = constructTestIdentity();
		loginAsAdmin("admin");
	}
	
	@After
	@Transactional
	public void deleteIdentity() {
		// we need to ensure "rollback" manually the same as we are starting transaction manually		
		identityRepository.delete(identity);
		if (role != null) {
			roleRepository.delete(role);
			role = null;
		}
		logout();
	}

	@Test
	public void testCreateIdentity() {
		identity = saveInTransaction(identity, identityRepository);

		assertNotNull(identity.getId());

		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(1, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
			}
		});	
	}
	
	@Test
	public void testUpdateIdentity() {
		identity = saveInTransaction(identity, identityRepository);
		identity.setFirstName("One"); 
		identity = saveInTransaction(identity, identityRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(2, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
				
			}
		});			
	}	

	@Test
	public void testWorkingPositionChange() {
		identity = saveInTransaction(identity, identityRepository);
		
		IdmIdentityWorkingPosition position = new IdmIdentityWorkingPosition();
		position.setIdentity(identity);
		position.setPosition("one");
		
		saveInTransaction(position, identityWorkingPositionRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(2, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
				
			}
		});
	}

	@Test
	public void testAssignedRoleChanges() {
		identity = saveInTransaction(identity, identityRepository);
		
		role = new IdmRole();
		role.setName("audit_role");
		
		role = saveInTransaction(role, roleRepository);
		
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(role);
		
		saveInTransaction(identityRole, identityRoleRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(2, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
				
			}
		});
	}
	
	private IdmIdentity constructTestIdentity() {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("audit_test_user");
		identity.setLastName("Auditor");
		return identity;
	}

	private <T extends BaseEntity> T saveInTransaction(final T object, final BaseRepository<T> repository) {
		return template.execute(new TransactionCallback<T>() {
			public T doInTransaction(TransactionStatus transactionStatus) {
				return repository.save(object);
			}
		});
	}

}
