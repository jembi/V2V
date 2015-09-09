package repository;

public class DonationNamedQueryConstants {
    
    public static final String NAME_COUNT_DONATIONS_FOR_DONOR =
            "Donation.countDonationsForDonor";
    public static final String QUERY_COUNT_DONATION_FOR_DONOR =
            "SELECT COUNT(d) " +
            "FROM Donation d " +
            "WHERE d.donor = :donor " +
            "AND d.isDeleted = :deleted ";
   
    public static final String NAME_FIND_ASCENDING_DONATION_DATES_FOR_DONOR =
            "Donation.findAscendingDonationDatesForDonor";
    public static final String QUERY_FIND_ASCENDING_DONATION_DATES_FOR_DONOR =
            "SELECT d.donationDate " +
            "FROM Donation d " +
            "WHERE d.donor.id = :donorId " +
            "AND d.isDeleted = :deleted " +
            "ORDER BY d.donationDate ";
    
    public static final String NAME_FIND_DESCENDING_DONATION_DATES_FOR_DONOR =
            "Donation.findDescendingDonationDatesForDonor";
    public static final String QUERY_FIND_DESCENDING_DONATION_DATES_FOR_DONOR =
            "SELECT d.donationDate " +
            "FROM Donation d " +
            "WHERE d.donor.id = :donorId " +
            "AND d.isDeleted = :deleted " +
            "ORDER BY d.donationDate DESC ";

}
