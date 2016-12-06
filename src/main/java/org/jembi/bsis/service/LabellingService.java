package org.jembi.bsis.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jembi.bsis.constant.GeneralConfigConstants;
import org.jembi.bsis.model.component.Component;
import org.jembi.bsis.model.component.ComponentStatus;
import org.jembi.bsis.model.componenttype.ComponentType;
import org.jembi.bsis.model.donation.Donation;
import org.jembi.bsis.model.inventory.InventoryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LabellingService {

  @Autowired
  private ComponentCRUDService componentCRUDService;
  @Autowired
  private LabellingConstraintChecker labellingConstraintChecker;
  @Autowired
  private GeneralConfigAccessorService generalConfigAccessorService;
  @Autowired 
  private CheckCharacterService checkCharacterService;
  
  public boolean verifyPackLabel(long componentId, String prePrintedDIN, String packLabelDIN) {
    Component component = componentCRUDService.findComponentById(componentId);
    if (!component.getStatus().equals(ComponentStatus.AVAILABLE)) {
      return false;
    }
    
    String recordedDin = component.getDonation().getDonationIdentificationNumber();
    String recordedFlagCharacters = component.getDonation().getFlagCharacters();
    if (!recordedDin.equals(prePrintedDIN) ||!(recordedDin+recordedFlagCharacters).equals(packLabelDIN)) {
      return false;
    } else {
      componentCRUDService.putComponentInStock(component);
      return true;
    }
  }
  
  public String printPackLabel(long componentId) {
    Component component = componentCRUDService.findComponentById(componentId);
    Donation donation = component.getDonation();
    ComponentType componentType = component.getComponentType();

    boolean canPrintPackLabel = labellingConstraintChecker.canPrintPackLabelWithConsistencyChecks(component);

    // Check to make sure label can be printed
    if (!canPrintPackLabel) {
      throw new IllegalArgumentException("Pack Label can't be printed");
    }

    // If current status is IN_STOCK, update inventory status to NOT_IN_STOCK for this component
    // The component will be put in stock upon successful verification of packLabel
    if (component.getInventoryStatus().equals(InventoryStatus.IN_STOCK)) {
      componentCRUDService.updateComponentToNotInStock(component);
    }

    // Set up date formats
    String dateFormatString = generalConfigAccessorService.getGeneralConfigValueByName("dateFormat");
    String dateTimeFormatString = generalConfigAccessorService.getGeneralConfigValueByName("dateTimeFormat");
    
    DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
    DateFormat dateTimeFormat = new SimpleDateFormat(dateTimeFormatString);
    DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Update all donations without flag charactors
    if (donation.getFlagCharacters() == null || donation.getFlagCharacters().isEmpty() ) {
      donation.setFlagCharacters(checkCharacterService.calculateFlagCharacters(donation.getDonationIdentificationNumber()));
    }
    
    // Generate element for blood Rh
    String bloodRh = "";
    if (donation.getBloodRh().contains("+")) {
      bloodRh = "^FT487,385^A0N,40,38^FB221,1,0,C^FH^FDRhD POSITIVE^FS";
    } else if (donation.getBloodRh().contains("-")) {
      bloodRh = "^FO482,346^GB233,50,50^FS^FT482,386^A0N,40,38^FB233,1,0,C^FR^FH^FDRhD NEGATIVE^FS";
    }
    
    // Get configured service info values
    String serviceInfoLine1 = generalConfigAccessorService.getGeneralConfigValueByName(
        GeneralConfigConstants.SERVICE_INFO_LINE_1);
    String serviceInfoLine2 = generalConfigAccessorService.getGeneralConfigValueByName(
        GeneralConfigConstants.SERVICE_INFO_LINE_2);
    
    String labelZPL = 
        "CT~~CD,~CC^~CT~" +
        "^CI28" +
        "^XA~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR2,2~SD15^JUS^LRN^CI28^XZ" +
        "^XA" +
        "^MMT" +
        "^PW799" +
        "^LL0799" +
        "^LS0" +
        "^FT61,64^A0N,17,38^FDDIN^FS" +
        "^BY3,3,82^FT62,150^BCN,,Y,N^FD" + donation.getDonationIdentificationNumber() + "^FS" +
        "^BY3,3,80^FT445,147^BCN,,Y,N^FD" + donation.getBloodAbo() + donation.getBloodRh() + "^FS" +
        "^FT62,208^A0N,17,38^FDCollected On^FS" +
        "^FT64,331^A0N,23,36^FD" + dateFormat.format(donation.getDonationDate()) + "^FS" +
        "^FT505,304^A0N,152,148^FB166,1,0,C^FD" + donation.getBloodAbo() + "^FS" +
        bloodRh +
        "^FT65,414^A0N,20,14^FD" + serviceInfoLine2 + "^FS" +
        "^FT65,387^A0N,20,14^FD" + serviceInfoLine1 + "^FS" +
        "^BY3,3,77^FT65,535^BCN,,Y,N^FD" + component.getComponentCode() + "^FS" +
        "^FT64,616^A0N,43,16^FD" + componentType.getComponentTypeName() + "^FS" +
        "^BY2,3,84^FT62,305^BCN,,N,N^FD" + isoDateFormat.format(donation.getDonationDate()) + "^FS" +
        "^FT450,439^A0N,17,38^FDExpires On^FS" +
        "^BY2,3,82^FT451,535^BCN,,N,N^FD" + isoDateFormat.format(component.getExpiresOn()) + "^FS" +
        "^FT452,565^A0N,23,31^FD" + dateTimeFormat.format(component.getExpiresOn()) + "^FS" +
        "^FT66,661^A0N,23,14^FD" + componentType.getPreparationInfo() + "^FS" +
        "^FT66,697^A0N,23,14^FD" + componentType.getStorageInfo() + "^FS" +
        "^FT66,734^A0N,23,14^FD" + componentType.getTransportInfo() + "^FS" +
        "^PQ1,0,1,Y^XZ";

    return labelZPL;
  }

  public String printDiscardLabel(long componentId) {
    Component component = componentCRUDService.findComponentById(componentId);

    boolean canPrintDiscardLabel = labellingConstraintChecker.canPrintDiscardLabel(component);

    // check to make sure discard label can be printed
    if (!canPrintDiscardLabel) {
      throw new IllegalArgumentException("Discard Label can't be printed");
    }

    // Get configured service info values
    String serviceInfoLine1 = generalConfigAccessorService.getGeneralConfigValueByName(
        GeneralConfigConstants.SERVICE_INFO_LINE_1);
    String serviceInfoLine2 = generalConfigAccessorService.getGeneralConfigValueByName(
        GeneralConfigConstants.SERVICE_INFO_LINE_2);

    // Generate ZPL label
    String labelZPL =
        "CT~~CD,~CC^~CT~" +
        "^XA~TA000~JSN^LT0^MNW^MTT^PON^PMN^LH0,0^JMA^PR2,2~SD15^JUS^LRN" + 
        "^CI28" +
        "^XZ" +
        "~DG000.GRF,11520,040," +
        ",:::::::::::::::::::::::gU01C0I01C0,gT03FC0I01FE,gS01FFC0I01FFC0,gR01FFE0K03FFC,gR0HFC0M01FF80,gQ03FF0O03FE0,gP01FF80O01FFC,gP0HFE0Q03FE80,gP0HFC0Q01FF80,gO03FF80R0HFE0,gO07FE0S03FF0,gN01FF80T0HFC,gN07FF0U07FF,gN0HFE0U03FF80,gM03FFC0U01FFE0,gM0IFX07FF8,gM0HFE0W03FF8,gL01FFE0W03FFC,gL03FF80W01FFE,gL0IF80X0IF80,gK01FHFg07FF80,gK03FFE0Y03FFE0,gK07FFC0Y01FHF0,gK0IF80g0IF8,gJ01FHFgH07FFC,gJ03FFE0gG03FFE,:gJ07FFE0gG03FHF,gJ07FFC0gG01FHF,gJ0IF80gH0IF80,gI01FHF80gH0IFC0,gI03FHF80gH0IFE0,gI07FHFgJ07FHF0,gI0IFE0gI03FHF8,:gI0IFE0gI03FHFC,gH01FHFC0gI01FHFC,gH03FHFC0gI01FHFE,:gH07FHF80gJ0JF,:gH0JF80gJ0JF80,gH0JFgL07FHF80,gH0JF80gJ0JFC0,gG01FIFgL07FHFC0,gG03FHFE0gK07FHFE0,:gG07FHFE0gK07FIF0,:gG0JFE0gK03FIF8,:::g01FIFE0gK03FIFC,:::g03FIFE0gK03FIFE,::g07FIFE0gK07FIFE,g03FIFE0gK03FIFE,g07FIFE0gK07FJF,g07FIFE0O02FIFE0O07FJF,g07FIFE0N07FLFO07FJF,g07FJFN07FNFN07FJF,g07FJFM03FOFE0L07FJF,g0LF80K0RFC0K0LF80,g07FJF80J07FRFL0LF,g0LF80I03FSFE0J0LF80,g07FJF80I07FTFK07FJF,g0LFC0I0VFC0I0LF80,g0LFC0H07FUFE0H01FKF80,g0LFE0H0XF8001FKF80,g0LFC003FWFE001FKF80,g0LFE00FYF803FKF80,g0LFE01FYFC07FKF80,g0MF03FYFE07FKF80,g07FKF01FYFC07FKF,g0MF81FYFC0FLF80,g0MF81FNFD5FNFC0FLF80,g0MFC0FLFE0I03FLF81FLF80,g07FKFC07FJFC0K01FKF01FLF,g0MFE07FIFE0M03FIFE03FLF80,g07FLF03FIFP07FHFE07FLF,g07FLF81FHFE0O01FHFC0FMF,g07FLFH0IFR07FF81FMF,g07FLFC0FFE0Q03FF81FMF,g07FLFC03F80R0FE01FMF,Y03FMFE03E0S03E03FMFE0,X01FOFH0C0S01807FNFC,X07FOF8080T080FPF,W01FPFC0W01FPFC0,W03FPFE0W03FPFE0,V01FRFX07FQFC,V03FRFC0U01FRFE,V07FRFC0U01FSF,V0UFV07FSF80,U03FTF80T0UFE0,U07FTFE0S03FUF0,U0WFT07FUF8,T03FVF80Q03FVFE,T07FWFR07FWF,T0YFC0O09FXF80,S01FYFP07FXFC0,S03FYF80N0gFE0,S07FgFN07FgF0,S0gIF80J0gIF8,R01FgIFD005FgIFC,R03FgJFC1FgJFE,R07FgJFC1FgKF,R0gLFC1FgKF80,R0gLFC1FgKFC0,Q01FgKFC1FgKFC0,:Q03FgKFC1FgKFE0,Q07FNF4007FRFC1FSF4007FNF0,Q0NFE0J03FPFE003FPFE0J03FMF8,Q0MFC0L01FOFJ07FNFC0L01FLF8,P01FKFE0N0OFE0I03FNF80M0BFKFC,P01FKF80O0NFC0I01FMFC0O0LFC,P07FJFE0J0FE80H03FLF80J0MFE0I0BF80I03FKF,P07FJFL07FF80H0MFL07FKF8001FHFL07FJF,P0KFE0K0IFE800FKFE0K03FKF800FIF80J03FJF80,P0KFC0K0JFC001FJFE0K03FJFC001FIF80J01FJF80,P0KF80K0JFC0H0KFE0K03FJF8001FIF80K0KF80,O01FIFC0L0JFC0H03FIFC0K01FIFE0H01FIF80K01FIFC0,O03FIF80L0JFC0I0JFC0K01FIF80H01FIF80L0JFE0,O03FIFN0JFC0I07FHFC0K01FIFJ01FIF80L07FHFE0,O03FHFE0M0JFC0I03FHF80L0IFE0I01FIF80L03FHFE0,O07FHFC0M0JFC0I01FHF80L0IFC0I01FIF80L01FIF0,O07FHF80M0JFC0J0IF80L0IF80I01FIF80M0JF0,O07FHFO0JFC0J07FF80L0IFK01FIF80M07FHF0,O0IFE0N0JFC0J03FF80L0IFK01FIF80M03FHF8,O0IF40N0JFC0J01FFC0K01FFC0J017FHF80M01FHF8,O0IF80N0JFC0K0HFC0K01FF80J01FIF80N0IF8,N01FHFP0JFC0K07FC0K01FF0K01FIF80N07FFC,N01FFE0O0JFC0K07F80L0HFL01FIF80N03FFC,N01FFC0O0JFC0K03C0M01E0K01FIF80N01FFC,N03FFC0O0JFC0K0380N0C0K01FIF80N01FFE,N03FFC0O07FHFC0K010O040K01FIFP01FFE,N03FF80O0JFE0gI03FIF80O0HFE,N03FF0P07FHFE0M0140I0140M03FIFQ07FE,N03FF0P07FHFE0M07E0I03F0M03FIFQ07FE,N07FE0P07FHFE0L01FF0I07FC0L03FIFQ03FF,N0HFE0P07FHFE0L03FFE003FFE0L03FIFQ03FF80,N07FC0P07FIFM01FHFC1FHFC0L07FIFQ01FF,N0HF80P03FIFM01FMFC0L07FHFE0Q0HF80,N0HF80P03FIFN0NF80L07FHFE0Q0HF80,N0HF80P03FIF80L0NF80L0JFE0Q0HF80,N0HFR01FIF80L07FLFN0JFC0Q07F80,:N0FE0Q01FIFC0L07FLFM01FIFC0Q07F80,N0FE0Q01FIFE0L03FKFE0L03FIFC0Q03F80,N0FE0R0JFE0L03FKFE0L03FIF80Q03F80,:M01FC0R07FIFM01FKFC0L07FIFS01FC0,N0FC0R07FIF80K03FKFE0L0KFS01F80,M017C0R07FIF80K01FKFC0L0KFS01F40,N0FC0R03FIFC0K01FKFC0K01FIFE0R01FC0,M01FC0R03FIFC0K01FKFC0K01FIFE0R01FC0,N0F80R03FIFE0K01FKFC0K03FIFE0S0F80,M01F80R01FJFL01FKFC0K07FIFC0S0FC0,N0F80S0KF80K0LF80K0KF80S0F80,N0F80S07FIFC0K0LF80J01FJFU0F80,N0F80S07FIFE0K0LF80J03FJFU0F80,N0F80S03FIFE0K0LF80J03FIFE0T0F80,N0F80S03FJFL0LF80J07FIFE0T0F80,N0F80S01FJFC0J0LF80J0KFC0T0F80,N0F80S01FJFE0J0LF80I03FJFC0T0F80,N0F80T0LFK0LF80I07FJF80T0F80,N0F80T0LF80I0LF80I0LF80T0F80,N0780T07FJFC0I0LF80H01FKFV0F,N0780T03FJFE0I0LF80H03FJFE0U0F,N0780T017FJF80H07FJFJ0LF40U0F,N0380U0LFE0H0LF8003FKF80U0E,N0380U07FKFH01FKFC007FKFW0E,N0380U03FKFE01FKFC03FKFE0V0E,N0380U01FLF01FKFC07FKFC0V0E,N03C0U01FLF81FKFC0FLFC0V0E,N01C0V0MF81FKFC0FLF80U01C,N01C0V07FKF83FKFE0FLFW01C,N01C0V01FKF81FKFC07FJFC0V01C,O0E0W0LF83FKFE0FKF80V038,O0E0W07FJF03FKFE07FJFX038,O0E0W03FJF03FKFE07FIFE0W038,O060W01FJF07FLF07FIFC0W030,O060X0JFE07FLF03FIF80W030,gO03FHFE07FLF03FHFE0,gP0IFE0FMF83FHF80,gP03FFC0FMF81FFE,gP08FF81FMFC0FFC,gQ07F81FMFC0FF0,gQ03F83FMFE0FE0,gR0703FMFE07,gT07FNF,:gT0PF80,gS01FOF40,gS01FOFC0,gS03FOFE0,gS07FPF0,:gS0RF8,gR01FQFC,R040X03FQFE0X03,R070X07FRFY07,R0F80W0TF80W0F80,R07C0V01FSFC0V01F,R03E0V03FSFE0V03E,R01F0V07FTFW078,S0F80U0VF80U0F8,S07C0T01FUFC0T01F0,S03E0T03FUFE0T03E0,S01F80S0XF80S0FC0,T0FE80Q03FWFE0R03F80,T07F0R07FXFS07F,T03FE0P03FYFE0P03FE,U0HFQ07FOF7FOFQ07F8,U07FE0N03FOFE3FOFE0N03FF0,U03FF0N0QFC1FPF80M07FE0,U01FFE0L07FPF80FQFM03FFC0,V07FHFK0H7PFE003FPF70J07FHF,V03FYFC001FYFE80,W0gFJ07FXFC,W03FWFE0I01FWFE0,W01FWFL07FVF80,X03FUFE0K03FUFE,Y0VFN07FTFC,Y0UFE0M03FTF8,Y01FSFP07FRF80,g03FQFC0O09FQFE,gG07FOFC0Q01FPF0,gH03FMFE0S03FNF,gI01FKFC0U01FKFD0,gK0BFFE80X0AFFE8,,:::::::::::::::::::::::::::^XA" +
        "^MMT" +
        "^PW799" +
        "^LL0799" +
        "^LS0" +
        "^BY3,3,77^FT75,140^BCN,,Y,N" +
        "^FD" + component.getComponentType().getComponentTypeCode() + "^FS" +
        "^FT415,102^A0N,20,14^FD" + serviceInfoLine1 + "^FS" +
        "^FT416,133^A0N,20,14^FD" + serviceInfoLine2 + "^FS" +
        "^FT224,512^XG000.GRF,1,1^FS" +
        "^FT291,592^A0N,39,38^FDDO NOT USE^FS" +
        "^FT297,539^A0N,39,38^FDBIOHAZARD^FS" +
        "^PQ1,0,1,Y^XZ" +
        "^XA^ID000.GRF^FS^XZ";

    return labelZPL;
  }

}
