package org.jembi.bsis.viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.Map;

import org.jembi.bsis.model.counselling.PostDonationCounselling;
import org.jembi.bsis.utils.DateTimeSerialiser;

public class PostDonationCounsellingViewModel {

  private PostDonationCounselling postDonationCounselling;

  private Map<String, Boolean> permissions;

  private DonationViewModel donation;

  public PostDonationCounsellingViewModel(PostDonationCounselling postDonationCounselling) {
    this.postDonationCounselling = postDonationCounselling;
  }

  public long getId() {
    return postDonationCounselling.getId();
  }

  public boolean isFlaggedForCounselling() {
    return postDonationCounselling.isFlaggedForCounselling();
  }

  @JsonSerialize(using = DateTimeSerialiser.class)
  public Date getCounsellingDate() {
    return postDonationCounselling.getCounsellingDate();
  }

  public CounsellingStatusViewModel getCounsellingStatus() {
    if (postDonationCounselling.getCounsellingStatus() == null) {
      return null;
    }
    return new CounsellingStatusViewModel(postDonationCounselling.getCounsellingStatus());
  }

  public String getNotes() {
    return postDonationCounselling.getDonation().getNotes();
  }

  public void setDonation(DonationViewModel donation) {
    this.donation = donation;
  }

  public DonationViewModel getDonation() {
    return donation;
  }

  public DonorViewModel getDonor() {
    return new DonorViewModel(postDonationCounselling.getDonation().getDonor());
  }

  public Map<String, Boolean> getPermissions() {
    return permissions;
  }

  public void setPermissions(Map<String, Boolean> permissions) {
    this.permissions = permissions;
  }

  @JsonIgnore
  public PostDonationCounselling getPostDonationCounselling() {
    return postDonationCounselling;
  }
}