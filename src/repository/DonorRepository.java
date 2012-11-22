package repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import model.donor.Donor;
import model.util.BloodGroup;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DonorRepository {
  @PersistenceContext
  private EntityManager em;

  public void saveDonor(Donor donor) {
    em.persist(donor);
    em.flush();
  }

  public void deleteDonor(Long donorId) {
    Donor existingDonor = findDonorById(donorId);
    existingDonor.setIsDeleted(Boolean.TRUE);
    em.merge(existingDonor);
    em.flush();
  }

  public Donor findDonorById(Long donorId) {
    try {
      String queryString = "SELECT d FROM Donor d WHERE d.id = :donorId and d.isDeleted = :isDeleted";
      TypedQuery<Donor> query = em.createQuery(queryString, Donor.class);
      query.setParameter("isDeleted", Boolean.FALSE);
      return query.setParameter("donorId", donorId).getSingleResult();
    } catch (NoResultException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public Donor findDonorById(String donorId) {
    try {
      String queryString = "SELECT d FROM Donor d WHERE d.id = :donorId and d.isDeleted = :isDeleted";
      TypedQuery<Donor> query = em.createQuery(queryString, Donor.class);
      query.setParameter("isDeleted", Boolean.FALSE);
      Donor resultDonor = query.setParameter("donorId", donorId)
          .getSingleResult();
      if (resultDonor != null) {
        resultDonor.setIsDeleted(Boolean.FALSE);
      }
      return resultDonor;
    } catch (NoResultException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public void deleteAllDonors() {
    Query query = em.createQuery("DELETE FROM Donor d");
    query.executeUpdate();
  }

  public List<Donor> findAnyDonor(String donorNumber, String firstName,
      String lastName, List<BloodGroup> bloodGroups) {

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Donor> cq = cb.createQuery(Donor.class);
    Root<Donor> root = cq.from(Donor.class);

    List<Predicate> bgPredicates = new ArrayList<Predicate>();
    for (BloodGroup bg : bloodGroups) {
      Expression<Boolean> aboExp = cb.equal(root.<String>get("bloodAbo"), bg.getBloodAbo());
      Expression<Boolean> rhdExp = cb.equal(root.<String>get("bloodRhd"), bg.getBloodRhd());
      bgPredicates.add(cb.and(aboExp, rhdExp));
    }

    Expression<Boolean> exp1 = cb.or(bgPredicates.toArray(new Predicate[0]));

    Predicate donorNumberExp = cb.equal(root.<String>get("donorNumber"), donorNumber);
    
    Predicate firstNameExp;
    if (firstName.trim().equals(""))
      firstNameExp = cb.disjunction();
    else
      firstNameExp = cb.like(root.<String>get("firstName"), "%" + firstName + "%");

    Predicate lastNameExp;
    if (lastName.trim().equals(""))
      lastNameExp = cb.disjunction();
    else
      lastNameExp = cb.like(root.<String>get("lastName"), "%" + lastName + "%");

    Expression<Boolean> exp2 = cb.or(exp1, cb.or(donorNumberExp, firstNameExp, lastNameExp));

    Predicate notDeleted = cb.equal(root.<String>get("isDeleted"), false);
    cq.where(cb.and(notDeleted, exp2));
    TypedQuery<Donor> query = em.createQuery(cq);
    List<Donor> donors = query.getResultList();
    if (donors != null && donors.size() > 0)
      return donors;
    return new ArrayList<Donor>();
  }

  public List<Donor> getAllDonors() {
    Query query = em.createQuery(
        "SELECT d FROM Donor d WHERE d.isDeleted = :isDeleted", Donor.class);
    query.setParameter("isDeleted", Boolean.FALSE);
    return query.getResultList();
  }

  public void addDonor(Donor donor) {
    em.persist(donor);
    em.flush();
  }

  public Donor updateDonor(Donor donor) {
    Donor existingDonor = findDonorById(donor.getId());
    if (existingDonor == null) {
      return null;
    }
    existingDonor.copy(donor);
    existingDonor.setIsDeleted(false);
    em.merge(existingDonor);
    em.flush();
    return existingDonor;
  }

  public Donor findDonorByNumber(String donorNumber) {
    try {
      String queryString = "SELECT d FROM Donor d WHERE d.donorNumber = :donorNumber and d.isDeleted = :isDeleted";
      TypedQuery<Donor> query = em.createQuery(queryString, Donor.class);
      query.setParameter("isDeleted", Boolean.FALSE);
      return query.setParameter("donorNumber", donorNumber).getSingleResult();
    } catch (NoResultException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
