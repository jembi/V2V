package model.address;

import javax.persistence.Column;
import javax.persistence.Entity;

import model.BaseEntity;


@Entity
public class ContactMethodType extends BaseEntity {

  private static final long serialVersionUID = 1L;

  @Column(length = 30)
  private String contactMethodType;

  private boolean isDeleted;

  public String getContactMethodType() {
    return contactMethodType;
  }

  public void setContactMethodType(String contactMethodType) {
    this.contactMethodType = contactMethodType;
  }

  public boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

}
