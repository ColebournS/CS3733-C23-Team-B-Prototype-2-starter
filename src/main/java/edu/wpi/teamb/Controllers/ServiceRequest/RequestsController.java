package edu.wpi.teamb.Controllers.ServiceRequest;

import edu.wpi.teamb.Database.DBSession;
import edu.wpi.teamb.Database.PatientTransportationRequest;
import edu.wpi.teamb.Database.SanitationRequest;
import edu.wpi.teamb.Navigation.Navigation;
import edu.wpi.teamb.Navigation.Screen;
import java.util.ArrayList;
import edu.wpi.teamb.Entities.ORMType;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class RequestsController {
  @FXML VBox mainVbox;
  @FXML MFXButton saniButton;
  @FXML MFXButton transButton;
  @FXML MFXButton secButton;
  @FXML MFXButton comButton;
  @FXML MFXButton audioButton;

  public void initialize() {
    List<String> saniColumns =
        List.of(
            new String[] {
              "lastname",
              "firstname",
              "employeeID",
              "email",
              "urgency",
              "assignedEmployee",
              "typeOfCleanUp",
              "cleanUpLocation",
              "status",
              "notes"
            });
    List<String> transColumns =
        List.of(
            "lastname",
            "firstname",
            "employeeID",
            "email",
            "urgency",
            "patientID",
            "patientCurrentLocation",
            "patientDestinationLocation",
            "equipmentNeeded",
            "status",
            "notes");
    List<String> comColumns =
        List.of(
            "lastname",
            "firstname",
            "employeeID",
            "email",
            "urgency",
            "assignedEmployee",
            "typeOfRepair",
            "repairLocation",
            "notes",
            "status",
            "device");
    saniButton.setOnAction(e -> makeTable(saniColumns, ORMType.SREQUEST, "Sanitation"));
    transButton.setOnAction(
        e -> makeTable(transColumns, ORMType.PTREQUEST, "Internal Patient Transportation"));
    comButton.setOnAction(e -> makeTable(comColumns, ORMType.CREQUEST, "Computer"));
    mainVbox.setPadding(new Insets(50, 20, 0, 20));
  }

  private void makeTable(List<String> columns, ORMType type, String name) {
    mainVbox.getChildren().clear();
    TableView t = new TableView();
    for (String colName : columns) {
      TableColumn col = new TableColumn();
      t.getColumns().add(col);
      col.setText(colName);
      col.setCellValueFactory(new PropertyValueFactory<>(colName));
    }
    List<Object> objectList = DBSession.getAll(type);
    objectList.forEach(
        (value) -> {
          t.getItems().add(value);
        });
    Label l = new Label();
    l.setText(name);
    l.setFont(new Font("Ariel", 25));
    mainVbox.getChildren().add(l);
    mainVbox.getChildren().add(t);
  }
}
