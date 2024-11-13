package hw4;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * The EpidemicSimulation class generates an interactive GUI
 * and runs an epidemic simulation using the EpidemicGraph class.
 * 
 * @author Chev Kodama
 * @version 1.0
 */
public class EpidemicSimulation extends Application {
	private Scene scene;
	private BorderPane root;
	private MenuBar menuBar;
	private ToolBar setUpToolBar;
	private ToolBar simulateToolBar;
	private VBox stats;
	private VBox configurations;
	
	private float death;
	private int time;
	private float lambda;
	private int num_threads;
	private EpidemicGraph graph;
	private String current_file;
	
	private int simulating;
	private int pause_speed;
	private Thread simulation_thread;
	private volatile boolean running;
	
	private Preferences prefs;
	
	private StringProperty numThreadsLabelProperty;
	
	private StringProperty speedLabelProperty;
	private StringProperty numNodesLabelProperty;
	private StringProperty numInfectedLabelProperty;
	private StringProperty numRecoveredLabelProperty;
	private StringProperty numDeadLabelProperty;
	private StringProperty numSusceptibleLabelProperty;
	private XYChart.Series<Number, Number> infectedSeries;
	private XYChart.Series<Number, Number> recoveredSeries;
	private XYChart.Series<Number, Number> deadSeries;
	private LineChart<Number, Number> lineChart;
	private Label numNodesLabel;
	private Label speedLabel;
	private Label numInfectedLabel;
	private Label numRecoveredLabel;
	private Label numDeadLabel;
	private Label numSusceptibleLabel;
	
	/**
	 * The EpidemicSimulation constructor.
	 */
	public EpidemicSimulation() {
		/* does nothing just here because javadoc gave me a warning. */
	}
	
	/**
	 * Root method.
	 * @param args any command line arguments.
	 */
	public static void main(String args[]) {
		launch(args);
	}
	
	private EventHandler<ActionEvent> infectRandom = new EventHandler<ActionEvent>() {
		/**
		 * Gets a whole number from the user and infects
		 * that number of nodes randomly.
		 */
		@Override
		public void handle(ActionEvent event) {
			/* 
			 * this should never be true since the button that calls this
			 * is only available when it is false, but it is here for semantics.
			 */
			if ( graph.getState() != EpidemicStates.INIT ) {
				return;
			}
			int n = requestInt("Number of nodes to infect randomly:");
			if ( n == -1 ) {
				return;
			}
			graph.infectRandom(n);
			updateStats();
			startSimulation();
		}
	};
	
	private EventHandler<ActionEvent> infectDegree = new EventHandler<ActionEvent>() {
		/**
		 * Gets a whole number from the user and infects
		 * all nodes with a degree greater than it.
		 */
		@Override
		public void handle(ActionEvent event) {
			/* 
			 * this should never be true since the button that calls this
			 * is only available when it is false, but it is here for semantics.
			 */
			if ( graph.getState() != EpidemicStates.INIT ) {
				return;
			}
			
			int s = requestInt("Infect nodes with degree greater than:");
			if ( s == -1 ) {
				return;
			}
			graph.infectDegree(s);
			updateStats();
			startSimulation();
		}
	};
	
	private EventHandler<ActionEvent> infectBFS = new EventHandler<ActionEvent>() {
		/**
		 * Gets a whole number from the user and infects
		 * that number of nodes using BFS.
		 */
		@Override
		public void handle(ActionEvent event) {
			/* 
			 * this should never be true since the button that calls this
			 * is only available when it is false, but it is here for semantics.
			 */
			if ( graph.getState() != EpidemicStates.INIT ) {
				return;
			}
			
			int k = requestInt("Number of nodes to infect using BFS:");
			if ( k == -1 ) {
				return;
			}
			graph.infectBFS(k);
			updateStats();
			startSimulation();
		}
	};
	
	/**
	 * Opens a text input dialog requesting a whole number and returns it.
	 * @param message	the string prompt to display to the user.
	 * @return			the whole number entered by the user.
	 */
	private int requestInt(String message) {
		TextInputDialog textDialog = new TextInputDialog("0");
		textDialog.setGraphic(null);
		textDialog.setHeaderText("Infect Nodes");
		textDialog.setContentText(message);
		Optional<String> result = textDialog.showAndWait();
		if ( result.isPresent()) {
			String string_n = result.get();
			for ( int i = 0; i < string_n.length(); i++ ) {
				if ( !Character.isDigit(string_n.charAt(i)) ) {
					Alert alert = new Alert(AlertType.ERROR, "The value entered must be a whole number");
					Optional<ButtonType> error_result = alert.showAndWait();
					if ( error_result.isPresent() && error_result.get() == ButtonType.OK) {
						return 0;
					}
				}
			}
			return Integer.parseInt(string_n);
		} else {
            /* User pressed "Cancel" */
            return -1;
        }
	}
	
	/**
	 * Switch to the home screen.
	 */
	private void switchToHome() {
		root.setTop(menuBar);
		root.setRight(null);
		root.setCenter(stats);
		root.setLeft(null);
		if ( graph.getState() == EpidemicStates.INIT ) {
			root.setBottom(setUpToolBar);
		}
		else if ( graph.getState() == EpidemicStates.IN_PROGRESS ) {
			root.setBottom(simulateToolBar);
		}
	}
	
	/**
	 * Switch to the configurations screen.
	 */
	private void switchToConfigs() {
		if ( running ) {
			return;
		}
		
		int cores = Runtime.getRuntime().availableProcessors();
		numThreadsLabelProperty.set("Number of threads to run the simulation on.\nAvailable cores: " + cores);
		
		root.setTop(null);
		root.setRight(null);
		root.setCenter(configurations);
		root.setLeft(null);
		root.setBottom(null);
	}
	
	/**
	 * Updates the graph visual.
	 */
	private void updateStats() {
	    if (graph == null) {
	        return;
	    }
	    
	    numNodesLabelProperty.set("Total Number of Nodes: " + graph.numNodes());
        numSusceptibleLabelProperty.set("Susceptible nodes: " + (graph.numNodes() - graph.numInfected() - graph.numRecovered()) );
        numInfectedLabelProperty.set("Infected nodes: " + graph.numInfected());
        numRecoveredLabelProperty.set("Recovered nodes: " + graph.numRecovered());
        numDeadLabelProperty.set("Dead nodes: " + graph.numDead());

	    int tick = graph.getTick();
	    
	    Platform.runLater(() -> {
	        /* Update series data */
	        infectedSeries.getData().add(new XYChart.Data<>(tick, graph.numInfected()));
	        recoveredSeries.getData().add(new XYChart.Data<>(tick, graph.numRecovered()));
	        deadSeries.getData().add(new XYChart.Data<>(tick, graph.numDead()));
	    });
	    
	    switchToHome();
	}
	
	/**
	 * Checks if the simulation finished running.
	 */
	/**
	 * Checks if the simulation finished running.
	 */
	private void checkCompletion() {
	    /* Check if simulation is complete */
	    if (graph.getState() != EpidemicStates.NONE_INFECTED &&
	        graph.getState() != EpidemicStates.ALL_RECOVERED &&
	        graph.getState() != EpidemicStates.ALL_DEAD) {
	        return;
	    }

	    /* Show completion status */
	    HBox finishedBox = new HBox();
	    finishedBox.setAlignment(Pos.CENTER);
	    String status = "";
	    switch (graph.getState()) {
	        case NONE_INFECTED:
	            status = "The epidemic is over after " + graph.getTick() + " ticks.";
	            break;
	        case ALL_RECOVERED:
	            status = "All nodes have recovered after " + graph.getTick() + " ticks.";
	            break;
	        case ALL_DEAD:
	            status = "All nodes have died after " + graph.getTick() + " ticks.";
	            break;
	    }
	    Label doneSimulating = new Label("Simulation complete: " + status + "   ");
	    doneSimulating.setStyle("-fx-font-size: 28");
	    Button resetButton = new Button("Reset");
	    resetButton.setOnAction(e -> resetSimulation());

	    finishedBox.getChildren().addAll(doneSimulating, resetButton);

	    Platform.runLater(() -> {
	        root.setBottom(finishedBox);
	    });

	    /* Stop simulation thread */
	    if (simulation_thread != null && simulation_thread.isAlive()) {
	        running = false; /* Signal the thread to stop */
	        try {
	            simulation_thread.join();
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	        simulation_thread = null;
	    }
	}

	
	/**
	 * Starts the simulation if it is not started yet
	 */
	private void startSimulation() {
		if ( running ) {
			return;
		}

		running = true;
		simulating = 50;
    	Platform.runLater(() -> {
    		speedLabelProperty.set("Simulating speed: " + (100 - simulating) + "/100");
    	});
    	pause_speed = simulating;
        simulation_thread = new Thread(() -> {
            while (running) {
                if (simulating > 0) {
                    graph.nextTick(num_threads);
                    Platform.runLater(() -> updateStats());
                    
                    Platform.runLater(() -> {
                        /* Check for completion */
                        checkCompletion();
                    });
                    
                    try {
                        Thread.sleep(simulating * 1000 / 50);
                    } catch (InterruptedException e) {
                        resetSimulation();
                    }
                } else {
                    try {
                        Thread.sleep(100); /* Sleep briefly to avoid busy-waiting */
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        simulation_thread.start();
	}
	
	/**
	 * Play simulation if it is paused.
	 */
	private void playSimulation() {
	    simulating = pause_speed;
	}


	
	/**
	 * Pause simulation if it has started and is not already paused.
	 */
	private void pauseSimulation() {
		if ( simulating > 0 ) {
			simulating = 0;
			speedLabelProperty.set("Simulating speed: " + (100 - simulating) + "/100");
		}
	}
	
	/**
	 * Slow down the simulation by 1 if it is playing and this change
	 * will not cause it to pause.
	 */
	private void slowSimulation() {
		if ( simulating > 0 && simulating < 100 ) {
			simulating++;
			pause_speed++;
			speedLabelProperty.set("Simulating speed: " + (100 - simulating) + "/100");
		}
	}
	
	/**
	 * Speed up the simulation by 1 if it is playing and this change
	 * will not cause it to become greater than 100.
	 */
	private void speedSimulation() {
		if ( simulating > 1 ) {
			simulating--;
			pause_speed--;
			speedLabelProperty.set("Simulating speed: " + (100 - simulating) + "/100");
		}
	}
	
	/**
	 * Resets the simulation to having no infected nodes.
	 */
	private void resetSimulation() {
	    if (simulation_thread != null && simulation_thread.isAlive()) {
	        running = false; /* Signal the thread to stop */
	        try {
	            simulation_thread.join();
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	        simulation_thread = null;
	    }
	    /* Continue resetting the simulation */
	    graph = new EpidemicGraph(death, time, lambda);
	    graph.initialize(current_file);
	    infectedSeries.getData().clear();
	    recoveredSeries.getData().clear();
	    deadSeries.getData().clear();
	    simulating = -1;
	    updateStats();
	}

	
	/**
	 * The GUI startup method.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage) throws Exception {
		root = new BorderPane();
		menuBar = new MenuBar();
		setUpToolBar = new ToolBar();
		simulateToolBar = new ToolBar();
		stats = new VBox();
		configurations = new VBox();
		
		/* default values */
		prefs = Preferences.userNodeForPackage(hw4.EpidemicSimulation.class);
		death = Float.parseFloat(prefs.get("death", "0.5"));
		time = Integer.parseInt(prefs.get("time", "10"));
		lambda = Float.parseFloat(prefs.get("lambda", "0.7"));
		num_threads = Integer.parseInt(prefs.get("num_threads", "10"));
		current_file = prefs.get("file", "");
		
		simulating = 50;
		pause_speed = simulating;
		running = false;
		simulation_thread = null;
		
		/* Set up stats */
		stats.setAlignment(Pos.CENTER);
		stats.setStyle("-fx-background-color: #b0b4ac");

		// Create a StringProperty to hold num nodes
        numNodesLabelProperty = new SimpleStringProperty();
        // Create a Label to display num nodes and bind its text property to the numNodesLabelProperty
        numNodesLabel = new Label();
        numNodesLabel.textProperty().bind(numNodesLabelProperty);
        
        // Create a StringProperty to hold speed
        speedLabelProperty = new SimpleStringProperty();
        speedLabelProperty.set("Simulating speed: " + (100 - simulating) + "/100");
        // Create a Label to display speed and bind its text property to the speedLabelProperty
        speedLabel = new Label();
        speedLabel.textProperty().bind(speedLabelProperty);
		
		/* x-axis */
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick (Generation)");

        /* y-axis */
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Number of Nodes");

        /* Create the line chart */
        lineChart = new LineChart<Number, Number>(xAxis, yAxis);
        lineChart.setTitle("Epidemic Simulation Statistics");

        /* Define a series for Infected, Recovered, and Dead nodes */
        infectedSeries = new XYChart.Series<>();
        infectedSeries.setName("Infected");

        recoveredSeries = new XYChart.Series<>();
        recoveredSeries.setName("Recovered");

        deadSeries = new XYChart.Series<>();
        deadSeries.setName("Dead");
        
        lineChart.getData().addAll(infectedSeries, recoveredSeries, deadSeries);
		
		/* Create graph and set up its visual */
		graph = new EpidemicGraph(death, time, lambda);
		graph.initialize(current_file).equals("Success");
		
        numSusceptibleLabelProperty = new SimpleStringProperty();
        numSusceptibleLabelProperty.set("Susceptible nodes: " + (graph.numNodes() - graph.numInfected() - graph.numRecovered()) );
        numSusceptibleLabel = new Label();
        numSusceptibleLabel.textProperty().bind(numSusceptibleLabelProperty);
        
        numInfectedLabelProperty = new SimpleStringProperty();
        numInfectedLabelProperty.set("Infected nodes: " + graph.numInfected());
        numInfectedLabel = new Label();
        numInfectedLabel.textProperty().bind(numInfectedLabelProperty);
        
        numRecoveredLabelProperty = new SimpleStringProperty();
        numRecoveredLabelProperty.set("Recovered nodes: " + graph.numRecovered());
        numRecoveredLabel = new Label();
        numRecoveredLabel.textProperty().bind(numRecoveredLabelProperty);
        
        numDeadLabelProperty = new SimpleStringProperty();
        numDeadLabelProperty.set("Dead nodes: " + graph.numDead());
        numDeadLabel = new Label();
        numDeadLabel.textProperty().bind(numDeadLabelProperty);

        stats.getChildren().addAll(numNodesLabel, numSusceptibleLabel, numInfectedLabel, numRecoveredLabel, numDeadLabel, lineChart, speedLabel);

		updateStats();
		
		/* File selector */
		EventHandler<ActionEvent> choose_file = new EventHandler<ActionEvent>() {
			/**
			 * Opens a file chooser dialog and then attempts to initialize
			 * the graph using that file.
			 */
			@Override
			public void handle(ActionEvent event) {
				if ( running ) {
					return;
				}
				
				FileChooser fileChooser = new FileChooser();
				File selectedFile = fileChooser.showOpenDialog(stage);
				if ( selectedFile == null ) {
					return;
				}
				String status = graph.initialize(selectedFile.getAbsolutePath());
				if ( !status.equals("Success") ) {
					Alert alert = new Alert(AlertType.ERROR, status);
					Optional<ButtonType> error_result = alert.showAndWait();
					if ( error_result.isPresent() && error_result.get() == ButtonType.OK) {
						return;
					}
				}
				else {
					current_file = selectedFile.getAbsolutePath();
					prefs.put("file", current_file);
					updateStats();
				}
			}
		};
		
		/* Build the menu bar */
		Menu setUpMenu = new Menu("Set Up");
		MenuItem fileItem = new MenuItem("Upload Graph File");
		MenuItem configsItem = new MenuItem("Edit Configurations");
		fileItem.setOnAction(choose_file);
		configsItem.setOnAction(e -> switchToConfigs());
		
		setUpMenu.getItems().addAll(fileItem, configsItem);
		
		Menu simulateMenu = new Menu("Simulate");
		MenuItem playItem = new MenuItem("Play");
		MenuItem pauseItem = new MenuItem("Pause");
		MenuItem slowerItem = new MenuItem("Slow Down");
		MenuItem fasterItem = new MenuItem("Speed Up");
		MenuItem resetItem = new MenuItem("Reset");
		playItem.setOnAction(e -> playSimulation());
		pauseItem.setOnAction(e -> pauseSimulation());
		slowerItem.setOnAction(e -> slowSimulation());
		fasterItem.setOnAction(e -> speedSimulation());
		resetItem.setOnAction(e -> resetSimulation());
		
		simulateMenu.getItems().addAll(playItem, pauseItem, slowerItem, fasterItem, resetItem);
		
		menuBar.getMenus().addAll(setUpMenu, simulateMenu);
		
		/* Build the setup tool bar */
		Button randomButton = new Button("Infect Randomly");
		Button degreeButton = new Button("Infect by Degree");
		Button BFSButton = new Button("Infect using BFS");
		randomButton.setOnAction(infectRandom);
		degreeButton.setOnAction(infectDegree);
		BFSButton.setOnAction(infectBFS);
		
		setUpToolBar.getItems().addAll(randomButton, degreeButton, BFSButton);
		
		/* Build the simulate tool bar */
		Button slowerButton = new Button("Slow Down");
		Button playButton = new Button("Play");
		Button pauseButton = new Button("Pause");
		Button fasterButton = new Button("Speed Up");
		Button resetButton = new Button("Reset");
		slowerButton.setOnAction(e -> slowSimulation());
		playButton.setOnAction(e -> playSimulation());
		pauseButton.setOnAction(e -> pauseSimulation());
		fasterButton.setOnAction(e -> speedSimulation());
		resetButton.setOnAction(e -> resetSimulation());
		
		simulateToolBar.getItems().addAll(slowerButton, playButton, pauseButton, fasterButton, resetButton);
		
		/* Build the configurations screen */
		configurations.setAlignment(Pos.CENTER);
		Label deathChanceLabel = new Label("Death chance");
		TextField deathChanceField = new TextField(String.format("%f", death));
		Label timeLabel = new Label("Infection duration");
		TextField timeField = new TextField(String.format("%d", time));
		Label lambdaLabel = new Label("Lambda: Force of Infection");
		TextField lambdaField = new TextField(String.format("%f", lambda));
		// Create a StringProperty to hold the core count
        numThreadsLabelProperty = new SimpleStringProperty();
        // Create a Label to display the core count and bind its text property to the numThreadsLabelProperty
        Label numThreadsLabel = new Label();
        numThreadsLabel.textProperty().bind(numThreadsLabelProperty);
		TextField numThreadsField = new TextField(String.format("%d", num_threads));
		Button saveButton = new Button("Save");
		
		deathChanceField.setMaxWidth(400);
		timeField.setMaxWidth(400);
		lambdaField.setMaxWidth(400);
		numThreadsField.setMaxWidth(400);
		
		saveButton.setOnAction(e -> {
			String dcText = deathChanceField.getText();
			String timeText = timeField.getText();
			String lambdaText = lambdaField.getText();
			String ntText = numThreadsField.getText();
			float dc = death;
			int t = time;
			float l = lambda;
			int nt = num_threads;
			try {
                dc = Float.parseFloat(dcText);
                if ( dc < 0 || dc > 1 ) {
                	throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
            	Alert alert = new Alert(AlertType.ERROR, "Death chance must be a value between 0 and 1, inclusive.");
				Optional<ButtonType> error_result = alert.showAndWait();
				if ( error_result.isPresent() && error_result.get() == ButtonType.OK) {
					return;
				}
            }
			try {
                t = Integer.parseInt(timeText);
                if ( t <= 0 ) {
                	throw new NumberFormatException();
                }
                
            } catch (NumberFormatException ex) {
            	Alert alert = new Alert(AlertType.ERROR, "Infection duration must be an integer greater than 0.");
				Optional<ButtonType> error_result = alert.showAndWait();
				if ( error_result.isPresent() && error_result.get() == ButtonType.OK) {
					return;
				}
            }
			try {
                l = Float.parseFloat(lambdaText);
                if ( l < 0 || l > 1 ) {
                	throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
            	Alert alert = new Alert(AlertType.ERROR, "Lambda must be a value between 0 and 1, inclusive.");
				Optional<ButtonType> error_result = alert.showAndWait();
				if ( error_result.isPresent() && error_result.get() == ButtonType.OK) {
					return;
				}
            }
			try {
                nt = Integer.parseInt(ntText);
                if ( nt <= 0 ) {
                	throw new NumberFormatException();
                }
                
            } catch (NumberFormatException ex) {
            	Alert alert = new Alert(AlertType.ERROR, "Number of threads must be an integer greater than 0.");
				Optional<ButtonType> error_result = alert.showAndWait();
				if ( error_result.isPresent() && error_result.get() == ButtonType.OK) {
					return;
				}
            }
			death = dc;
			time = t;
			lambda = l;
			num_threads = nt;
			
			graph.editConfigs(death, time, lambda);
			
			prefs.put("death", String.format("%f", death));
			prefs.put("time", String.format("%d", time));
			prefs.put("lambda", String.format("%f", lambda));
			prefs.put("num_threads", String.format("%d", num_threads));
			
			switchToHome();
		});
		
		configurations.getChildren().addAll(deathChanceLabel, deathChanceField, timeLabel, timeField,
				lambdaLabel, lambdaField, numThreadsLabel, numThreadsField, saveButton);
		
		switchToHome();
		
		/* Set stage */
		scene = new Scene(root, 800, 450);
		stage.setTitle("Epidemic Simulation");
		stage.setScene(scene);
		stage.setMinWidth(600);
		stage.setMinHeight(300);
		stage.setMaximized(true);
		stage.show();
	}
}