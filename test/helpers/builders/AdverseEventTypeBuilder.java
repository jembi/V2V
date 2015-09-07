package helpers.builders;

import model.adverseevent.AdverseEventType;

public class AdverseEventTypeBuilder extends AbstractEntityBuilder<AdverseEventType> {

    private Long id;
    private String name;
    private String description;
    private boolean deleted;
    
    public AdverseEventTypeBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public AdverseEventTypeBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public AdverseEventTypeBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public AdverseEventTypeBuilder thatIsDeleted() {
        deleted = true;
        return this;
    }

    @Override
    public AdverseEventType build() {
        AdverseEventType adverseEventType = new AdverseEventType();
        adverseEventType.setId(id);
        adverseEventType.setName(name);
        adverseEventType.setDescription(description);
        adverseEventType.setDeleted(deleted);
        return adverseEventType;
    }
    
    public static AdverseEventTypeBuilder anAdverseEventType() {
        return new AdverseEventTypeBuilder();
    }

}
