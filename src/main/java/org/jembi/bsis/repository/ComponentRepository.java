package org.jembi.bsis.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.component.ComponentStatus;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.donor.Donor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public class ComponentRepository extends AbstractRepository<Component> {

  @PersistenceContext
  private EntityManager em;

  public List<Component> findAnyComponent(String donationIdentificationNumber, List<Long> componentTypes, List<ComponentStatus> status,
      Date donationDateFrom, Date donationDateTo) {
    TypedQuery<Component> query;
    String queryStr = "SELECT DISTINCT c FROM Component c LEFT JOIN FETCH c.donation WHERE " +
        "c.isDeleted= :isDeleted ";

    if (status != null && !status.isEmpty()) {
      queryStr += "AND c.status IN :status ";
    }
    if (!StringUtils.isBlank(donationIdentificationNumber)) {
      queryStr += "AND c.donation.donationIdentificationNumber = :donationIdentificationNumber ";
    }
    if (componentTypes != null && !componentTypes.isEmpty()) {
      queryStr += "AND c.componentType.id IN (:componentTypeIds) ";
    }
    if (donationDateFrom != null) {
      queryStr += "AND c.donation.donationDate >= :donationDateFrom ";
    }
    if (donationDateTo != null) {
      queryStr += "AND c.donation.donationDate <= :donationDateTo ";
    }

    queryStr += " ORDER BY c.id ASC";

    query = em.createQuery(queryStr, Component.class);
    query.setParameter("isDeleted", Boolean.FALSE);

    if (status != null && !status.isEmpty()) {
      query.setParameter("status", status);
    }
    if (!StringUtils.isBlank(donationIdentificationNumber)) {
      query.setParameter("donationIdentificationNumber", donationIdentificationNumber);
    }
    if (componentTypes != null && !componentTypes.isEmpty()) {
      query.setParameter("componentTypeIds", componentTypes);
    }
    if (donationDateFrom != null) {
      query.setParameter("donationDateFrom", donationDateFrom);
    }
    if (donationDateTo != null) {
      query.setParameter("donationDateTo", donationDateTo);
    }

    return query.getResultList();
  }

  public List<Component> findComponentsByDonationIdentificationNumber(String donationIdentificationNumber) {
    return em.createNamedQuery(ComponentNamedQueryConstants.NAME_FIND_COMPONENTS_BY_DIN, Component.class)
        .setParameter("isDeleted", Boolean.FALSE)
        .setParameter("donationIdentificationNumber", donationIdentificationNumber)
        .getResultList();
  }

  public Component findComponent(Long componentId) {
    return em.find(Component.class, componentId);
  }

  public Component findComponentById(Long componentId) throws NoResultException {
    String queryString = "SELECT c FROM Component c LEFT JOIN FETCH c.donation where c.id = :componentId AND c.isDeleted = :isDeleted";
    TypedQuery<Component> query = em.createQuery(queryString, Component.class);
    query.setParameter("isDeleted", Boolean.FALSE);
    query.setParameter("componentId", componentId);
    Component component = null;
    component = query.getSingleResult();
    return component;
  }

  public void updateExpiryStatus() {
    String updateExpiryQuery = "UPDATE Component c SET c.status=:status WHERE " +
        "c.status=:availableStatus AND " +
        "c.expiresOn < :today";
    Query query = em.createQuery(updateExpiryQuery);
    query.setParameter("status", ComponentStatus.EXPIRED);
    query.setParameter("availableStatus", ComponentStatus.AVAILABLE);
    query.setParameter("today", new Date());
    query.executeUpdate();
  }

  public List<Component> findComponentsByDINAndType(String donationIdentificationNumber, long componentTypeId) {
    String queryStr = "SELECT c from Component c WHERE " +
        "c.donation.donationIdentificationNumber = :donationIdentificationNumber AND " +
        "c.componentType.id = :componentTypeId";
    TypedQuery<Component> query = em.createQuery(queryStr, Component.class);
    query.setParameter("donationIdentificationNumber", donationIdentificationNumber);
    query.setParameter("componentTypeId", componentTypeId);
    return query.getResultList();
  }

  // TODO: Test
  public int countChangedComponentsForDonation(long donationId) {
    return em.createNamedQuery(
        ComponentNamedQueryConstants.NAME_COUNT_CHANGED_COMPONENTS_FOR_DONATION,
        Number.class)
        .setParameter("donationId", donationId)
        .setParameter("deleted", false)
        .setParameter("initialStatus", ComponentStatus.QUARANTINED)
        .getSingleResult()
        .intValue();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void updateComponentStatusesForDonor(List<ComponentStatus> oldStatuses, ComponentStatus newStatus,
                                              Donor donor) {

    em.createNamedQuery(ComponentNamedQueryConstants.NAME_UPDATE_COMPONENT_STATUSES_FOR_DONOR)
        .setParameter("oldStatuses", oldStatuses)
        .setParameter("newStatus", newStatus)
        .setParameter("donor", donor)
        .executeUpdate();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void updateComponentStatusForDonation(List<ComponentStatus> oldStatuses, ComponentStatus newStatus,
                                               Donation donation) {

    em.createNamedQuery(ComponentNamedQueryConstants.NAME_UPDATE_COMPONENT_STATUSES_FOR_DONATION)
        .setParameter("oldStatuses", oldStatuses)
        .setParameter("newStatus", newStatus)
        .setParameter("donation", donation)
        .executeUpdate();
  }
  
  public Component findComponentByCodeAndDIN(String componentCode, String donationIdentificationNumber) {
    return em.createNamedQuery(ComponentNamedQueryConstants.NAME_FIND_COMPONENT_BY_CODE_AND_DIN, Component.class)
        .setParameter("donationIdentificationNumber", donationIdentificationNumber)
        .setParameter("componentCode", componentCode)
        .getSingleResult();
  }

  public boolean verifyComponentExists(Long id) {
    Long count = em.createNamedQuery(ComponentNamedQueryConstants.NAME_COUNT_COMPONENT_WITH_ID, Long.class)
        .setParameter("id", id).getSingleResult();
    if (count == 1) {
      return true;
    }
    return false;
  }
}
