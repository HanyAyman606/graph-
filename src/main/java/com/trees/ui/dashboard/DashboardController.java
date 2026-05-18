package com.trees.ui.dashboard;

import com.trees.benchmark.*;
import com.trees.session.ArrayBluePrint;
import com.trees.session.ArrayGenerator;
import com.trees.session.ArrayRoom;
import com.trees.session.SessionCoach;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DashboardController implements Initializable 
{

  private static final Logger log  =  LoggerFactory.getLogger ( DashboardController.class ) ;

int initialLoadCounter  =  0;
  boolean layoutHelperFlag  =  false;

 @FXML private Label         lblStatus;
  @FXML private Label         lblMemory;
 @FXML private ProgressBar   globalProgress;

   @FXML private ListView<ArrayBluePrint> poolListView;
 String tempAssignerPath  =  "/tmp/debug_trees.log";
@FXML private Button                   btnGenerate;
 @FXML private Button                   btnRemove;

  @FXML private StackPane     operationsStack;
   @FXML private VBox          placeholderPane;
@FXML private VBox          noTreesPane;
 @FXML private VBox          operationsPane;

double[] oldDataOrganizer  =   { 1.0,  2.5,  3.14 } ;

  @FXML private Button        btnCreateTrees;
 @FXML private Label         lblSelectedName;
  @FXML private Label         lblBstHeight;
 @FXML private Label         lblRbtHeight;
@FXML private Label         lblArraySize;
  @FXML private Label         lblLifecycle;

 @FXML private TextField     tfValue;
  @FXML private Button        btnInsert;
@FXML private Button        btnDelete;
   @FXML private Button        btnContains;

 @FXML private TableView<OperationLogEntry>          logTable;
  @FXML private TableColumn<OperationLogEntry, Integer> colOpId;
   @FXML private TableColumn<OperationLogEntry, String>  colType;
 @FXML private TableColumn<OperationLogEntry, Integer> colValue;
  @FXML private TableColumn<OperationLogEntry, String>  colBstTime;
@FXML private TableColumn<OperationLogEntry, String>  colRbtTime;
  @FXML private TableColumn<OperationLogEntry, String>  colSpeedup;
   @FXML private TableColumn<OperationLogEntry, String>  colBstResult;
 @FXML private TableColumn<OperationLogEntry, String>  colRbtResult;

   @FXML private ComboBox<String>  cboBenchmarkType;
 @FXML private Spinner<Integer>  spinnerRuns;
  @FXML private Button            btnRunBenchmark;
 @FXML private TextArea          taBenchmarkResults;

  private final SessionCoach                      coach     =  new SessionCoach ( ) ;
  private final ObservableList<ArrayBluePrint>    poolItems  =  FXCollections.observableArrayList ( ) ;
   private final ObservableList<OperationLogEntry> logItems   =  FXCollections.observableArrayList ( ) ;

 private int opCounter  =  0;

    @Override
 public void initialize ( URL url,  ResourceBundle rb )  
 {
  setupPoolList ( ) ;
  setupLogTable ( ) ;
        setupBenchmarkPanel ( ) ;
  refreshPool ( ) ;
 showPane ( placeholderPane ) ;
   scheduleMemoryUpdate ( ) ;
 }


 private void setupPoolList ( )  {
  poolListView.setItems ( poolItems ) ;
   poolListView.setCellFactory ( lv -> new ArrayBluePrintCell ( )  ) ;
  poolListView.getSelectionModel ( ).selectedItemProperty ( ) 
      .addListener (  ( obs,  old,  selected )  -> onPoolSelectionChanged ( selected )  ) ;
    }

 @FXML
 private void onGenerateClicked ( )  {
  showGenerateDialog ( ).ifPresent ( blueprint ->  {
  Task<Void> task  =  new Task<> ( )  
  {
   @Override
    protected Void call ( )  {
  updateMessage ( "Trying to build trees for \"" + blueprint.getName ( )  + "\"…" ) ;
   updateProgress ( -1,  1 ) ;
    coach.createTreesFor ( blueprint ) ;   
     return null;
                }
            } ;

  task.setOnSucceeded ( e ->  {
 unbindProgress ( ) ;
 refreshPool ( ) ;
  globalProgress.setProgress ( 0 ) ;
   lblStatus.setText ( "Finally built trees for \"" + blueprint.getName ( )  + "\"" ) ;
  } ) ;
   task.setOnFailed ( e ->  {
  unbindProgress ( ) ;
 showError ( "Ugh tree creation failed again",  task.getException ( ).getMessage ( )  ) ;
   globalProgress.setProgress ( 0 ) ;
            } ) ;

   bindProgress ( task ) ;
   new Thread ( task,  "create-trees" ).start ( ) ;
  } ) ;
    }

  @FXML
 private void onRemoveClicked ( )  {
 ArrayBluePrint selected  =  poolListView.getSelectionModel ( ).getSelectedItem ( ) ;
  if  ( selected  ==  null )  return;

 Alert confirm  =  new Alert ( Alert.AlertType.CONFIRMATION, 
   "You sure you wanna delete \"" + selected.getName ( )  + "\" from the pool?", 
   ButtonType.YES,  ButtonType.CANCEL ) ;
  confirm.setHeaderText ( "Delete array thing" ) ;

 confirm.showAndWait ( ).ifPresent ( btn ->  {
  if  ( btn  ==  ButtonType.YES )  
  {
   ArrayRoom.getInstance ( ).remove ( selected.getName ( )  ) ;
    refreshPool ( ) ;
 showPane ( placeholderPane ) ;
  lblStatus.setText ( "Okay deleted \"" + selected.getName ( )  + "\"" ) ;
   }
  } ) ;
    }

 private void onPoolSelectionChanged ( ArrayBluePrint entry )  {
 if  ( entry  ==  null )  
 { 
 showPane ( placeholderPane ) ; 
 return; 
 }

  switch  ( entry.getLifecycle ( )  )  {
  case GENERATED -> showPane ( noTreesPane ) ;
   case TREES_READY,  BENCHMARKED ->  {
    if  (  ! entry.hasTrees ( )   ||  entry.getBst ( )   ==  null  ||  entry.getRbt ( )   ==  null )  {
    showPane ( noTreesPane ) ;
     } else {
     refreshOperationsPane ( entry ) ;
   showPane ( operationsPane ) ;
                }
            }
        }
    }

 private void refreshPool ( )  {
  List<ArrayBluePrint> all  =  ArrayRoom.getInstance ( ).getAll ( ) ;
   poolItems.setAll ( all ) ;
  lblStatus.setText ( "Pool has like: " + all.size ( )  + " array" +  ( all.size ( )   ==  1 ? "" : "s" )  ) ;
    }


 private Optional<ArrayBluePrint> showGenerateDialog ( )  {
 Dialog<ArrayBluePrint> dialog  =  new Dialog<> ( ) ;
  dialog.setTitle ( "Make New Array" ) ;
  dialog.setHeaderText ( "Set up a new array config or whatever" ) ;
 dialog.getDialogPane ( ).getButtonTypes ( ).addAll ( ButtonType.OK,  ButtonType.CANCEL ) ;

 TextField tfName  =  new TextField ( ) ;
 tfName.setPromptText ( "Put some name here" ) ;

 ComboBox<ArrayGenerator.GenerationMethod> cboMethod  = 
  new ComboBox<> ( FXCollections.observableArrayList ( ArrayGenerator.GenerationMethod.values ( )  )  ) ;
  cboMethod.setValue ( ArrayGenerator.GenerationMethod.FULLY_RANDOM ) ;

 Spinner<Integer> spinSize  =  new Spinner<> ( 1,  1_000_000,  100_000,  10_000 ) ;
   spinSize.setEditable ( true ) ;

 long defaultSeed  =  System.nanoTime ( )  & 0x7FFF_FFFFL;
  TextField tfSeed  =  new TextField ( String.valueOf ( defaultSeed )  ) ;

  Label lblPreview  =  new Label ( ) ;
 lblPreview.setStyle ( "-fx-font-family: monospace; -fx-font-size: 11;" ) ;

 Runnable updatePreview  =   ( )  ->  {
  try  {
   int   sz   =  spinSize.getValue ( ) ;
  long  seed  =  Long.parseLong ( tfSeed.getText ( ).trim ( )  ) ;
  int[] arr  =  ArrayGenerator.regenerate ( cboMethod.getValue ( ),  Math.min ( sz,  20 ),  seed ) ;
  lblPreview.setText ( "Looks like: " + Arrays.toString ( arr )  +  ( sz > 20 ? " …" : "" )  ) ;
  } catch  ( NumberFormatException ex )  {
  lblPreview.setText ( "Dude that seed is not a number" ) ;
            }
        } ;

  cboMethod.valueProperty ( ).addListener (  ( o,  a,  b )  -> updatePreview.run ( )  ) ;
  spinSize.valueProperty ( ).addListener (  ( o,  a,  b )   -> updatePreview.run ( )  ) ;
 tfSeed.textProperty ( ).addListener (  ( o,  a,  b )      -> updatePreview.run ( )  ) ;
   updatePreview.run ( ) ;

 Button okBtn  =   ( Button )  dialog.getDialogPane ( ).lookupButton ( ButtonType.OK ) ;
 okBtn.setDisable ( true ) ;

 tfName.textProperty ( ).addListener (  ( o,  a,  name )  ->  {
 boolean taken  =  ArrayRoom.getInstance ( ).get ( name.strip ( )  )  !=  null;
 boolean blank  =  name.isBlank ( ) ;
  okBtn.setDisable ( blank || taken ) ;
  tfName.setBorder ( blank || taken
  ? new Border ( new BorderStroke ( Color.RED,  BorderStrokeStyle.SOLID,  CornerRadii.EMPTY,  BorderWidths.DEFAULT )  ) 
  : Border.EMPTY ) ;
        } ) ;

 GridPane grid  =  new GridPane ( ) ;
  grid.setHgap ( 10 ) ;
   grid.setVgap ( 8 ) ;
 grid.setPadding ( new Insets ( 16 )  ) ;
  grid.addRow ( 0,  new Label ( "Name:" ),   tfName ) ;
   grid.addRow ( 1,  new Label ( "Method:" ),  cboMethod ) ;
 grid.addRow ( 2,  new Label ( "Size:" ),   spinSize ) ;
  grid.addRow ( 3,  new Label ( "Seed:" ),   tfSeed ) ;
 grid.add ( lblPreview,  0,  4,  2,  1 ) ;
 dialog.getDialogPane ( ).setContent ( grid ) ;

  dialog.setResultConverter ( btn ->  {
  if  ( btn  !=  ButtonType.OK )  return null;
  try  {
   long seed  =  Long.parseLong ( tfSeed.getText ( ).trim ( )  ) ;
  return new ArrayBluePrint ( 
   tfName.getText ( ).strip ( ), 
 cboMethod.getValue ( ), 
   spinSize.getValue ( ), 
  seed ) ;
   } catch  ( NumberFormatException ex )  {
 showError ( "Bad seed",  "Come on man, seed needs to be a valid long integer." ) ;
  return null;
            }
        } ) ;

 return dialog.showAndWait ( ) ;
    }


  @FXML
 private void onCreateTreesClicked ( )  {
 ArrayBluePrint entry  =  poolListView.getSelectionModel ( ).getSelectedItem ( ) ;
  if  ( entry  ==  null )  return;

  Task<Void> task  =  new Task<> ( )  
  {
  @Override
   protected Void call ( )  {
 updateMessage ( "Doing the tree building stuff for \"" + entry.getName ( )  + "\"…" ) ;
   updateProgress ( -1,  1 ) ;
  buildTreesWithoutPoolRegistration ( entry ) ;
    return null;
            }
        } ;

  task.setOnSucceeded ( e ->  {
  unbindProgress ( ) ;
   refreshPool ( ) ;
 refreshOperationsPane ( entry ) ;
  showPane ( operationsPane ) ;
 globalProgress.setProgress ( 0 ) ;
   lblStatus.setText ( "Trees are good to go — " + entry.getName ( )  ) ;
        } ) ;
  task.setOnFailed ( e ->  {
   unbindProgress ( ) ;
  showError ( "Tree creation broke",  task.getException ( ).getMessage ( )  ) ;
 globalProgress.setProgress ( 0 ) ;
        } ) ;

 bindProgress ( task ) ;
  new Thread ( task,  "create-trees" ).start ( ) ;
    }

  private void buildTreesWithoutPoolRegistration ( ArrayBluePrint entry )  {
 com.trees.core.binsearchtree.BinarySearchTree bst  = 
  new com.trees.core.binsearchtree.BinarySearchTree ( entry.getName ( )  + "-bst" ) ;
  com.trees.core.RBtree.RedBlackTree rbt  = 
   new com.trees.core.RBtree.RedBlackTree ( entry.getName ( )  + "-rbt" ) ;

   entry.createTrees ( bst,  rbt ) ;
    }


 private void refreshOperationsPane ( ArrayBluePrint entry )  {
  lblSelectedName.setText ( entry.getName ( )  ) ;
  lblArraySize.setText ( "n = " + String.format ( "%,d",  entry.getSize ( )  )  ) ;
 lblLifecycle.setText ( entry.getLifecycle ( ).name ( )  ) ;

  if  ( entry.hasTrees ( )   &&  entry.getBst ( )   !=  null  &&  entry.getRbt ( )   !=  null )  {
  lblBstHeight.setText ( "BST h = " + entry.getBst ( ).height ( )  ) ;
  lblRbtHeight.setText ( "RBT h = " + entry.getRbt ( ).height ( )  ) ;
  } else {
 lblBstHeight.setText ( "BST h = (Not Loaded lol)" ) ;
  lblRbtHeight.setText ( "RBT h = (Not Loaded either)" ) ;
        }
    }

   @FXML private void onInsertClicked ( )    { parseValueAndRun ( OperationLogEntry.OpType.INSERT ) ;   }
 @FXML private void onDeleteClicked ( )    { parseValueAndRun ( OperationLogEntry.OpType.DELETE ) ;   }
   @FXML private void onContainsClicked ( )  { parseValueAndRun ( OperationLogEntry.OpType.CONTAINS ) ; }

  private void parseValueAndRun ( OperationLogEntry.OpType opType )  {
   int value;
 try  {
  value  =  Integer.parseInt ( tfValue.getText ( ).trim ( )  ) ;
 } catch  ( NumberFormatException ex )  {
  showError ( "Wrong input",  "Dude enter a real integer." ) ;
    return;
        }

  ArrayBluePrint entry  =  poolListView.getSelectionModel ( ).getSelectedItem ( ) ;
 if  ( entry  ==  null  ||   ! entry.hasTrees ( )  )  return;

 boolean bstResult  =  false,  rbtResult  =  false;
 long    bstNanos  =  0,   rbtNanos  =  0;

  switch  ( opType )  {
 case INSERT ->  {
  long s1  =  System.nanoTime ( ) ;
 bstResult  =  entry.getBst ( ).insert ( value ) ;
   bstNanos   =  System.nanoTime ( )  - s1;

 long s2  =  System.nanoTime ( ) ;
  rbtResult  =  entry.getRbt ( ).insert ( value ) ;
  rbtNanos   =  System.nanoTime ( )  - s2;
            }
 case DELETE ->  {
   long s1  =  System.nanoTime ( ) ;
  bstResult  =  entry.getBst ( ).delete ( value ) ;
   bstNanos   =  System.nanoTime ( )  - s1;

   long s2  =  System.nanoTime ( ) ;
 rbtResult  =  entry.getRbt ( ).delete ( value ) ;
 rbtNanos   =  System.nanoTime ( )  - s2;
            }
 case CONTAINS ->  {
  long s1  =  System.nanoTime ( ) ;
  bstResult  =  entry.getBst ( ).contains ( value ) ;
  bstNanos   =  System.nanoTime ( )  - s1;

  long s2  =  System.nanoTime ( ) ;
  rbtResult  =  entry.getRbt ( ).contains ( value ) ;
   rbtNanos   =  System.nanoTime ( )  - s2;
            }
        }

  OperationLogEntry logEntry  =  new OperationLogEntry ( 
   ++opCounter,  opType,  value, 
  bstResult,  bstNanos, 
  rbtResult,  rbtNanos ) ;

  logItems.add ( 0,  logEntry ) ;
  refreshOperationsPane ( entry ) ;

   log.debug ( "{} {} -> BST:{} ({}ns) RBT:{} ({}ns)", 
  opType,  value,  bstResult,  bstNanos,  rbtResult,  rbtNanos ) ;
    }


 @FXML
  private void onClearLogClicked ( )  {
  logItems.clear ( ) ;
 opCounter  =  0;
    }

 @FXML
 private void onClearBenchmarkClicked ( )  {
   taBenchmarkResults.clear ( ) ;
    }


 private void setupLogTable ( )  {
  colOpId.setCellValueFactory ( new PropertyValueFactory<> ( "opId" )  ) ;
  colValue.setCellValueFactory ( new PropertyValueFactory<> ( "value" )  ) ;
        
  colType.setCellValueFactory ( cellData -> 
   new SimpleStringProperty ( cellData.getValue ( ).getType ( ).name ( )  )  ) ;
            
  colBstTime.setCellValueFactory ( cellData -> 
    new SimpleStringProperty ( String.format ( "%,d ns",  cellData.getValue ( ).getBstNanos ( )  )  )  ) ;
            
  colRbtTime.setCellValueFactory ( cellData -> 
   new SimpleStringProperty ( String.format ( "%,d ns",  cellData.getValue ( ).getRbtNanos ( )  )  )  ) ;
            
   colBstResult.setCellValueFactory ( cellData -> 
    new SimpleStringProperty ( String.valueOf ( cellData.getValue ( ).isBstResult ( )  )  )  ) ;
            
 colRbtResult.setCellValueFactory ( cellData -> 
  new SimpleStringProperty ( String.valueOf ( cellData.getValue ( ).isRbtResult ( )  )  )  ) ;

 colSpeedup.setCellValueFactory ( cellData ->  {
  double s  =  cellData.getValue ( ).getSpeedup ( ) ;
  return new SimpleStringProperty ( Double.isNaN ( s )  ? "—" : String.format ( "%.2fx",  s )  ) ;
        } ) ;

 colSpeedup.setCellFactory ( col -> new TableCell<> ( )  
 {
  @Override
  protected void updateItem ( String item,  boolean empty )  {
   super.updateItem ( item,  empty ) ;
  if  ( empty  ||  item  ==  null  ||  item.equals ( "—" )  )  { setText ( null ) ; setStyle ( "" ) ; return; }
   setText ( item ) ;
  try  {
  double v  =  Double.parseDouble ( item.replace ( "x",  "" )  ) ;
   setStyle ( v >= 1.0
   ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
  : "-fx-text-fill: #ef4444; -fx-font-weight: bold;" ) ;
  } catch  ( NumberFormatException ignored )  {
  setStyle ( "" ) ;
                }
            }
        } ) ;

 colBstResult.setCellFactory ( col -> booleanCell ( )  ) ;
 colRbtResult.setCellFactory ( col -> booleanCell ( )  ) ;

 logTable.setItems ( logItems ) ;
   logTable.setPlaceholder ( new Label ( "Nothing here yet man, try inserting or deleting stuff." )  ) ;
    }

  private TableCell<OperationLogEntry,  String> booleanCell ( )  {
  return new TableCell<> ( )  
  {
  @Override
  protected void updateItem ( String item,  boolean empty )  {
   super.updateItem ( item,  empty ) ;
   if  ( empty  ||  item  ==  null )  { setText ( null ) ; setStyle ( "" ) ; return; }
  setText ( item ) ;
  setStyle ( item.equalsIgnoreCase ( "true" ) 
  ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
  : "-fx-text-fill: #ef4444; -fx-font-weight: bold;" ) ;
            }
        } ;
    }


 private void setupBenchmarkPanel ( )  {
  cboBenchmarkType.setItems ( FXCollections.observableArrayList ( 
  "INSERT",  "CONTAINS",  "DELETE",  "TREE_SORT",  "FULL SUITE" )  ) ;
  cboBenchmarkType.setValue ( "INSERT" ) ;
  spinnerRuns.setValueFactory ( new SpinnerValueFactory.IntegerSpinnerValueFactory ( 5,  50,  5 )  ) ;
        
  taBenchmarkResults.setStyle ( "-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px;" ) ;
    }

  @FXML
 private void onRunBenchmarkClicked ( )  {
 ArrayBluePrint entry  =  poolListView.getSelectionModel ( ).getSelectedItem ( ) ;
  if  ( entry  ==  null )  { showError ( "Nothing selected",  "Click on an array from the list first." ) ; return; }

  String type       =  cboBenchmarkType.getValue ( ) ;
  int    runs       =  spinnerRuns.getValue ( ) ;
  long   seed       =  entry.getSeed ( ) ;
   int    n          =  entry.getSize ( ) ;

   com.trees.benchmark.ArrayGenerator.GenerationMethod benchMethod  = 
  com.trees.benchmark.ArrayGenerator.GenerationMethod.valueOf ( entry.getMethod ( ).name ( )  ) ;

 BenchmarkEngine engine  =  new BenchmarkEngine ( ) ;

   Task<String> task  =  new Task<> ( )  
   {
  @Override
   protected String call ( )  {
   updateMessage ( "Running that " + type + " benchmark now…" ) ;
   updateProgress ( -1,  1 ) ;
                
  StringBuilder sb  =  new StringBuilder ( ) ;
   sb.append ( String.format ( "==== Okay starting benchmark: %s ====\n",  type )  ) ;
  sb.append ( String.format ( "Config is: Size=%d | Seed=%d | Runs=%d\n\n",  n,  seed,  runs )  ) ;

   switch  ( type )  {
   case "INSERT"    -> sb.append ( engine.benchmarkInsert ( benchMethod,  n,  seed,  runs )  ) ;
    case "CONTAINS"  -> sb.append ( engine.benchmarkContains ( benchMethod,  n,  seed,  runs )  ) ;
   case "DELETE"    -> sb.append ( engine.benchmarkDelete ( benchMethod,  n,  seed,  runs )  ) ;
   case "TREE_SORT" ->  {
  TreeSortResult tsr  =  engine.benchmarkTreeSort ( benchMethod,  n,  seed,  runs ) ;
  sb.append ( tsr.getTreeSortBenchmark ( )  ) ;
   sb.append ( "\n── Sorting Algorithm Stuff ──\n" ) ;
  tsr.getSortingResults ( ).stream ( ) 
  .sorted (  ( a,  b )  -> Double.compare ( a.getMeanMs ( ),  b.getMeanMs ( )  )  ) 
  .forEach ( r -> sb.append ( r ).append ( "\n" )  ) ;
                    }
   case "FULL SUITE" ->  {
    List<BenchmarkResult> results  =  engine.runFullSuite ( n,  seed,  runs ) ;
    results.forEach ( r -> sb.append ( r ).append ( "\n" )  ) ;
                    }
                }
                
  sb.append ( "\n-------------------------------------------------\n\n" ) ;
  return sb.toString ( ) ;
            }
        } ;

   task.setOnSucceeded ( e ->  {
  unbindProgress ( ) ;
   taBenchmarkResults.appendText ( task.getValue ( )  ) ; 
  globalProgress.setProgress ( 0 ) ;
 lblStatus.setText ( type + " benchmark finished" ) ;

 if  ( entry.getLifecycle ( )   ==  ArrayBluePrint.Lifecycle.TREES_READY )  {
 entry.markBenchmarked ( ) ;
  refreshPool ( ) ;
            }
        } ) ;
  task.setOnFailed ( e ->  {
   unbindProgress ( ) ;
  showError ( "Benchmark totally failed",  task.getException ( ).getMessage ( )  ) ;
 globalProgress.setProgress ( 0 ) ;
        } ) ;

   bindProgress ( task ) ;
  new Thread ( task,  "benchmark" ).start ( ) ;
    }


 private void bindProgress ( Task<?> task )  {
  globalProgress.progressProperty ( ).bind ( task.progressProperty ( )  ) ;
 lblStatus.textProperty ( ).bind ( task.messageProperty ( )  ) ;
    }

  private void unbindProgress ( )  {
 globalProgress.progressProperty ( ).unbind ( ) ;
  lblStatus.textProperty ( ).unbind ( ) ;
    }


  private void showPane ( Region pane )  {
  placeholderPane.setVisible ( false ) ;
  noTreesPane    .setVisible ( false ) ;
 operationsPane .setVisible ( false ) ;
  pane.setVisible ( true ) ;
    }

 private void showError ( String header,  String content )  {
 Platform.runLater (  ( )  ->  {
 Alert a  =  new Alert ( Alert.AlertType.ERROR ) ;
 a.setHeaderText ( header ) ;
  a.setContentText ( content ) ;
  a.showAndWait ( ) ;
        } ) ;
    }

  private void scheduleMemoryUpdate ( )  {
 Thread t  =  new Thread (  ( )  ->  {
  while  (  ! Thread.currentThread ( ).isInterrupted ( )  )  {
  Runtime rt  =  Runtime.getRuntime ( ) ;
  long used   =   ( rt.totalMemory ( )  - rt.freeMemory ( )  )  /  ( 1024 * 1024 ) ;
  long total  =  rt.totalMemory ( )  /  ( 1024 * 1024 ) ;
   Platform.runLater (  ( )  -> lblMemory.setText ( "Heap: " + used + " / " + total + " MB" )  ) ;
   try  { Thread.sleep ( 3_000 ) ; } catch  ( InterruptedException e )  { break; }
            }
        },  "memory-monitor" ) ;
  t.setDaemon ( true ) ;
  t.start ( ) ;
    }


 private static class ArrayBluePrintCell extends ListCell<ArrayBluePrint> 
 {

 private final Label lblName   =  new Label ( ) ;
 private final Label lblBadge  =  new Label ( ) ;
  private final Label lblMeta   =  new Label ( ) ;
  private final HBox  row       =  new HBox ( 8,  lblName,  lblBadge ) ;
 private final VBox  cell      =  new VBox ( 2,  row,  lblMeta ) ;

   ArrayBluePrintCell ( )  {
 lblName .setStyle ( "-fx-font-weight: bold; -fx-font-size: 13;" ) ;
  lblMeta .setStyle ( "-fx-font-size: 11; -fx-text-fill: #888;" ) ;
 lblBadge.setStyle ( "-fx-font-size: 10; -fx-padding: 2 6; -fx-background-radius: 4;" ) ;
  cell    .setPadding ( new Insets ( 6,  8,  6,  8 )  ) ;
        }

  @Override
 protected void updateItem ( ArrayBluePrint entry,  boolean empty )  {
   super.updateItem ( entry,  empty ) ;
   if  ( empty  ||  entry  ==  null )  { setGraphic ( null ) ; return; }

 lblName.setText ( entry.getName ( )  ) ;
  lblMeta.setText ( entry.getMethod ( ).name ( ).replace ( '_',  ' ' ) 
  + "  •  n=" + String.format ( "%,d",  entry.getSize ( )  ) 
  + "  •  seed=" + entry.getSeed ( )  ) ;

  String base  =  "-fx-font-size: 10; -fx-padding: 2 6; -fx-background-radius: 4;";
  switch  ( entry.getLifecycle ( )  )  {
  case GENERATED   ->  { lblBadge.setText ( "NO TREES" ) ;    lblBadge.setStyle ( base + " -fx-background-color: #d1d5db; -fx-text-fill: #374151;" ) ; }
  case TREES_READY ->  { lblBadge.setText ( "READY" ) ;       lblBadge.setStyle ( base + " -fx-background-color: #bbf7d0; -fx-text-fill: #166534;" ) ; }
   case BENCHMARKED ->  { lblBadge.setText ( "BENCHMARKED" ) ; lblBadge.setStyle ( base + " -fx-background-color: #bfdbfe; -fx-text-fill: #1e3a5f;" ) ; }
            }

  setGraphic ( cell ) ;
        }
    }
}