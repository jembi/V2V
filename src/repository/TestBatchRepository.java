package repository;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import model.donationbatch.DonationBatch;
import model.testbatch.TestBatch;
import model.testbatch.TestBatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import factory.TestBatchViewModelFactory;

@Repository
@Transactional
public class TestBatchRepository {

  @PersistenceContext
  private EntityManager em;
  
  @Autowired
  private TestBatchViewModelFactory testBatchViewModelFactory;

 
  public TestBatch saveTestBatch(TestBatch testBatch, String testBatchNumber) {
	  testBatch.setIsDeleted(false);
	  testBatch.setBatchNumber(testBatchNumber);
	  testBatch.setStatus(TestBatchStatus.OPEN);
	  updateDonationWithTestBatch(testBatch);
          return  testBatch;
  }
  
  public void updateDonationWithTestBatch(TestBatch testBatch){
		
		List<DonationBatch> donationBatches = testBatch.getDonationBatches();
		if (donationBatches != null && !donationBatches.isEmpty()) {
		        em.persist(testBatch);
			for(DonationBatch donationBatch : donationBatches){
				donationBatch.setTestBatch(testBatch);
				em.merge(donationBatch); 
			}
		}
		
  }
  
  public List<TestBatch> getAllTestBatch() {
	    TypedQuery<TestBatch> query = em.createQuery(
	        "SELECT t FROM TestBatch t WHERE t.isDeleted= :isDeleted",
	        TestBatch.class);
	    query.setParameter("isDeleted", false);
	    return query.getResultList();
	  }
  
    public TestBatch findTestBatchById(Long id) throws NoResultException{
        TypedQuery<TestBatch> query = em.createQuery(
                "SELECT t FROM TestBatch t WHERE t.id = :id", TestBatch.class);
        query.setParameter("id", id);
        TestBatch testBatch = query.getSingleResult();
        return testBatch;
    }
    
    public TestBatch updateTestBatch(TestBatch testBatch) {
        return em.merge(testBatch);
    }

  public List<TestBatch> findTestBatches(
	      String status, String createdAfterDate,
	      String createdBeforeDate,Map<String, Object> pagingParams) {

	    String queryStr =  "SELECT * FROM TestBatch where status = :status "
                    + " or createdDate BETWEEN :createdAfterDate AND :createdBeforeDate";
	    
	  
	    if (pagingParams.containsKey("sortColumn")) {
	      queryStr += " ORDER BY " + pagingParams.get("sortColumn") + " " + pagingParams.get("sortDirection");
	    }
            
           Query query = em.createNativeQuery(queryStr, TestBatch.class);
	    
            query.setParameter("status", status);
            query.setParameter("createdAfterDate", createdAfterDate);
	    query.setParameter("createdBeforeDate", createdBeforeDate);
	    int start = ((pagingParams.get("start") != null) ? Integer.parseInt(pagingParams.get("start").toString()) : 0);
	    int length = ((pagingParams.get("length") != null) ? Integer.parseInt(pagingParams.get("length").toString()) : Integer.MAX_VALUE);

	    query.setFirstResult(start);
	    query.setMaxResults(length);

        return query.getResultList();
    }
  
  /**
   * issue - #229 un used method
  private Long getResultCount(String queryStr, Query query) {
	    String countQueryStr = queryStr.replaceFirst("SELECT c", "SELECT COUNT(c)");
	    // removing the join fetch is important otherwise Hibernate will complain
	    // owner of the fetched association was not present in the select list
	    countQueryStr = countQueryStr.replaceFirst("LEFT JOIN FETCH c.testBatch", "");
	    TypedQuery<Long> countQuery = em.createQuery(countQueryStr, Long.class);
	    for (Parameter<?> parameter : query.getParameters()) {
	      countQuery.setParameter(parameter.getName(), query.getParameterValue(parameter));
	    }
	    return countQuery.getSingleResult().longValue();
	  }  
*/
  
  public void deleteTestBatch(Long id){
      TestBatch testBatch = findTestBatchById(id);
      testBatch.setIsDeleted(false);
      em.merge(testBatch);
  }
  
  public List<TestBatch> getRecentlyClosedTestBatches(Integer numOfResults){ 
	String queryStr = "SELECT tb FROM TestBatch tb "
			+ "WHERE status = :status  ORDER BY lastUpdated DESC";
	TypedQuery<TestBatch> query = em.createQuery(queryStr, TestBatch.class);
	query.setParameter("status", TestBatchStatus.CLOSED);
	query.setMaxResults(numOfResults);
	return query.getResultList();
  }
}