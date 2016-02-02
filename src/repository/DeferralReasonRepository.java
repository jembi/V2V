package repository;

import model.donordeferral.DeferralReason;
import model.donordeferral.DeferralReasonType;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.util.List;

@Repository
@Transactional

public class DeferralReasonRepository {

  @PersistenceContext
  private EntityManager em;

  public List<DeferralReason> getAllDeferralReasons() {
    TypedQuery<DeferralReason> query;
    query = em.createQuery("SELECT d from DeferralReason d", DeferralReason.class);
    return query.getResultList();
  }

  public DeferralReason findDeferralReason(String reason) {
    String queryString = "SELECT d FROM DeferralReason d WHERE d.reason = :reason";
    TypedQuery<DeferralReason> query = em.createQuery(queryString, DeferralReason.class);
    query.setParameter("reason", reason);
    DeferralReason result = null;
    try {
      result = query.getSingleResult();
    } catch (NoResultException ex) {
    }
    return result;
  }

  public DeferralReason getDeferralReasonById(Long DeferralReasonId) {
    TypedQuery<DeferralReason> query;
    query = em.createQuery("SELECT d from DeferralReason d " +
        "where d.id=:id", DeferralReason.class);
    query.setParameter("id", DeferralReasonId);
    if (query.getResultList().size() == 0)
      return null;
    return query.getSingleResult();
  }

  public void saveAllDeferralReasons(List<DeferralReason> allDeferralReasons) {
    for (DeferralReason dr : allDeferralReasons) {
      DeferralReason existingDeferralReason = getDeferralReasonById(dr.getId());
      if (existingDeferralReason != null) {
        existingDeferralReason.setReason(dr.getReason());
        em.merge(existingDeferralReason);
      } else {
        em.persist(dr);
      }
    }
    em.flush();
  }

  public DeferralReason saveDeferralReason(DeferralReason deferralReason) {
    em.persist(deferralReason);
    em.flush();
    return deferralReason;
  }

  public DeferralReason updateDeferralReason(DeferralReason deferralReason) {
    DeferralReason existingDeferralReason = getDeferralReasonById(deferralReason.getId());
    if (existingDeferralReason == null) {
      return null;
    }
    existingDeferralReason.copy(deferralReason);
    em.merge(existingDeferralReason);
    em.flush();
    return existingDeferralReason;
  }

  public DeferralReason findDeferralReasonByType(DeferralReasonType deferralReasonType)
      throws NonUniqueResultException, NoResultException {

    return em.createNamedQuery(
        DeferralReasonNamedQueryConstants.NAME_FIND_DEFERRAL_REASON_BY_TYPE,
        DeferralReason.class)
        .setParameter("type", deferralReasonType)
        .setParameter("deleted", false)
        .getSingleResult();
  }
}
