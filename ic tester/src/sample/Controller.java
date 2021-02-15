package sample;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    @FXML
    private Tooltip tooltip;

    @FXML
    private ComboBox<String> portlist, composant_model_list;

    @FXML
    private Label lab_H,lab_L,lab_N,lab_H_STAT,lab_L_STAT,lab_N_STAT,lab_R_STAT,labex,lab_r;

    @FXML
    private Button connectButton,start_test,stop_test,refresh_b;

    @FXML
    private TextArea monitorText;

    @FXML
    private LineChart<String, Double> lineChart;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private ProgressBar pProgressBar;

    @FXML
    private ProgressIndicator pWaitIndicator;

    @FXML
    private TextField user_text,pass_text,composant_article;

    @FXML
    private TableView<TableClass> tableview;
    public TableColumn<TableClass,String> article , model,resultat,description,date,heure;
    ArrayList<Double> arrayList = new ArrayList<>();
    private Thread connectThread;
    private static int i = 0, c = 0;
    private XYChart.Series<String, Double> dataseries;
    private boolean first_boot = true;
    private static SerialPort chosenPort;
    private static double Hvalue = 00.00, Lvalue = 00.00, Nvalue = 00.00;
    final SimpleDateFormat current_time = new SimpleDateFormat("HH:mm");
    final SimpleDateFormat current_date = new SimpleDateFormat("dd/MM/yyyy");
    String article_s=" ",model_s="",resultat_s="",description_s="",date_s="",time_s="";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        composant_model_list.getItems().addAll("RM3182","DEI3182-DMC");
        //TABLE :
        article.setCellValueFactory(new PropertyValueFactory<>("TCArticle"));
        model.setCellValueFactory(new PropertyValueFactory<>("TCmodel"));
        resultat.setCellValueFactory(new PropertyValueFactory<>("TCresultat"));
        description.setCellValueFactory(new PropertyValueFactory<>("TCdescription"));
        date.setCellValueFactory(new PropertyValueFactory<>("TCdate"));
        heure.setCellValueFactory(new PropertyValueFactory<>("TCheure"));
        //add data to TABLE
        try {
            Connection con = DBConnector.getConnection();
            ResultSet  rs =con.createStatement().executeQuery("select * from testpvdata");
            while(rs.next()){
                TableClass tableClass=new TableClass(rs.getString("article"),rs.getString("model"),rs.getString("resultat"),rs.getString("description"),rs.getString("date"),rs.getString("time"));
                tableview.getItems().add(tableClass);
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pWaitIndicator.setVisible(false);
        //Chart setup
        //NumberAxis axis;
        dataseries = new XYChart.Series<>();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(-10);
        yAxis.setUpperBound(10);
        yAxis.setMinorTickLength(1);
        yAxis.setMinorTickVisible(true);
        yAxis.setMinorTickCount(2);
        //connecting process in Thread
        connectThread = new Thread(() -> {
            pProgressBar.setProgress(0.2);
            String line="";
            monitorText.appendText("Process...\n");
            System.out.println("1.0- Start Thread");
            System.out.println("2.0- Start Scanner");
            Scanner scanner = new Scanner(chosenPort.getInputStream());
            while (c == 0 && scanner.hasNextLine()) {
                System.out.println("3.0- Start while loop");
                try {
                    pProgressBar.setProgress(0.6);
                    Thread.sleep(1000);
                    line = scanner.nextLine();
                    System.out.println("3.1- line length: " + line.length());
                    System.out.println("3.2- line content : " + line);
                    pProgressBar.setProgress(0.8);
                    //get the restart sign from the system
                    if (line.equals("SYS_RE_START")) {
                        Thread.sleep(300);
                        c = 1;
                        //System.out.println("4.0- Afectation: c <= 1");
                        System.out.println("4.1- System is ready.");
                        pProgressBar.setProgress(0.9);
                        monitorText.appendText("System is ready. \n");
                        start_test.setDisable(false);
                        stop_test.setDisable(false);
                        PrintWriter output = new PrintWriter(chosenPort.getOutputStream());
                        output.print("v");//RX connected sign to the system
                        output.flush();
                        output.close();
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                    System.out.println(e.getMessage());
                    e.printStackTrace(); }
            }
            scanner.close();
            System.out.println("5.0- Thread : End");
            pProgressBar.setProgress(1);
        });
        //get all COM (port) to the port list
        SerialPort[] portNames = SerialPort.getCommPorts();
        for (int i = 0; i < portNames.length; i++) {
            System.out.println("Port: " + portNames[i].getSystemPortName());
            portlist.getItems().addAll(portNames[i].getSystemPortName());
        }
    }

    public void chartupdate(ArrayList arl) {
        dataseries.getData().clear();
        dataseries.setName("Waveform @ Voltage");
        for (int j = 0; j < arl.size(); j++) {
            dataseries.getData().add(new XYChart.Data<String, Double>(String.valueOf(j), Double.parseDouble(arl.get(j).toString())));
            if (j == 0){
                lineChart.getData().clear();
                lineChart.getData().add(dataseries);
                //lineChart.setTitle("Wavefome");
            }
        }
    }

    public void stat_test_Event(ActionEvent event) {
        TXRXDataTest(chosenPort.getInputStream(), chosenPort.getOutputStream());

    }

    public void portRefreshAction(ActionEvent event) {

        portlist.getItems().clear();
        SerialPort[] portNames = SerialPort.getCommPorts();
        for (int i = 0; i < portNames.length; i++)
            portlist.getItems().addAll(portNames[i].getSystemPortName());
        System.out.println("refrech...");
    }

    public void connectToPortSerie(ActionEvent event) {
        c = 0;
        if (connectButton.getText().equals("Connect")) {
            //attempt to connect with serial port
            chosenPort = SerialPort.getCommPort(portlist.getValue());
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
            if (chosenPort.openPort()) {
                connectButton.setText("Disconnect");
                connectButton.setStyle(
                        "-fx-background-color:linear-gradient(to right ,#1990b9,#0ec4b3);" +
                        "-fx-border-color: #1990B9;" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:3;" +
                        "-fx-background-radius:3");
                portlist.setDisable(true);
                refresh_b.setDisable(true);
                monitorText.appendText("Connected with " + portlist.getValue() + "\n");
                if (first_boot)
                    monitorText.appendText("Waiting for response...\n");
                System.out.println("0::is alive:"+ connectThread.isAlive());
                if (first_boot)
                    connectThread.start();
                else {
                    start_test.setDisable(false);
                    stop_test.setDisable(false);
                }
                System.out.println("1::is alive:"+ connectThread.isAlive());
                first_boot=false;
            }
        } else {
            //disconnect from the serial port
            chosenPort.closePort();
            portlist.setDisable(false);
            refresh_b.setDisable(false);
            start_test.setDisable(true);
            stop_test.setDisable(true);
            connectButton.setText("Connect");
            connectButton.setStyle("-fx-background-color:linear-gradient(to right ,#e20e72,#c4510e);");
        }
    }

    public void TXRXDataTest(InputStream is, OutputStream os) {
        pWaitIndicator.setVisible(true);
        start_test.setDisable(true);
        Scanner scanner = new Scanner(is);
        PrintWriter output = new PrintWriter(os);
        output.print("ts");//send command to the system to get the mesurement data
        output.flush();
        output.close();

        Thread threadGetMesure =new Thread(new Runnable() {
            @Override
            public void run() {
                pWaitIndicator.setVisible(true);
                try {
                    String lineB;
                    c = 0;
                    i = 0;
                    while (c == 0 && i < 4 && scanner.hasNextLine()) {
                        i++;
                        lineB = scanner.nextLine();
                        System.out.println("length: " + lineB.length());
                        System.out.println("code : " + lineB);
                        if (lineB.equals("datastart")) {
                            lineB = scanner.nextLine();
                            System.out.println("Start Get Mesure: ");
                            if (lineB.equals("H")) {
                                String temp1 = scanner.nextLine();
                                Hvalue = Double.parseDouble(temp1);
                                System.out.println("Done H : " + lineB);
                            } else {
                                System.out.println("err H : " + lineB);
                            }
                            lineB = scanner.nextLine();
                            if (lineB.equals("L")) {
                                Lvalue = Double.parseDouble(scanner.nextLine());
                                System.out.println("Done L : " + lineB);
                            } else {
                                System.out.println("err L : " + lineB);
                            }
                            lineB = scanner.nextLine();
                            if (lineB.equals("N")) {
                                Nvalue = Double.parseDouble(scanner.nextLine());
                                System.out.println("Done N : " + lineB);
                            } else {
                                System.out.println("err N : " + lineB);
                            }
                            lineB = scanner.nextLine();
                            if (lineB.equals("dataend")) {
                                System.out.println("Value received DONE");
                                c = 1;
                            } else {
                                System.err.println("Erro in Receiving ");
                            }
                        } else {
                            System.out.println("Line No Conforme");
                        }
                    }
                    scanner.close();

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            monitorText.appendText(new SimpleDateFormat("HH:mm:ss").format(new Date())+" : \n");
                            monitorText.appendText("+5V value: " + Hvalue + "\n");
                            monitorText.appendText("-5V value: " + Lvalue + "\n");
                            monitorText.appendText("+0V value: " + Nvalue + "\n");
                            System.out.println("H: " + Hvalue + "\nL: " + Lvalue + "\nN: " + Nvalue);
                            lab_H.setText(Hvalue + "V");
                            lab_L.setText(Lvalue + "V");
                            lab_N.setText(Nvalue + "V");
                            boolean passed = true;

                            if (Hvalue > 4.75 && Hvalue < 5.25) {
                                lab_H_STAT.setText("PASSED");
                                lab_H_STAT.setStyle("-fx-background-color:green;-fx-border-color: #000000;-fx-border-width:1");
                            } else {
                                passed = false;
                                lab_H_STAT.setText("FAILED");
                                lab_H_STAT.setStyle("-fx-background-color:red;-fx-border-color: #000000;-fx-border-width:1");
                            }

                            if (Lvalue < -4.75 && Lvalue > -5.25) {
                                lab_L_STAT.setText("PASSED");
                                lab_L_STAT.setStyle("-fx-background-color:green;-fx-border-color: #000000;-fx-border-width:1");
                            } else {
                                passed = false;
                                lab_L_STAT.setText("FAILED");
                                lab_L_STAT.setStyle("-fx-background-color:red;-fx-border-color: #000000;-fx-border-width:1");
                            }

                            if (Nvalue > -0.75 && Nvalue < 0.75) {

                                lab_N_STAT.setText("PASSED");
                                lab_N_STAT.setStyle("-fx-background-color:green;-fx-border-color: #000000;-fx-border-width:1");
                            } else {
                                passed = false;
                                lab_N_STAT.setText("FAILED");
                                lab_N_STAT.setStyle("-fx-background-color:red;-fx-border-color: #000000;-fx-border-width:1");
                            }

                            if (passed) {
                                lab_R_STAT.setText("TEST OK");
                                lab_R_STAT.setStyle("-fx-background-color:green;");
                                lab_r.setStyle("-fx-background-color:green;");
                            } else {
                                lab_R_STAT.setText("TEST NOK");
                                lab_R_STAT.setStyle("-fx-background-color:red;");
                                lab_r.setStyle("-fx-background-color:red;");
                            }

                            try {
                                Connection dbconnect = DBConnector.getConnection();
                                //todo checking the connection
                                boolean t = dbconnect.isClosed();
                                date_s = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
                                time_s = new SimpleDateFormat("HH:mm").format(new Date());
                                System.out.println(date_s+" "+time_s+"isclosed = "+ t);
                                article_s = composant_article.getText().toString();
                                model_s = composant_model_list.getValue();
                                resultat_s=passed ? "BON" : "Failed";
                                description_s="High Stat Value = "+
                                        Hvalue+"V and Low Stat Value = "+
                                        Lvalue+"V and Null Stat Value = "+
                                        Nvalue+"V";

                                String query = "INSERT INTO testpvdata VALUES (NULL,'"+article_s+"','"+model_s+"','"+resultat_s+"','"+description_s+"','"+date_s+"','"+time_s+"')";
                                PreparedStatement preparedStmt = dbconnect.prepareStatement(query);
                                boolean m = preparedStmt.execute();//not used
                                dbconnect.close();
                                System.out.println("Add was Done");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            //add data to the table
                            TableClass tableClass=new TableClass(article_s,model_s,resultat_s,description_s,date_s,time_s);
                            tableview.getItems().add(tableClass);
                            //add data to the chart
                            if (Hvalue < 5.5 && Lvalue > -5.5) {
                                yAxis.setUpperBound(5.5);
                                yAxis.setLowerBound(-5.5);
                            } else {
                                yAxis.setUpperBound(10);
                                yAxis.setLowerBound(-10);
                            }
                            ArrayList<String> dataYAxisList = new ArrayList<>();
                            int pointA = 0, pointB = 20;
                            for (int j = 1; j < 2; j++) {

                                while (!(pointA == 10)) {
                                    dataYAxisList.add(String.valueOf(Nvalue));
                                    pointA++;
                                }
                                pointA = 0;
                                while (!(pointA == pointB)) {
                                    dataYAxisList.add(String.valueOf(Hvalue));
                                    pointA++;
                                }
                                pointA = 0;
                                while (!(pointA == pointB)) {
                                    dataYAxisList.add(String.valueOf(Nvalue));
                                    pointA++;
                                }
                                pointA = 0;
                                while (!(pointA == pointB)) {
                                    dataYAxisList.add(String.valueOf(Lvalue));
                                    pointA++;
                                }
                                pointA = 0;
                                while (!(pointA == pointB)) {
                                    dataYAxisList.add(String.valueOf(Nvalue));
                                    pointA++;
                                }
                            }
                            chartupdate(dataYAxisList);
                            dataYAxisList.clear();
                        }
                    });

                } catch (Exception e) {
                    System.out.println(e.toString());
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
                pWaitIndicator.setVisible(false);
                start_test.setDisable(false);
            }
        } );
        threadGetMesure.setName("start test");
        threadGetMesure.setDaemon(true);
        threadGetMesure.start();
    }

    //for test Event :
    public void test_Event(ActionEvent event) {
        TXRXDataforTest(chosenPort.getInputStream(), chosenPort.getOutputStream());
    }

    public void TXRXDataforTest(InputStream is, OutputStream os) {

        Scanner scanner = new Scanner(is);
        PrintWriter output = new PrintWriter(os);
        output.print("ts");
        String line2 = "";
        output.flush();
        output.close();
        i = 0;
        while (scanner.hasNextLine()) {
            i++;
            //Thread.sleep(500);
            line2 = scanner.nextLine();
            System.out.println(line2);
            monitorText.appendText(line2 + "\n");
            if (line2.equals("dataFin"))
                break;
            if (i > 12)
                System.out.println("Time OUT problem");
            if (i > 12)
                break;
        }
        scanner.close();
        System.out.println("TXRX close");
    }

    public void SQLTestAddforTest(ActionEvent event){
        try {
            Connection con = DBConnector.getConnection();
            String user_DB =user_text.getText().toString();
            String pass_DB= user_text.getText().toString();
            String query = "INSERT INTO user VALUES ('"+user_DB+"','"+pass_DB+"')";
            PreparedStatement preparedStmt = con.prepareStatement(query);
            boolean m = preparedStmt.execute();
            con.close();
            if(m)
            System.out.println("Add Done");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void SQLTestShow(ActionEvent event){
        try {
            Connection con = DBConnector.getConnection();
            ResultSet  rs =con.createStatement().executeQuery("select * from user");
            while(rs.next()){
                System.out.println(
                        "user: "
                        + rs.getString("user")
                        + "\n"
                        + "Password: "
                        + rs.getString("pass")
                );

                monitorText.appendText(
                        "user: "
                        + rs.getString("user")
                        + "\n"
                        + "Password: "
                        + rs.getString("pass")+"\n"
                );
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clear_text_area(ActionEvent event) {

        monitorText.clear();

    }
}
