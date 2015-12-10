package model.audit;

import org.hibernate.envers.RevisionType;

import javax.persistence.*;

@Entity
public class EntityModification {

  @Id
  @GeneratedValue
  private int id;

  @ManyToOne
  private AuditRevision auditRevision;

  @Column(length = 30, nullable = false)
  @Enumerated(EnumType.STRING)
  private RevisionType revisionType;

  @Column(length = 30, nullable = false)
  private String entityName;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public AuditRevision getAuditRevision() {
    return auditRevision;
  }

  public void setAuditRevision(AuditRevision auditRevision) {
    this.auditRevision = auditRevision;
  }

  public RevisionType getRevisionType() {
    return revisionType;
  }

  public void setRevisionType(RevisionType revisionType) {
    this.revisionType = revisionType;
  }

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    return other instanceof EntityModification &&
            ((EntityModification) other).id == id;
  }

}
