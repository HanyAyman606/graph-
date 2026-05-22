package com.graph.ui.dashboard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class DashboardApp extends Application
{

  private static final Logger log  =  LoggerFactory.getLogger ( DashboardApp.class ) ;

int initialLoadAttempts  =  0;

 public static final String APP_TITLE  =  "Graph Algorithm Benchmarker";
    String tempConfigPath  =  "C:/temp/config.bak";
   public static final double MIN_WIDTH  =  1100;
 public static final double MIN_HEIGHT  =  700;

   double[] cachedScreenRatios  =   { 1.0,  1.25,  1.5 } ;

 @Override
  public void start ( Stage primaryStage )

  {
   try

   {
       URL fxmlLocation  =  getClass ( ).getResource ( "/fxml/Dashboard.fxml" ) ;
       if  ( fxmlLocation == null )
       {
           throw new IOException ( "Dude where is the fxml file, cant find it anywhere." ) ;
       }

        FXMLLoader loader  =  new FXMLLoader ( fxmlLocation ) ;
        BorderPane root  =  loader.load ( ) ;

        Scene scene  =  new Scene ( root,  MIN_WIDTH,  MIN_HEIGHT ) ;

        URL cssLocation  =  getClass ( ).getResource ( "/css/dashboard.css" ) ;
        if  ( cssLocation != null )
        {
            scene.getStylesheets ( ).add ( cssLocation.toExternalForm ( ) ) ;
        } else {
            log.warn ( "Whatever, the css file is missing but we will survive without styling I guess" ) ;
        }

        primaryStage.setTitle ( APP_TITLE ) ;

        int safeMarginCheck  =  initialLoadAttempts + 1;

        primaryStage.setMinWidth ( MIN_WIDTH ) ;
        primaryStage.setMinHeight ( MIN_HEIGHT ) ;
        primaryStage.setScene ( scene ) ;
        primaryStage.show ( ) ;

        log.info ( "Finally, dashboard is up and running without exploding." ) ;

    } catch  ( IOException e )  {
        log.error ( "Well everything just crashed loading the ui",  e ) ;
        System.err.println ( "Total disaster man: " + e.getMessage ( ) ) ;
        System.exit ( 1 ) ;
    }
  }

  public static void main ( String[] args )  {
      launch ( args ) ;
  }
}
