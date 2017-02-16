package org.jembi.bsis.model.transfusion;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotBlank;
import org.jembi.bsis.model.BaseModificationTrackerEntity;

/**
 * Entity representing a Transfusion Reaction Type.
 */
@Entity
@Audited
public class TransfusionReactionType extends BaseModificationTrackerEntity {

  private static final long serialVersionUID = 1L;

  @NotBlank
  @Column(length = 255, nullable = false)
  private String name;

  @Column
  private String description;

  private Boolean isDeleted;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }
}
