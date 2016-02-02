package service;

import helpers.builders.DonationBuilder;
import helpers.builders.DonorBuilder;
import model.donation.Donation;
import model.donor.Donor;
import model.packtype.PackType;
import model.util.Gender;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DonorServiceTest {

  @Test
  public void testSetDateOfFirstDonation() throws Exception {

    Donor david1 = DonorBuilder.aDonor().withDonorNumber("1").withFirstName("David").withLastName("Smith")
        .withGender(Gender.male).withBirthDate("1977-10-20").build();

    Date dateOfFirstDonation = new SimpleDateFormat("yyyy-MM-dd").parse("2015-09-01");
    Donation donation1 = DonationBuilder.aDonation().withDonor(david1).withDonationDate(dateOfFirstDonation).build();

    DonorService donorService = new DonorService();
    donorService.setDonorDateOfFirstDonation(david1, donation1);

    Assert.assertEquals("Date of First Donation set", dateOfFirstDonation, david1.getDateOfFirstDonation());
  }

  @Test
  public void testSetDonorDateOfLastDonation() throws Exception {

    Donor david1 = DonorBuilder.aDonor().withDonorNumber("1").withFirstName("David").withLastName("Smith")
        .withGender(Gender.male).withBirthDate("1977-10-20").build();

    Date dateOfFirstDonation = new SimpleDateFormat("yyyy-MM-dd").parse("2015-02-01");
    Date dateOfLastDonation = new SimpleDateFormat("yyyy-MM-dd").parse("2015-10-01");
    PackType packType = new PackType();
    int period = 23;
    packType.setPeriodBetweenDonations(period);
    Donation donation1 = DonationBuilder.aDonation().withDonor(david1).withDonationDate(dateOfFirstDonation)
        .withPackType(packType).build();
    Donation donation2 = DonationBuilder.aDonation().withDonor(david1).withDonationDate(dateOfLastDonation)
        .withPackType(packType).build();

    DonorService donorService = new DonorService();
    donorService.setDonorDateOfLastDonation(david1, donation2);
    donorService.setDonorDateOfLastDonation(david1, donation1);

    Assert.assertEquals("Date of last donation set", dateOfLastDonation, david1.getDateOfLastDonation());
  }
}
