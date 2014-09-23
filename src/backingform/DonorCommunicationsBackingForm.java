/**
 * 
 * commented on issue #209 [Adapt BSIS to Expose Rest Services]**
 * 
package backingform;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import model.donor.Donor;
import model.location.Location;
import model.util.BloodGroup;

import org.apache.commons.lang3.StringUtils;

import viewmodel.DonorViewModel;

public class DonorCommunicationsBackingForm {

	private Donor donor;

	private List<BloodGroup> bloodGroups;
        
        @JsonIgnore
	private List<Location> donorPanels;

	private String clinicDate;

	private String lastDonationFromDate;

	private String lastDonationToDate;

	private String anyBloodGroup;

	private boolean createDonorSummaryView;

	private String anyDonorPanel;

	private String donorPanelErrorMessage;

	private String bloodGroupErrorMessage;

	public DonorCommunicationsBackingForm() {
		donor = new Donor();
	}

	public DonorCommunicationsBackingForm(Donor donor) {
		this.donor = donor;
	}

        @JsonIgnore
	public DonorViewModel getDonorViewModel() {
		return new DonorViewModel(donor);
	}

	public String toString() {
		return donor.toString();
	}

	public String getDonorPanel() {
		Location donorPanel = donor.getDonorPanel();
		if (donorPanel == null || donorPanel.getId() == null)
			return null;
		return donorPanel.getId().toString();
	}

	private static final Pattern COMMA_SPLIT_PATTERN = Pattern.compile(",");

	public void setDonorPanel(String donorPanel) {
		if (StringUtils.isBlank(donorPanel)) {
			donor.setDonorPanel(null);
		} else {
			try {
				List<Location> panels = new ArrayList<Location>();
				String[] donorPanelStr = COMMA_SPLIT_PATTERN.split(donorPanel);
				for (String donorPanelId : donorPanelStr) {
					Location l = new Location();
					l.setId(Long.parseLong(donorPanelId));
					panels.add(l);
					donor.setDonorPanel(l);
				}
				donorPanels = panels;
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				donor.setDonorPanel(null);
			}
		}
	}

	public String getClinicDate() {
		return clinicDate;
	}

	public void setClinicDate(String clinicDate) {
		this.clinicDate = clinicDate;
	}

	public String getLastDonationFromDate() {
		return lastDonationFromDate;
	}

	public void setLastDonationFromDate(String lastDonationFromDate) {
		this.lastDonationFromDate = lastDonationFromDate;
	}

	public String getLastDonationToDate() {
		return lastDonationToDate;
	}

	public void setLastDonationToDate(String lastDonationToDate) {
		this.lastDonationToDate = lastDonationToDate;
	}

	public List<Location> getDonorPanels() {
		return donorPanels;
	}

	public void setDonorPanels(List<Location> donorPanels) {
		this.donorPanels = donorPanels;
	}

	public String getAnyBloodGroup() {
		return anyBloodGroup;
	}

	public void setAnyBloodGroup(String anyBloodGroup) {
		this.anyBloodGroup = anyBloodGroup;
	}

	public List<BloodGroup> getBloodGroups() {
		return bloodGroups;
	}

	public void setBloodGroups(List<String> bloodGroups) {
		this.bloodGroups = new ArrayList<BloodGroup>();
		for (String bg : bloodGroups) {
			this.bloodGroups.add(new BloodGroup(bg));
		}
	}
	
	public boolean getCreateDonorSummaryView() {
		return createDonorSummaryView;
	}

	public void setCreateDonorSummaryView(boolean createDonorSummaryView) {
		this.createDonorSummaryView = createDonorSummaryView;
	}

	public String getAnyDonorPanel() {
		return anyDonorPanel;
	}

	public void setAnyDonorPanel(String anyDonorPanel) {
		this.anyDonorPanel = anyDonorPanel;
	}

	public String getDonorPanelErrorMessage() {
		return donorPanelErrorMessage;
	}

	public void setDonorPanelErrorMessage(String donorPanelErrorMessage) {
		this.donorPanelErrorMessage = donorPanelErrorMessage;
	}

	public String getBloodGroupErrorMessage() {
		return bloodGroupErrorMessage;
	}

	public void setBloodGroupErrorMessage(String bloodGroupErrorMessage) {
		this.bloodGroupErrorMessage = bloodGroupErrorMessage;
	}
}
*/