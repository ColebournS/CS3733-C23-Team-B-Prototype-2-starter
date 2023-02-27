package edu.wpi.teamb.Controllers.SubmittedRequests;

import edu.wpi.teamb.Algorithms.Sorting;
import edu.wpi.teamb.Controllers.Profile.SigninController;
import edu.wpi.teamb.Controllers.ServiceRequest.BaseRequestController;
import edu.wpi.teamb.Database.*;
import edu.wpi.teamb.Database.Requests.*;
import edu.wpi.teamb.Entities.RequestStatus;
import edu.wpi.teamb.Entities.RequestType;
import edu.wpi.teamb.Entities.Urgency;
import edu.wpi.teamb.Navigation.Navigation;
import edu.wpi.teamb.Navigation.Screen;
import io.github.palexdev.materialfx.controls.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class SubmittedServiceRequestsController {
  @FXML VBox mainVbox;
  @FXML Label RequestInformationTitle;
  @FXML VBox specificRequestInfoBox;
  @FXML VBox filterVbox;
  @FXML MFXButton clearFiltersButton;
  @FXML MFXComboBox<RequestStatus> requestStatusFilter;
  @FXML MFXFilterComboBox<String> assignedStaffFilter;
  @FXML MFXFilterComboBox<RequestType> requestTypeFilter;
  @FXML MFXComboBox<Urgency> requestUrgencyFilter;
  @FXML MFXComboBox<String> requestReporterFilter;
  @FXML MFXCheckbox myRequestsFilter;
  @FXML ImageView helpButton;
  @FXML Label dateLabel = new Label();
  @FXML Label timeLabel = new Label();
  SubmittedSanitationRequestTable saniTable = new SubmittedSanitationRequestTable();
  SubmittedTransportationRequestTable ptTable = new SubmittedTransportationRequestTable();
  SubmittedComputerRequestTable comTable = new SubmittedComputerRequestTable();
  SubmittedAVRequestTable avTable = new SubmittedAVRequestTable();
  SubmittedFacilitiesRequestTable securityTable = new SubmittedFacilitiesRequestTable();
  SubmittedGeneralRequestTable allTable = new SubmittedGeneralRequestTable();
  SubmittedMedicineRequestTable medicineTable = new SubmittedMedicineRequestTable();
  SubmittedMedicalEquipmentRequestTable equipTable = new SubmittedMedicalEquipmentRequestTable();
  SubmittedFacilitiesRequestTable facTable = new SubmittedFacilitiesRequestTable();
  List<String> titles = new ArrayList<>();
  List<String> data = new ArrayList<>();
  @FXML Label field1label, field2label, field3label, field4label;
  List<Label> Labels = new ArrayList<>();
  @FXML Label field1text, field2text, field3text, field4text;
  List<Label> text = new ArrayList<>();

  @FXML MFXButton editButton;
  @FXML MFXButton cancelButton;
  @FXML MFXButton submitButton;
  @FXML GridPane buttons;

  @FXML Label la;

  String page = "none";
  RequestType cur;
  Boolean myrequests = false;
  private ObservableList<RequestStatus> Status =
      FXCollections.observableArrayList(
          RequestStatus.BLANK, RequestStatus.PROCESSING, RequestStatus.DONE);
  protected ObservableList<Urgency> urgencyList =
      FXCollections.observableArrayList(
          Urgency.LOW, Urgency.MODERATE, Urgency.HIGH, Urgency.REQUIRESIMMEADIATEATTENTION);
  private ObservableList<RequestType> requestType =
      FXCollections.observableArrayList(
          RequestType.ALLREQUESTS,
          RequestType.AUDOVISUAL,
          RequestType.COMPUTER,
          RequestType.FACILITIES,
          RequestType.PATIENTTRANSPOTATION,
          RequestType.MEDICALEQUIPMENT,
          RequestType.MEDICINE,
          RequestType.SANITATION,
          RequestType.SECURITY);
  private ObservableList<String> staff = DBSession.getStaff();

  private Login currUser = SigninController.getInstance().currentUser;

  public void initialize() {
    if (!currUser.getAdmin()) myrequests = true;
    saniTable.initialize();
    ptTable.initialize();
    comTable.initialize();
    avTable.initialize();
    securityTable.initialize();
    allTable.initialize();
    medicineTable.initialize();
    equipTable.initialize();
    facTable.initialize();
    initLabels();
    makeTable(RequestType.ALLREQUESTS);
    myRequestsFilter.setOnAction(
        e -> {
          myrequests = myRequestsFilter.isSelected();
          filter();
        });
    requestTypeFilter.setOnAction(e -> makeTable((RequestType) requestTypeFilter.getValue()));
    clearFiltersButton.setOnAction(e -> clearFilters());
    requestStatusFilter.setOnAction(e -> filter());
    assignedStaffFilter.setOnAction(e -> filter());
    requestUrgencyFilter.setOnAction(e -> filter());
    requestReporterFilter.setOnAction(e -> filter());

    mainVbox.setPadding(new Insets(50, 20, 0, 20));

    requestStatusFilter.setItems(Status);
    assignedStaffFilter.setItems(staff);
    requestTypeFilter.setItems(requestType);
    requestUrgencyFilter.setItems(urgencyList);
    requestReporterFilter.setItems(staff);
    requestTypeFilter.setText("All Requests");
    requestTypeFilter.setValue(RequestType.ALLREQUESTS);
    requestStatusFilter.setText("--Select--");
    assignedStaffFilter.setText("--Select--");
    requestUrgencyFilter.setText("--Select--");
    requestReporterFilter.setText("--Select--");
    setFilters();
    resetRequestVboxes();

    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    String formattedDate = currentDate.format(formatter);

    Timeline timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(0),
                event -> {
                  LocalDateTime currentTime = LocalDateTime.now();
                  DateTimeFormatter timefmt = DateTimeFormatter.ofPattern("h:mm a");
                  timeLabel.setText(currentTime.format(timefmt));
                }),
            new KeyFrame(Duration.seconds(1)));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    dateLabel.setText(formattedDate);
    buttons.getChildren().clear();
    buttons.getChildren().add(editButton);
  }

  public void initLabels() {
    Labels.add(field1label);
    Labels.add(field2label);
    Labels.add(field3label);
    Labels.add(field4label);
    text.add(field1text);
    text.add(field2text);
    text.add(field3text);
    text.add(field4text);
  }

  public void resetRequestVboxes() {
    specificRequestInfoBox.getChildren().clear();
    specificRequestInfoBox.getChildren().clear();
    RequestInformationTitle.setText("Request Info");
    specificRequestInfoBox.getChildren().add(RequestInformationTitle);
  }

  public void helpButtonClicked() throws IOException {
    Navigation.navigate(Screen.SERVICE_REQUEST_SYSTEMS);
  }

  private void makeTable(RequestType name) {
    page = name.toString();
    this.cur = name;
    TableView table = new TableView<>();

    mainVbox.getChildren().clear();
    if (page.equals(RequestType.SANITATION.toString())) {
      table =
          saniTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.PATIENTTRANSPOTATION.toString())) {
      table =
          ptTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.COMPUTER.toString())) {
      table =
          comTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.AUDOVISUAL.toString())) {
      table =
          avTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.SECURITY.toString())) {
      table =
          securityTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.ALLREQUESTS.toString())) {
      table =
          allTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.MEDICINE.toString())) {
      table =
          medicineTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.MEDICALEQUIPMENT.toString())) {
      table =
          equipTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    } else if (page.equals(RequestType.FACILITIES.toString())) {
      table =
          facTable.getTable(
              requestStatusFilter.getValue(),
              assignedStaffFilter.getValue(),
              requestReporterFilter.getValue(),
              requestUrgencyFilter.getValue(),
              myrequests);
    }
    TableView finalTable = table;
    table.setOnMouseClicked(e -> mouseClicked(finalTable));
    editButton.setOnAction(e -> editClicked(finalTable));
    cancelButton.setOnAction(e -> cancelClicked(finalTable));
    setLabel(page);
    mainVbox.getChildren().add(table);
  }

  private void editClicked(TableView t) {
    edit = true;
    mouseClicked(t);
    buttons.getChildren().clear();
    buttons.getChildren().add(submitButton);
    buttons.getChildren().add(cancelButton);
  }

  private void submitClicked(TableView t, GeneralRequest r) {
    BaseRequestController br = new BaseRequestController();
    br.submit(r);
    cancelClicked(t);
  }

  private void cancelClicked(TableView t) {
    edit = false;
    mouseClicked(t);
    buttons.getChildren().clear();
    buttons.getChildren().add(editButton);
  }

  private void setLabel(String name) {
    la.setFont(new Font("Ariel", 50));
    la.setText(name);
  }

  public void clearFilters() {
    assignedStaffFilter.getSelectionModel().clearSelection();
    assignedStaffFilter.setValue(null);
    requestStatusFilter.getSelectionModel().clearSelection();
    requestStatusFilter.setValue(null);
    requestUrgencyFilter.getSelectionModel().clearSelection();
    requestUrgencyFilter.setValue(null);
    requestReporterFilter.getSelectionModel().clearSelection();
    requestReporterFilter.setValue(null);
    requestTypeFilter.setValue(cur);

    if (currUser.getAdmin()) myRequestsFilter.setSelected(false);
    else myRequestsFilter.setSelected(true);
    requestStatusFilter.setText("--Select--");
    assignedStaffFilter.setText("--Select--");
    requestUrgencyFilter.setText("--Select--");
    requestReporterFilter.setText("--Select--");
    filter();
  }

  boolean edit = false;

  @FXML
  public void mouseClicked(TableView table) {
    GeneralRequest r = (GeneralRequest) table.getSelectionModel().getSelectedItem();
    resetRequestVboxes();
    titles = new ArrayList<>();
    data = new ArrayList<>();
    //    edit = true;

    if (r != null) {
      //      get request info
      String type = r.getRequestType().toString();
      String date = r.getDate();
      String requestor = r.getFirstname() + " " + r.getLastname();
      String employeeId = r.getEmployeeID();
      String email = r.getEmail();
      String urgency = r.getUrgency().toString();
      String assignedEmployee = r.getAssignedEmployee();
      String status = r.getStatus().toString();
      String notes = r.getNotes();
      //      type, date, requestor, employeeId, email, urgency, assignedEmployee
      addCommonAttritbutes(type, date, requestor, employeeId, email, urgency, assignedEmployee);
      if (r.getRequestType().equals(RequestType.PATIENTTRANSPOTATION)) {
        PatientTransportationRequest pt = (PatientTransportationRequest) r;
        addAttribute("Patient Current Location:", r.getLocation());
        addAttribute("Patient Destination:", pt.getPatientDestinationLocation());
        addAttribute("Patient ID:", pt.getPatientID());
        addAttribute("Equipment Needed:", pt.getEquipmentNeeded());
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.SANITATION)) {
        SanitationRequest sr = (SanitationRequest) r;
        addAttribute("Clean Up Location:", r.getLocation());
        addAttribute("Type of Clean Up:", sr.getTypeOfCleanUp());
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.COMPUTER)) {
        ComputerRequest cr = (ComputerRequest) r;
        addAttribute("Repair Location:", r.getLocation());
        addAttribute("Type of Repair:", cr.getTypeOfRepair());
        addAttribute("Type of Device:", cr.getDevice());
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.AUDOVISUAL)) {
        AudioVideoRequest avr = (AudioVideoRequest) r;
        addAttribute("Location:", avr.getLocation());
        addAttribute("Audio Visual Type:", avr.getAVType());
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.SECURITY)) {
        SecurityRequest secr = (SecurityRequest) r;
        addAttribute("Location: ", secr.getLocation());
        addAttribute("Type Of Issue:", secr.getIssueType());
        addAttribute("Equipment Needed:", secr.getEquipmentNeeded());
        addAttribute("Number Required:", String.valueOf(secr.getNumberRequired()));
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.MEDICINE)) {
        MedicineDeliveryRequest medr = (MedicineDeliveryRequest) r;
        addAttribute("Location:", medr.getLocation());
        addAttribute("Type of Medicine:", medr.getMedicineType());
        addAttribute("Dosage:", medr.getDoasage());
        addAttribute("Patient ID:", medr.getPatientID());
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.MEDICALEQUIPMENT)) {
        MedicalEquipmentDeliveryRequest equipr = (MedicalEquipmentDeliveryRequest) r;
        addAttribute("Location:", equipr.getLocation());
        addAttribute("Type of Equipment:", equipr.getEquipmentType());
        setFields(r);
      } else if (r.getRequestType().equals(RequestType.FACILITIES)) {
        FacilitiesRequest fr = (FacilitiesRequest) r;
        addAttribute("Location:", fr.getLocation());
        addAttribute("Type of Maintenance", fr.getMaintenanceType());
        setFields(r);
      }

      addStatusAndNotes(status, notes);
    } else {

    }
  }

  private void addAttribute(String title, String field) {
    titles.add(title);
    data.add(field);
  }

  @FXML Label requestTypeText;
  @FXML Label dateText;
  @FXML Label requestorText;
  @FXML Label employeeIdText;
  @FXML Label emailText;
  @FXML Label UrgencyLabel, urgencyText;
  @FXML Label assignedEmployeeLabel, assignedEmployeeText;
  @FXML MFXFilterComboBox<Urgency> requestUrgencyEdit;
  @FXML MFXFilterComboBox<String> assignedStaffEdit;

  private void addCommonAttritbutes(
      String type,
      String date,
      String requestor,
      String employeeId,
      String email,
      String urgency,
      String assignedEmployee) {
    addAttribute(requestTypeText, type);
    addAttribute(dateText, date);
    addAttribute(requestorText, "Requestor: " + requestor);
    addAttribute(employeeIdText, "Employee ID: " + employeeId);
    addAttribute(emailText, "Email: " + email);
    if (edit) {
      //      urgency
      specificRequestInfoBox.getChildren().add(UrgencyLabel);
      requestUrgencyEdit.setItems(urgencyList);
      requestUrgencyEdit.setText(urgency);
      specificRequestInfoBox.getChildren().add(requestUrgencyEdit);

      //      assigned employee
      specificRequestInfoBox.getChildren().add(assignedEmployeeLabel);
      assignedStaffEdit.setItems(staff);
      assignedStaffEdit.setText(assignedEmployee);
      specificRequestInfoBox.getChildren().add(assignedStaffEdit);
    } else {
      addAttribute(UrgencyLabel, urgencyText, urgency);
      addAttribute(assignedEmployeeLabel, assignedEmployeeText, assignedEmployee);
    }
  }

  private void addAttribute(Label title, Label text, String s) {
    specificRequestInfoBox.getChildren().add(title);
    addAttribute(text, s);
  }

  @FXML Label statusLabel;
  @FXML Label statusText;
  @FXML Label notesLabel;
  @FXML Label notesText;
  @FXML MFXFilterComboBox<RequestStatus> statusEdit;
  @FXML MFXTextField notesEdit;

  private void addStatusAndNotes(String status, String notes) {
    if (edit) {
      specificRequestInfoBox.getChildren().add(statusLabel);
      statusEdit.setItems(Status);
      statusEdit.setText(status);
      specificRequestInfoBox.getChildren().add(statusEdit);

      specificRequestInfoBox.getChildren().add(notesLabel);
      notesEdit.setText(notes);
      specificRequestInfoBox.getChildren().add(notesEdit);
    } else {
      addAttribute(statusLabel, statusText, status);
      addAttribute(notesLabel, notesText, notes);
    }
  }

  private void addAttribute(Label l, String s) {
    l.setText(s);
    specificRequestInfoBox.getChildren().add(l);
  }

  @FXML MFXFilterComboBox field1EditBox, field2EditBox, field3EditBox, field4EditBox;
  @FXML MFXTextField field2EditText, field3EditText, field4EditText;

  private ObservableList<String> ptEquipment =
      FXCollections.observableArrayList("Stretcher", "Wheelchair", "Restraints", "Stair Chair");
  ObservableList<String> typeOfCleanUpList =
      FXCollections.observableArrayList("Bathroom", "Spill", "Vacant Room", "Blood", "Chemicals");
  ObservableList<String> typeOfRepairList =
      FXCollections.observableArrayList("New Hardware", "Broken Hardware", "Technical Issues");
  ObservableList<String> typeOfDeviceList =
      FXCollections.observableArrayList("Computer", "Phone", "Monitor");
  ObservableList<String> avEquipment =
      FXCollections.observableArrayList("TV", "Radio", "iPad", "Headphones");
  ObservableList<String> secEquipment =
      FXCollections.observableArrayList("Restraints", "Taser", "Barricade");

  ObservableList<String> secIssue =
      FXCollections.observableArrayList(
          "Unruly Patient",
          "Intruder",
          "General Danger",
          "Escalated Disagreement",
          "Suspicious Activity");

  private void setEditFields(GeneralRequest r) {
    setFieldLabels();
    specificRequestInfoBox.getChildren().add(field1label);
    field1EditBox.setItems(getLocations());
    field1EditBox.setText(r.getLocation());
    specificRequestInfoBox.getChildren().add(field1EditBox);

    if (r.getRequestType().equals(RequestType.PATIENTTRANSPOTATION)) {

      PatientTransportationRequest pt = (PatientTransportationRequest) r;

      addAttribute("Patient Destination:", pt.getPatientDestinationLocation());
      //      patient destination
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditBox.setItems(getLocations());
      field2EditBox.setText(r.getLocation());
      specificRequestInfoBox.getChildren().add(field2EditBox);

      //      Patient ID
      specificRequestInfoBox.getChildren().add(field3label);
      field3EditText.setText(pt.getPatientID());
      specificRequestInfoBox.getChildren().add(field3EditText);

      //    equipment
      specificRequestInfoBox.getChildren().add(field4label);
      field4EditBox.setItems(ptEquipment);
      field4EditBox.setText(pt.getEquipmentNeeded());
      specificRequestInfoBox.getChildren().add(field4EditBox);

    } else if (r.getRequestType().equals(RequestType.SANITATION)) {
      SanitationRequest sr = (SanitationRequest) r;
      //      type of clean up
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditBox.setItems(typeOfCleanUpList);
      field2EditBox.setText(sr.getTypeOfCleanUp());
      specificRequestInfoBox.getChildren().add(field2EditBox);

    } else if (r.getRequestType().equals(RequestType.COMPUTER)) {
      ComputerRequest cr = (ComputerRequest) r;
      //      type of repair
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditBox.setItems(typeOfRepairList);
      field2EditBox.setText(cr.getTypeOfRepair());
      specificRequestInfoBox.getChildren().add(field2EditBox);

      //    type of device
      specificRequestInfoBox.getChildren().add(field3label);
      field3EditBox.setItems(typeOfDeviceList);
      field3EditBox.setText(cr.getDevice());
      specificRequestInfoBox.getChildren().add(field3EditBox);

    } else if (r.getRequestType().equals(RequestType.AUDOVISUAL)) {
      AudioVideoRequest avr = (AudioVideoRequest) r;
      //      av type
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditBox.setItems(avEquipment);
      field2EditBox.setText(avr.getAVType());
      specificRequestInfoBox.getChildren().add(field2EditBox);
      addAttribute("Audio Visual Type:", avr.getAVType());

    } else if (r.getRequestType().equals(RequestType.SECURITY)) {
      SecurityRequest secr = (SecurityRequest) r;
      //      type of issue
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditBox.setItems(secIssue);
      field2EditBox.setText(secr.getIssueType());
      specificRequestInfoBox.getChildren().add(field2EditBox);

      //    equipment
      specificRequestInfoBox.getChildren().add(field3label);
      field3EditBox.setItems(secEquipment);
      field3EditBox.setText(secr.getEquipmentNeeded());
      specificRequestInfoBox.getChildren().add(field3EditBox);

      //      number req
      specificRequestInfoBox.getChildren().add(field4label);
      field4EditText.setText(String.valueOf(secr.getNumberRequired()));
      specificRequestInfoBox.getChildren().add(field4EditText);

    } else if (r.getRequestType().equals(RequestType.MEDICINE)) {
      MedicineDeliveryRequest medr = (MedicineDeliveryRequest) r;
      //      type of medicine
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditText.setText(medr.getMedicineType());
      specificRequestInfoBox.getChildren().add(field2EditText);

      //     dosage
      specificRequestInfoBox.getChildren().add(field3label);
      field3EditText.setText(medr.getDoasage());
      specificRequestInfoBox.getChildren().add(field3EditText);

      //      Patient ID
      specificRequestInfoBox.getChildren().add(field4label);
      field4EditText.setText(medr.getPatientID());
      specificRequestInfoBox.getChildren().add(field4EditText);

    } else if (r.getRequestType().equals(RequestType.MEDICALEQUIPMENT)) {
      MedicalEquipmentDeliveryRequest equipr = (MedicalEquipmentDeliveryRequest) r;
      //      type of equip
      specificRequestInfoBox.getChildren().add(field2label);
      field2EditBox.setItems(ptEquipment);
      field2EditBox.setText(equipr.getEquipmentType());
      specificRequestInfoBox.getChildren().add(field2EditBox);

    } else if (r.getRequestType().equals(RequestType.FACILITIES)) {
      FacilitiesRequest fr = (FacilitiesRequest) r;
      //      type of maintenance
      specificRequestInfoBox.getChildren().add(field2label);
      //      change this once added
      field2EditBox.setItems(secIssue);
      field2EditBox.setText(fr.getMaintenanceType());
      specificRequestInfoBox.getChildren().add(field2EditBox);
    }
  }

  private void setFields(GeneralRequest r) {
    if (edit) {
      setEditFields(r);
    } else {
      for (int i = 0; i < data.size(); i++) {
        Labels.get(i).setText(titles.get(i));
        text.get(i).setText(data.get(i));
        specificRequestInfoBox.getChildren().add(Labels.get(i));
        specificRequestInfoBox.getChildren().add(text.get(i));
      }
    }
  }

  private void setFieldLabels() {
    for (int i = 0; i < data.size(); i++) {
      Labels.get(i).setText(titles.get(i));
    }
  }

  @FXML Label requestTypeBox;
  @FXML Label urgencyBox;
  @FXML Label requestStatusBox;
  @FXML Label assignedStaffBox;
  @FXML Label requestReporterBox;

  private void setFilters() {
    filterVbox.getChildren().clear();
    filterVbox.getChildren().add(requestTypeBox);
    filterVbox.getChildren().add(requestTypeFilter);
    addFilter(urgencyBox, requestUrgencyFilter);
    addFilter(requestStatusBox, requestStatusFilter);
    filterVbox.getChildren().add(assignedStaffBox);
    filterVbox.getChildren().add(assignedStaffFilter);

    if (currUser.getAdmin()) {
      addFilter(requestReporterBox, requestReporterFilter);
      filterVbox.getChildren().add(myRequestsFilter);
    }

    filterVbox.getChildren().add(clearFiltersButton);
  }

  private void addFilter(Label l, MFXComboBox b) {
    filterVbox.getChildren().add(l);
    filterVbox.getChildren().add(b);
  }

  public void filter() {
    makeTable(cur);
  }

  protected ObservableList<String> getLocations() {
    ObservableList<String> list = FXCollections.observableArrayList();

    Map<String, LocationName> locationNames = DBSession.getAllLocationNames();
    locationNames.forEach((key, value) -> list.add(value.getLongName()));

    Sorting.quickSort(list);
    return list;
  }
}
