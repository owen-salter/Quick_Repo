package com.example.fileparsing;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelloApplication extends Application {
    //Input File Folder
    Label inputFileTitle;
    TextField inputFileField;
    Button inputFileButton;

    //Time Colums
    Label timeColumnTitle;
    TextField timeColumnField;

    //Time Offests
    Label timeOffsetTitle;
    TextField timeOffsetField;

    //Time Units
    Label timeUnitTitle;
    TextField timeUnitField;

    //Header
    Label headerIgnoreTitle;
    TextField headerIgnoreField;

    //Generate
    Button generateButton;

    Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        //FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));

        //Input File Folder
        inputFileTitle = new Label("Input Folder Path");
        inputFileField = new TextField("");
        inputFileField.setPrefColumnCount(10);
        inputFileButton = new Button("Select");
        inputFileButton.setOnAction(this::handle);

        //Time Colums
        timeColumnTitle = new Label("Enter Time Columns");
        timeColumnField = new TextField("");
        timeColumnField.setPrefColumnCount(15);

        //Time Offests
        timeOffsetTitle = new Label("Enter Time Offset");
        timeOffsetField = new TextField("");
        timeOffsetField.setPrefColumnCount(15);

        //Time Units
        timeUnitTitle = new Label("Time Units");
        timeUnitField = new TextField("");
        timeUnitField.setPrefColumnCount(5);
        //Header
        headerIgnoreTitle = new Label("Ignore Header Rows");
        headerIgnoreField = new TextField("");
        headerIgnoreField.setPrefColumnCount(5);

        //Generate
        generateButton = new Button("Generate");
        generateButton.setOnAction(this::handle);

        GridPane layout = new GridPane();
        //StackPane layout = new StackPane();
        layout.add(inputFileTitle,0,0,1,1);
        layout.add(inputFileField,0,1,1,1);
        layout.add(inputFileButton,1,1,1,1);
        layout.add(timeColumnTitle,0,2,1,1);
        layout.add(timeColumnField,0,3,1,1);
        layout.add(timeOffsetTitle,0,4,1,1);
        layout.add(timeOffsetField,0,5,1,1);
        layout.add(timeUnitTitle,0,6,1,1);
        layout.add(timeUnitField,0,7,1,1);
        layout.add(headerIgnoreTitle,1,6,1,1);
        layout.add(headerIgnoreField,1,7,1,1);
        layout.add(generateButton,2,7,1,1);


        Scene scene = new Scene(layout, 375,200);


        stage.setTitle("DSP File Parser");
        stage.setScene(scene);
        stage.show();
    }

    public void handle(ActionEvent event){
        if(event.getSource()==inputFileButton){
            System.out.println("Inp");
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(stage);

            if(selectedDirectory == null){
                //No Directory selected
            }else{
                inputFileField.setText(selectedDirectory.getAbsolutePath());
                System.out.println(selectedDirectory.getAbsolutePath());
            }


        }
        else if(event.getSource()==generateButton){
            int TS[] = {0,7,14,21};
            double TI[] = {0,0,0,0};

            double unit = 1;
            try {
                getFolder(inputFileField.getText(),
                        Arrays.stream(timeColumnField.getText().split(",")).mapToInt(Integer::parseInt).toArray(),
                        Arrays.stream(timeOffsetField.getText().split(",")).mapToDouble(Double::parseDouble).toArray(),
                        getUnit(timeUnitField.getText()), Integer.parseInt(headerIgnoreField.getText()));
                System.out.println("Gen");
            }catch (Exception invalidInputs){
                System.out.println("ERROR: invalid inputs");
                //invalidInputs.printStackTrace();

            }
        }
    }

    public static double getUnit(String unit){
        if(unit.equals("S")){
            return 1.0;
        }
        else if(unit.equals("MS")){
            return 0.001;
        }
        else if(unit.equals("MIN")){
            return 60;
        }
        else{
            return 1;
        }
    }

    public static void getFolder(String path, int TS[], double TI[], double unit, int ignoreHeader){
        String[] pathnames;
        try {
            File f = new File(path);

            pathnames = f.list();

            for (String pathname : pathnames) {
                if(pathname.substring(pathname.length()-3,pathname.length()).equals("csv")){;
                    Parse(path+"\\"+pathname,TS,TI,pathname,unit, ignoreHeader);
                }
            }
        }
        catch(Exception fileSearchError){
            System.out.println("Could not find files in dir");
            fileSearchError.printStackTrace();
        }
    }

    public static void Parse(String path, int TS[], double TI[], String name, double unit, int ignoreHeader) throws IOException{
        BufferedReader rd = new BufferedReader(new FileReader(path));
        // Send the header to the void !!!!!
        for(int i = 0; i < ignoreHeader; i++){
            rd.readLine();
        }
        List <String> titles = new ArrayList<String>();
        String line = rd.readLine();
        titles = (Arrays.asList(line.split(",")));
        line = rd.readLine();

        ArrayList <List<String>> file = new ArrayList<List<String>>();
        int i = 0;
        do {
            file.add(Arrays.asList(line.split(",")));

            line = rd.readLine();
            i++;
        }while(line != null);
        rd.close();

        int CT = 0;

        for(i = 0; i < titles.size(); i++){
            if(CT < TS.length - 1 && i == TS[CT + 1]) {
                CT++;
            }
            else if(i != TS[CT]){
                String subBuff[][] = new String[file.size()+1][2];
                subBuff[0][0] = titles.get(TS[CT]); // "Time"
                subBuff[0][1] = titles.get(i);      // "Y-Value"

                for(int j = 0; j < file.size();j++){
                    //Double.toString((Double.parseDouble(titles.get(TS[CT])) + TI[CT])*unit.toString(); // "Time"
                    subBuff[j+1][0] = Double.toString( (Double.parseDouble("1") + TI[CT]) * unit );
                    subBuff[j+1][1] = (file.get(j).get(i));

                }

                saveTSV("C:\\Users\\owens\\PycharmProjects\\DSP_File_Parsing\\Out_Data",subBuff,name, titles.get(i));

            }

        }
    }

    public static void saveTSV(String path,String buffer[][], String fileName, String subID){

        try{
            String clean_subID = subID.replaceAll("\s+","_");
            clean_subID= clean_subID.replaceAll("/","-");

            PrintWriter writer = new PrintWriter(path + "\\" + fileName + "_" + clean_subID + ".tsv", "UTF-8");
            for(int i = 0; i < buffer.length; i++){
                writer.println(buffer[i][0] + "\t" + buffer[i][1]);
            }
            writer.close();

        }catch(IOException e){
            System.out.println("Could not write to file");
            e.printStackTrace();
        }

    }


}