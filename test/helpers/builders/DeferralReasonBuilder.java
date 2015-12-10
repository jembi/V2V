package helpers.builders;

import model.donordeferral.DeferralReason;
import model.donordeferral.DeferralReasonType;
import model.donordeferral.DurationType;

public class DeferralReasonBuilder extends AbstractEntityBuilder<DeferralReason> {

  private DeferralReasonType type = DeferralReasonType.NORMAL;
  private Boolean deleted;
  private DurationType durationType = DurationType.TEMPORARY;
  private Integer defaultDuration;
  private String reason;

  public static DeferralReasonBuilder aDeferralReason() {
    return new DeferralReasonBuilder();
  }

  public DeferralReasonBuilder withType(DeferralReasonType type) {
    this.type = type;
    return this;
  }

  public DeferralReasonBuilder thatIsNotDeleted() {
    deleted = false;
    return this;
  }

  public DeferralReasonBuilder thatIsDeleted() {
    deleted = true;
    return this;
  }

  public DeferralReasonBuilder withDurationType(DurationType durationType) {
    this.durationType = durationType;
    return this;
  }

  public DeferralReasonBuilder withDefaultDuration(Integer defaultDuration) {
    this.defaultDuration = defaultDuration;
    return this;
  }

  public DeferralReasonBuilder withReason(String reason) {
    this.reason = reason;
    return this;
  }

  @Override
  public DeferralReason build() {
    DeferralReason deferralReason = new DeferralReason();
    deferralReason.setType(type);
    deferralReason.setIsDeleted(deleted);
    deferralReason.setDurationType(durationType);
    deferralReason.setDefaultDuration(defaultDuration);
    deferralReason.setReason(reason);
    return deferralReason;
  }

}
