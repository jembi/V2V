package helpers.builders;

import backingform.AdverseEventTypeBackingForm;

public class AdverseEventTypeBackingFormBuilder {

  private Long id;
  private String name;
  private String description;
  private boolean deleted;

  public static AdverseEventTypeBackingFormBuilder anAdverseEventTypeBackingForm() {
    return new AdverseEventTypeBackingFormBuilder();
  }

  public AdverseEventTypeBackingFormBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public AdverseEventTypeBackingFormBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public AdverseEventTypeBackingFormBuilder withDescription(String description) {
    this.description = description;
    return this;
  }

  public AdverseEventTypeBackingFormBuilder thatIsDeleted() {
    deleted = true;
    return this;
  }

  public AdverseEventTypeBackingForm build() {
    AdverseEventTypeBackingForm backingForm = new AdverseEventTypeBackingForm();
    backingForm.setId(id);
    backingForm.setName(name);
    backingForm.setDescription(description);
    backingForm.setIsDeleted(deleted);
    return backingForm;
  }
}
