package repository;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import model.productmovement.ProductStatusChangeReason;
import model.productmovement.ProductStatusChangeReasonCategory;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test using DBUnit to test the DiscardReasonRepository
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:**/applicationContextTest.xml")
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@WebAppConfiguration
public class DiscardReasonRepositoryTest {
	
	@Autowired
	DiscardReasonRepository discardReasonRepository;
	
	@Autowired
	private DataSource dataSource;
	
	private IDataSet getDataSet() throws Exception {
		File file = new File("test/dataset/DiscardReasonRepositoryDataset.xml");
		return new FlatXmlDataSetBuilder().setColumnSensing(true).build(file);
	}
	
	private IDatabaseConnection getConnection() throws SQLException {
		IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource);
		DatabaseConfig config = connection.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
		return connection;
	}
	
	@Before
	public void init() throws Exception {
		IDatabaseConnection connection = getConnection();
		try {
			IDataSet dataSet = getDataSet();
			DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
		}
		finally {
			connection.close();
		}
	}

	@After
	public void after() throws Exception {
		IDatabaseConnection connection = getConnection();
		try {
			IDataSet dataSet = getDataSet();
			DatabaseOperation.DELETE_ALL.execute(connection, dataSet);
		}
		finally {
			connection.close();
		}
	}
	
	@Test
	@Transactional
	public void testGetAll() throws Exception {
		List<ProductStatusChangeReason> all = discardReasonRepository.getAllDiscardReasons();
		Assert.assertNotNull("There are discard reasons defined", all);
		Assert.assertEquals("There are 6 discard reasons defined", 6, all.size());
	}
	
	@Test
	@Transactional
	public void testGetDiscardReasonById() throws Exception {
		ProductStatusChangeReason discardReason = discardReasonRepository.getDiscardReasonById(1);
		Assert.assertNotNull("Discard reason with id 1 exists", discardReason);
	}
	
	@Test
	@Transactional
	public void testFindDeferralReason() throws Exception {
		ProductStatusChangeReason reason = discardReasonRepository.findDiscardReason("Incomplete Donation");
		Assert.assertNotNull("Discard reason exists", reason);
		Assert.assertEquals("Discard reason matches", "Incomplete Donation", reason.getStatusChangeReason());
	}
	
	@Test
	@Transactional
	public void testFindDeferralReasonUnknown() throws Exception {
		ProductStatusChangeReason reason = discardReasonRepository.findDiscardReason("Junit");
		Assert.assertNull("Discard reason does not exist", reason);
	}
	
	@Test
	@Transactional
	@Ignore("Because this test updates data and rollback is not working correctly, DBUnit hangs when cleaning up the database")
	public void testUpdateDeferralReason() throws Exception {
		ProductStatusChangeReason reason = discardReasonRepository.getDiscardReasonById(1);
		Assert.assertNotNull("Discard reason exists", reason);
		
		reason.setStatusChangeReason("Junit");
		discardReasonRepository.updateDiscardReason(reason);
		
		ProductStatusChangeReason savedReason = discardReasonRepository.getDiscardReasonById(1);
		Assert.assertNotNull("Discard reason still exists", savedReason);
		Assert.assertEquals("Reason has been updated", "Junit", savedReason.getStatusChangeReason());
	}
	
	@Test
	@Transactional
	@Ignore("Because this test inserts data and rollback is not working correctly, DBUnit hangs when cleaning up the database")
	public void testSaveDeferralReason() throws Exception {
		ProductStatusChangeReason reason = new ProductStatusChangeReason();
		reason.setStatusChangeReason("Junit");
		reason.setCategory(ProductStatusChangeReasonCategory.DISCARDED);
		discardReasonRepository.saveDiscardReason(reason);
		
		List<ProductStatusChangeReason> all = discardReasonRepository.getAllDiscardReasons();
		Assert.assertNotNull("There are Discard reasons defined", all);
		Assert.assertEquals("There are 7 Discard reasons defined", 7, all.size());
	}
}
