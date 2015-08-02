package repository;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.sql.DataSource;

import model.donation.Donation;
import model.donationbatch.DonationBatch;
import model.location.Location;

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
 * Test using DBUnit to test the DonationBatchRepository
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:**/applicationContextTest.xml")
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@WebAppConfiguration
public class DonationBatchRepositoryTest {
	
	@Autowired
	DonationBatchRepository donationBatchRepository;
	
	@Autowired
	LocationRepository locationRepository;
	
	@Autowired
	private DataSource dataSource;
	
	private IDataSet getDataSet() throws Exception {
		File file = new File("test/dataset/DonationBatchRepositoryDataset.xml");
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
	public void testFindDonationBatchById() throws Exception {
		DonationBatch one = donationBatchRepository.findDonationBatchById(1);
		Assert.assertNotNull("There is a donation batch with the id 1", one);
		Assert.assertEquals("The donation batch has the number 'B0215000000'", "B0215000000", one.getBatchNumber());
	}
	
	@Test
	@Transactional
	public void testGetRecentlyClosedDonationBatches() throws Exception {
		List<DonationBatch> closed = donationBatchRepository.getRecentlyClosedDonationBatches(5);
		Assert.assertNotNull("There are recently closed donation batches", closed);
		// NOTE: This includes deleted donation batches.
		Assert.assertEquals("There are 3 recently closed donation batches", 3, closed.size());
	}
	
	@Test
	@Transactional
	public void testFindDonationBatchByBatchNumber() throws Exception {
		DonationBatch one = donationBatchRepository.findDonationBatchByBatchNumber("B0215000000");
		Assert.assertNotNull("There is a donation batch with the number 'B0215000000'", one);
		Assert.assertEquals("The donation batch has the number 'B0215000000'", "B0215000000", one.getBatchNumber());
	}
	
	@Test
	@Transactional
	public void testFindDonationsInBatch() throws Exception {
		List<Donation> donations = donationBatchRepository.findDonationsInBatch(1);
		Assert.assertNotNull("There donations in the batch with id 1", donations);
		Assert.assertEquals("There is 1 donation in the batch with id 1", 1, donations.size());
	}
	
	@Test
	@Transactional
	@Ignore("Because this test inserts data and rollback is not working correctly, DBUnit hangs when cleaning up the database")
	public void testAddDonationBatches() throws Exception {
		DonationBatch donationBatch = new DonationBatch();
		donationBatch.setBatchNumber("JUNIT123");
		donationBatch.setCreatedDate(new Date());
		donationBatch.setLastUpdated(new Date());
		donationBatch.setIsDeleted(false);
		donationBatch.setIsClosed(true);
		donationBatch.setNotes("Testing 123");
		Location location = locationRepository.findLocationByName("Maseru");
		donationBatch.setDonorPanel(location);
		donationBatchRepository.addDonationBatch(donationBatch);
		
		DonationBatch savedDonationBatch = donationBatchRepository.findDonationBatchByBatchNumber("JUNIT123");
		Assert.assertNotNull("There is a donation batch with the number 'JUNIT123'", savedDonationBatch);
		Assert.assertEquals("The donation batch has the number 'JUNIT123'", "JUNIT123", savedDonationBatch.getBatchNumber());
	}
	
	@Test
	@Transactional
	@Ignore("Because this test updates data and rollback is not working correctly, DBUnit hangs when cleaning up the database")
	public void testUpdateDonationBatch() throws Exception {
		DonationBatch one = donationBatchRepository.findDonationBatchByBatchNumber("B0215000000");
		Assert.assertNotNull("There is a donation batch with the number 'B0215000000'", one);
		one.setNotes("Testing 123");
		donationBatchRepository.updateDonationBatch(one);
		
		DonationBatch savedOne = donationBatchRepository.findDonationBatchByBatchNumber("B0215000000");
		Assert.assertEquals("The donation batch has updated notes", "Testing 123", savedOne.getNotes());
	}
	
	@Test
	@Transactional
	@Ignore("Because this test inserts data and rollback is not working correctly, DBUnit hangs when cleaning up the database")
	public void testFindUnassignedDonationBatches() throws Exception {
		List<DonationBatch> unassigned = donationBatchRepository.findUnassignedDonationBatches();
		Assert.assertNotNull("Should not return a null list", unassigned);
		Assert.assertEquals("There are no unassigned donation batches", 0, unassigned.size());
		
		// create an unassigned batch
		DonationBatch donationBatch = new DonationBatch();
		donationBatch.setBatchNumber("JUNIT123");
		donationBatch.setCreatedDate(new Date());
		donationBatch.setLastUpdated(new Date());
		donationBatch.setIsDeleted(false);
		donationBatch.setIsClosed(true);
		donationBatchRepository.addDonationBatch(donationBatch);
		
		unassigned = donationBatchRepository.findUnassignedDonationBatches();
		Assert.assertNotNull("TShould not return a null list", unassigned);
		Assert.assertEquals("There is 1 unassigned donation batch", 1, unassigned.size());
		DonationBatch savedDonationBatch = unassigned.get(0);
		Assert.assertEquals("The donation batch has the number 'JUNIT123'", "JUNIT123", savedDonationBatch.getBatchNumber());
	}
	
	@Test
	@Transactional
	public void testFindDonationBatchByBatchNumberIncludeDeleted() throws Exception {
		try {
			donationBatchRepository.findDonationBatchByBatchNumber("B0715000000");
			Assert.fail("Donation batch with the number 'B0415000000' is deleted");
		}
		catch (NoResultException e) {
			// expected exception
		}
		DonationBatch two = donationBatchRepository.findDonationBatchByBatchNumberIncludeDeleted("B0715000000");
		Assert.assertNotNull("There is a donation batch with the number 'B0715000000'", two);
		Assert.assertEquals("The donation batch has the number 'B0715000000'", "B0715000000", two.getBatchNumber());
	}
	
	@Test
	@Transactional
	public void testFindDonationBatches() throws Exception {
		List<Long> locationIds = new ArrayList<Long>();
		locationIds.add(1L);
		List<DonationBatch> batches = donationBatchRepository.findDonationBatches(true, locationIds);
		Assert.assertNotNull("There are batches in Maseru", batches);
		Assert.assertEquals("There are 1 donation batches in Maseru", 1, batches.size());
	}
}
