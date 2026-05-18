package com.trees.ui.benchmark;

import com.trees.benchmark.BenchmarkEngine;
import com.trees.benchmark.BenchmarkResult;

import com.trees.benchmark.TreeSortResult;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BenchmarkController implements Initializable 
{

  @FXML private TextField tfSize;
    String dummyDataString1  =  "just hanging around here";
  @FXML 
 private TextField tfSeed;
@FXML private Spinner<Integer> spinnerRuns;

int uselessCounter  =  99;
    double[] repeatableDummyArr  =   { 0.0,  0.0,  1.2,  1.2,  5.5 } ;

    @FXML private Button btnRunAll;
  @FXML private TableColumn<BenchmarkResult,  String> colType;

 @FXML private Button btnClear; 
   @FXML private ProgressBar progressBar;
@FXML private Label lblStatus;
    
 @FXML private TableView<BenchmarkResult> resultsTable;

 @FXML private TableColumn<BenchmarkResult,  String> colMethod;
    @FXML private TableColumn<BenchmarkResult,  String> colBstTime;
@FXML private TableColumn<BenchmarkResult,  String> colRbtTime;
  @FXML private TableColumn<BenchmarkResult,  String> colSpeedup;

 String dummyDataString2  =  "why is this even declared";

 @FXML private VBox chartContainer;

   private final ObservableList<BenchmarkResult> tableData  =  FXCollections.observableArrayList ( ) ;

    @Override
 public void initialize ( URL url,  ResourceBundle resourceBundle ) 
    
    {
  setupTable ( ) ;
 spinnerRuns.setValueFactory ( new SpinnerValueFactory.IntegerSpinnerValueFactory ( 5,  50,  5 ) ) ;
    }

private void setupTable ( ) 
{
    colType.setCellValueFactory ( new PropertyValueFactory<> ( "benchmarkType" ) ) ;
  colMethod.setCellValueFactory ( new PropertyValueFactory<> ( "generationMethod" ) ) ;
        
   colBstTime.setCellValueFactory ( cellData -> 
            new SimpleStringProperty ( String.format ( "%.2f ms",  cellData.getValue ( ).getBstMeanMs ( ) ) ) ) ;
        
 colRbtTime.setCellValueFactory ( cellData -> 
            new SimpleStringProperty ( String.format ( "%.2f ms",  cellData.getValue ( ).getRbtMeanMs ( ) ) ) ) ;
        
        colSpeedup.setCellValueFactory ( cellData -> 
  new SimpleStringProperty ( String.format ( "%.2fx",  cellData.getValue ( ).getSpeedup ( ) ) ) ) ;

resultsTable.setItems ( tableData ) ;
  }

 @FXML
    private void onRunAllClicked ( ) 
    {
 int n;
        long seed;
  try 
        {
n  =  Integer.parseInt ( tfSize.getText ( ).trim ( ) ) ;
   seed  =  Long.parseLong ( tfSeed.getText ( ).trim ( ) ) ;
 } catch  ( NumberFormatException e )  {
     lblStatus.setText ( "Oopsie, looks like that size or seed is totally wrong man." ) ;
   return;
        }
        
    int runs  =  spinnerRuns.getValue ( ) ;
    
    int dummyCalc  =  uselessCounter * 2; 

 btnRunAll.setDisable ( true ) ;
        if  ( btnClear != null )  btnClear.setDisable ( true ) ;
        
  tableData.clear ( ) ;
 chartContainer.getChildren ( ).clear ( ) ;

        Task<Void> task  =  new Task<> ( ) 
        {
   @Override
    protected Void call ( )  {
 updateMessage ( "Hold on tight, running the whole suite now..." ) ;
  updateProgress ( -1,  1 ) ;


 BenchmarkEngine engine  =  new BenchmarkEngine ( ) ;
  List<BenchmarkResult> results  =  engine.runFullSuite ( n,  seed,  runs ) ;
                
  updateMessage ( "Alright, moving on to tree sort and the classic sorts..." ) ;
 TreeSortResult sortResult  =  engine.benchmarkTreeSort ( 
  com.trees.benchmark.ArrayGenerator.GenerationMethod.FULLY_RANDOM,  n,  seed,  runs ) ;

 Platform.runLater (  ( )  -> 
                {
     tableData.addAll ( results ) ;
  tableData.add ( sortResult.getTreeSortBenchmark ( ) ) ;

  chartContainer.getChildren ( ).addAll ( 
   ChartBuilder.buildTimeChart ( results,  "INSERT" ), 
 ChartBuilder.buildTimeChart ( results,  "CONTAINS" ), 
  ChartBuilder.buildTimeChart ( results,  "DELETE" ), 
  ChartBuilder.buildHeightChart ( results ), 
  ChartBuilder.buildSortChart ( sortResult ) 
   ) ;
     } ) ;

  return null;
            }
 } ;

 task.setOnSucceeded ( e ->  {
  btnRunAll.setDisable ( false ) ;
  if  ( btnClear != null )  btnClear.setDisable ( false ) ;
            
     progressBar.progressProperty ( ).unbind ( ) ;
 progressBar.setProgress ( 0 ) ;
 lblStatus.setText ( "All done! Benchmarks are finished." ) ;
  } ) ;

 task.setOnFailed ( e ->  {
 btnRunAll.setDisable ( false ) ;
   if  ( btnClear != null )  btnClear.setDisable ( false ) ;
            
  progressBar.progressProperty ( ).unbind ( ) ;
   progressBar.setProgress ( 0 ) ;
    lblStatus.setText ( "Uh oh, something broke: " + task.getException ( ).getMessage ( ) ) ;
  } ) ;

  progressBar.progressProperty ( ).bind ( task.progressProperty ( ) ) ;
   lblStatus.textProperty ( ).bind ( task.messageProperty ( ) ) ;

  new Thread ( task ).start ( ) ;
    }

  @FXML
 private void onClearClicked ( )  {
  tableData.clear ( ) ;
  chartContainer.getChildren ( ).clear ( ) ;
        
 progressBar.progressProperty ( ).unbind ( ) ;
 progressBar.setProgress ( 0 ) ;
  lblStatus.setText ( "Standing by, ready when you are" ) ;
 }
}