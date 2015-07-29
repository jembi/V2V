package model.testbatch;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import model.donationbatch.DonationBatch;
import model.modificationtracker.ModificationTracker;
import model.modificationtracker.RowModificationTracker;
import model.user.User;

import org.hibernate.envers.Audited;


@Entity
@Audited
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public class TestBatch implements ModificationTracker {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable=false)
	private Long id;

	@Valid
	private RowModificationTracker modificationTracker;

	@Lob
	private String notes;

	private Boolean isDeleted;

	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(length=20, unique=true)
	@Size(min=6,max=6)
	private String batchNumber;

	
	@Enumerated(EnumType.STRING)
	@Column(length=20)
	private TestBatchStatus status;
        
        @OneToMany(mappedBy = "testBatch", fetch = FetchType.EAGER)
        private List<DonationBatch> donationBatches;


	public TestBatch() {
		modificationTracker = new RowModificationTracker();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

        public Boolean isIsDeleted() {
                return isDeleted;
        }

        public void setIsDeleted(Boolean isDeleted) {
                this.isDeleted = isDeleted;
        }

	public String getBatchNumber() {
		return batchNumber;
	}

	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}

	public TestBatchStatus getStatus() {
		return status;
	}

	public void setStatus(TestBatchStatus status) {
		this.status = status;
	}

	public Date getLastUpdated() {
		return modificationTracker.getLastUpdated();
	}

	public Date getCreatedDate() {
		return modificationTracker.getCreatedDate();
	}

	public User getCreatedBy() {
		return modificationTracker.getCreatedBy();
	}

	public User getLastUpdatedBy() {
		return modificationTracker.getLastUpdatedBy();
	}

	public void setLastUpdated(Date lastUpdated) {
		modificationTracker.setLastUpdated(lastUpdated);
	}

	public void setCreatedDate(Date createdDate) {
		modificationTracker.setCreatedDate(createdDate);
	}

	public void setCreatedBy(User createdBy) {
		modificationTracker.setCreatedBy(createdBy);
	}

	public void setLastUpdatedBy(User lastUpdatedBy) {
		modificationTracker.setLastUpdatedBy(lastUpdatedBy);
	}

    public List<DonationBatch> getDonationBatches() {
            return donationBatches;
    }

    public void setDonationBatches(List<DonationBatch> donationBatches) {
            this.donationBatches = donationBatches;
    }

}