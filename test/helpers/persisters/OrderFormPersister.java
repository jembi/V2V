package helpers.persisters;

import static helpers.persisters.EntityPersisterFactory.aLocationPersister;

import javax.persistence.EntityManager;

import model.order.OrderForm;

public class OrderFormPersister extends AbstractEntityPersister<OrderForm> {

  @Override
  public OrderForm deepPersist(OrderForm orderForm, EntityManager entityManager) {
    if (orderForm.getDispatchedFrom() != null) {
      aLocationPersister().deepPersist(orderForm.getDispatchedFrom(), entityManager);
    }
    if (orderForm.getDispatchedTo() != null) {
      aLocationPersister().deepPersist(orderForm.getDispatchedTo(), entityManager);
    }
    return persist(orderForm, entityManager);
  }

}