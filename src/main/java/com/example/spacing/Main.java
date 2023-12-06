package com.example.spacing;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

// THREE PRESES WITH -R-
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Spacer");

        // Button to open Earth Window
        Button button1 = new Button("Earth Model");
        button1.setOnAction(e -> {
            try {
                openEarthWindow(primaryStage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Button to open Space Window
        Button button2 = new Button("Experimental Space Model");
        button2.setOnAction(e -> {
            try {
                openSpaceWindow(primaryStage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        // Layout for the main window
        VBox layout = new VBox(10);
        layout.getChildren().addAll(button1, button2);
        layout.setAlignment(Pos.CENTER); // Center the buttons vertically

        // Set up the main scene
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void openEarthWindow(Stage stage) throws Exception {
        Earth earth = new Earth();
        earth.start(stage);
    }

    // Method to open a new window
    private void openSpaceWindow(Stage stage) throws Exception {
        Space space = new Space();
        space.start(stage);
    }

    public class ISSTracker {

        private static final String ASTROS_API_URL = "http://api.open-notify.org/astros.json";
        private static final String ISS_NOW_API_URL = "http://api.open-notify.org/iss-now.json";

        public static String displayAstronautInfo() {
            try {
                URL url = new URL(ASTROS_API_URL);
                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();

                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                scanner.close();

                String jsonString = response.toString();

                // Parse the JSON string
                JSONObject jsonObject = new JSONObject(jsonString);

                // Extract the "people" array
                JSONArray peopleArray = jsonObject.getJSONArray("people");
                StringBuilder b = new StringBuilder();
                // Extract names from the "people" array
                for (int i = 0; i < peopleArray.length(); i++) {
                    JSONObject person = peopleArray.getJSONObject(i);
                    b.append(person.getString("name")).append(", ");
                }
                return b.toString();

            } catch (IOException e) {
                throw new RuntimeException("Error when displaying astronauts", e);
            }
        }

        public static String updateISSPosition() {
            try {
                URL url = new URL(ISS_NOW_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream);
                StringBuilder response = new StringBuilder();

                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                scanner.close();

                // Parse the JSON string
                JSONObject jsonObject = new JSONObject(response.toString());

                // Extract longitude and latitude from the "iss_position" object
                JSONObject issPosition = jsonObject.getJSONObject("iss_position");
                String longitude = issPosition.getString("longitude");
                String latitude = issPosition.getString("latitude");

                // Print the values
                return "Longitude: " + longitude + System.lineSeparator() + "Latitude: " + latitude;

            } catch (IOException e) {
                throw new RuntimeException("Error when getting ISS position", e);
            }
        }
    }








    public class Earth extends Application {

        private static final float WIDTH = 1000;
        private static final float HEIGHT = 600;
        private static Label astrounauts;
        private static Label issCoordinates;
        private double anchorX, anchorY;
        private double anchorAngleX = 0;
        private double anchorAngleY = 0;
        private final DoubleProperty angleX = new SimpleDoubleProperty(0);
        private final DoubleProperty angleY = new SimpleDoubleProperty(0);
        private final Sphere earth = new Sphere(150);


        @Override
        public void start(Stage primaryStage) {

            Camera camera = new PerspectiveCamera(true);
            camera.setNearClip(1);
            camera.setFarClip(10000);
            camera.translateZProperty().set(-1000);

            Group world = new Group();
            world.getChildren().add(prepareEarth());

            Group root = new Group();
            root.getChildren().add(world);
            root.getChildren().add(prepareImageView());

            // Create a label
            issCoordinates = prepareLabel(-60, 150);
            astrounauts = prepareLabel(-320, 190);
            issCoordinates.setText(ISSTracker.updateISSPosition());
            astrounauts.setText(ISSTracker.displayAstronautInfo());
            root.getChildren().add(astrounauts);
            root.getChildren().add(issCoordinates);

            String audioFilePath = "C:\\Users\\Bohdan\\Desktop\\Space\\src\\main\\resources\\First-Moon-Landing-1969.mp3"; // Replace with the actual path to your audio file
            Media media = new Media(new File(audioFilePath).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            Button playButton = new Button("Play");
            playButton.setOnAction(e -> mediaPlayer.play());
            Button stopButton = new Button("Stop");
            stopButton.setOnAction(e -> mediaPlayer.stop());

            playButton.setLayoutY(210);
            stopButton.setLayoutY(240);

            root.getChildren().addAll(playButton, stopButton);

            Sphere spaceStation = prepareISS();
            root.getChildren().add(spaceStation);

            Sphere moon = prepareMoon();
            root.getChildren().add(moon);

            Scene scene = new Scene(root, WIDTH, HEIGHT, true);
            scene.setFill(Color.SILVER);
            scene.setCamera(camera);

            initMouseControl(world, scene, primaryStage);

            primaryStage.setTitle("Earth");
            primaryStage.setScene(scene);

            Space.rotate(earth, 24);
            prepareTimer();
            primaryStage.show();
        }

        private void prepareTimer() {
            UpdateTextInformationFromAPI threadOne = new UpdateTextInformationFromAPI(issCoordinates, astrounauts, 5000);
            threadOne.start();
//            // Create a Timeline that triggers every 5 seconds
//            Duration duration = Duration.seconds(5);
//            Timeline timeline = new Timeline(new KeyFrame(duration, new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    issCoordinates.setText(ISSTracker.updateISSPosition());
//                }
//            }));
//
//            // Set the timeline to repeat indefinitely
//            timeline.setCycleCount(Timeline.INDEFINITE);
//
//            // Start the timeline
//            timeline.play();
//
//            Duration duration2 = Duration.hours(24);
//            Timeline timeline2 = new Timeline(new KeyFrame(duration2, new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    astrounauts.setText(ISSTracker.displayAstronautInfo());
//                }
//            }));
//
//            // Set the timeline to repeat indefinitely
//            timeline2.setCycleCount(Timeline.INDEFINITE);
//
//            // Start the timeline
//            timeline2.play();
        }

        private ImageView prepareImageView() {
            Image image = new Image(Earth.class.getResourceAsStream("/galaxy.jpg"));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.getTransforms().add(new Translate(-image.getWidth() / 2, -image.getHeight() / 2, 800));
            return imageView;
        }

        private Label prepareLabel(int x, int y) {
            Label slider = new Label();
            slider.setText("Hello World In Space!");
            slider.setPrefWidth(700d);
            slider.setLayoutX(x);
            slider.setLayoutY(y);
            slider.setTranslateZ(5);
            slider.setStyle("-fx-base: black");
            return slider;
        }

        private Node prepareEarth() {
            PhongMaterial earthMaterial = new PhongMaterial();
            earthMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("/earth/earth-d.jpg")));
            earthMaterial.setSelfIlluminationMap(new Image(getClass().getResourceAsStream("/earth/earth-l.jpg")));
            earthMaterial.setSpecularMap(new Image(getClass().getResourceAsStream("/earth/earth-s.jpg")));
            earthMaterial.setBumpMap(new Image(getClass().getResourceAsStream("/earth/earth-n.jpg")));

            earth.setRotationAxis(Rotate.Y_AXIS);
            earth.setMaterial(earthMaterial);
            return earth;
        }

        private Sphere prepareISS() {
            Sphere spaceStation = new Sphere(2);
            PhongMaterial material = new PhongMaterial(Color.BLUE);
            spaceStation.setMaterial(material);

            spaceStation.setTranslateZ( -earth.getRadius() - spaceStation.getRadius() );

            Rotate rotate = new Rotate();
            rotate.setAxis(new Point3D(1, 1, 0));
            rotate.setPivotZ(-spaceStation.getTranslateZ());

            // Adding the transformation to rectangle
            spaceStation.getTransforms().addAll(rotate);

            // Create a Timeline for the rotation animation
            Timeline timeline = Space.getTimeline(10, rotate);

            // Play the animation
            timeline.play();

            // Adding the transformation to rectangle
            return spaceStation;
        }

        private Sphere prepareMoon() {
            Sphere moon = new Sphere(earth.getRadius()*0.27);
            moon.setTranslateZ( -earth.getRadius() - moon.getRadius());

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseMap(new Image(this.getClass().getResourceAsStream("/moonmap1k.jpg")));
            moon.setMaterial(material);

            Rotate rotate = new Rotate();
            rotate.setAxis(new Point3D(0, 1, 0));

            rotate.setPivotZ(-3 * moon.getTranslateZ());

            // Adding the transformation to rectangle
            moon.getTransforms().addAll(rotate);

            // Create a Timeline for the rotation animation
            Timeline timeline = Space.getTimeline(27*88, rotate);

            // Play the animation
            timeline.play();

            // Adding the transformation to rectangle
            return moon;
        }

        private void initMouseControl(Group group, Scene scene, Stage stage) {
            Rotate xRotate;
            Rotate yRotate;
            group.getTransforms().addAll(
                    xRotate = new Rotate(0, Rotate.X_AXIS),
                    yRotate = new Rotate(0, Rotate.Y_AXIS)
            );
            xRotate.angleProperty().bind(angleX);
            yRotate.angleProperty().bind(angleY);

            scene.setOnMousePressed(event -> {
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
                anchorAngleX = angleX.get();
                anchorAngleY = angleY.get();
            });

            scene.setOnMouseDragged(event -> {
                angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
                angleY.set(anchorAngleY + anchorX - event.getSceneX());
            });

            stage.addEventHandler(ScrollEvent.SCROLL, event -> {
                double delta = event.getDeltaY();
                group.translateZProperty().set(group.getTranslateZ() + delta);
            });
        }
    }





    public class Space extends Application {
        private double width;

        private static Sphere SUN;
        private static final int PLANET_K = 50;
        private static final int SPEED = 500;
        public static final int SUN_SIZE = 86400 / 50;
        public static final int MERCURY_SIZE = 2439 / 100;
        public static final int VENUS_SIZE = 12104 / 100;
        public static final int EARTH_SIZE = 6371 / 100;
        public static final int MARS_SIZE = 3389 / 100;
        public static final int JUPITER_SIZE = 69911 / 100;
        public static final int SATURN_SIZE = 58232 / 100;
        public static final int URANUS_SIZE = 25362 / 100;
        public static final int NEPTUNE_SIZE = 24622 / 100;

        public static final int MERCURY_SUN_DISTANCE = 90_900 / PLANET_K;
        public static final int VENUS_SUN_DISTANCE = 108_200 / PLANET_K;
        public static final int EARTH_SUN_DISTANCE = 149_600 / PLANET_K;
        public static final int MARS_SUN_DISTANCE = 375_350 / PLANET_K;
        public static final int JUPITER_SUN_DISTANCE = 778_350 / PLANET_K;
        public static final int SATURN_SUN_DISTANCE = 1_456_600 / PLANET_K;
        public static final int URANUS_SUN_DISTANCE = 2_933_700 / PLANET_K;
        public static final int NEPTUNE_SUN_DISTANCE = 4_433_700 / PLANET_K;

        public static final int NEPTUNE_SPIN_IN_HOURS = 16;
        public static final int URANUS_SPIN_IN_HOURS = 17;
        public static final int SATURN_SPIN_IN_HOURS = 10;
        public static final int JUPITER_SPIN_IN_HOURS = 10;
        public static final int MARS_SPIN_IN_HOURS = 24;
        public static final int EARTH_SPIN_IN_HOURS = 24;
        public static final int VENUS_SPIN_IN_HOURS = 24 * 243;
        public static final int MERCURY_SPIN_IN_HOURS = 58 * 243;

        public static final int NEPTUNE_SPIN_IN_DAYS = 60190;
        public static final int URANUS_SPIN_IN_DAYS = 30687;
        public static final int SATURN_SPIN_IN_DAYS = 10759;
        public static final int JUPITER_SPIN_IN_DAYS = 4333;
        public static final int MARS_SPIN_IN_DAYS = 687;
        public static final int EARTH_SPIN_IN_DAYS = 365;
        public static final int VENUS_SPIN_IN_DAYS = 225;
        public static final int MERCURY_SPIN_IN_DAYS = 88;

        @Override
        public void start(Stage primaryStage) throws Exception {
            Camera camera = new PerspectiveCamera();
            camera.setTranslateZ( -10000);

            primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                switch (keyEvent.getCode()) {
                    case W:
                        camera.setTranslateZ(camera.getTranslateZ() + SPEED);
                        System.out.print("Z: ");
                        System.out.println(camera.getTranslateZ());
                        break;
                    case S:
                        camera.setTranslateZ(camera.getTranslateZ() - SPEED);
                        System.out.print("Z: ");
                        System.out.println(camera.getTranslateZ());
                        break;
                    case A:
                        camera.setTranslateX(camera.getTranslateX() - SPEED);
                        System.out.print("X: ");
                        System.out.println(camera.getTranslateX());
                        break;
                    case D:
                        camera.setTranslateX(camera.getTranslateX() + SPEED);
                        System.out.print("X: ");
                        System.out.println(camera.getTranslateX());
                        break;
                    case R:
                        camera.setTranslateY(camera.getTranslateY() - SPEED);
                        System.out.print("Y: ");
                        System.out.println(camera.getTranslateY());
                        break;
                    case F:
                        camera.setTranslateY(camera.getTranslateY() + SPEED);
                        System.out.print("Y: ");
                        System.out.println(camera.getTranslateY());
                        break;
                    case P:
                        camera.setTranslateZ(camera.getTranslateZ() - 0.01);
                        break;
                }
            });
            Group mainNode = createMainNode(primaryStage);
            Scene scene = createScene(mainNode);
            scene.setCamera(camera);

            camera.setTranslateX(0);
            camera.setTranslateY(0);

            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private Scene createScene(Group mainNode) {
            ImagePattern background = new ImagePattern(getImageFromResource("/sky.png"));
            return new Scene(mainNode, background);
        }

        private Group createMainNode(Stage primaryStage) {
            initStage(primaryStage);

            SUN = createPlanet(SUN_SIZE, 0, "/sunmap.jpg", 0, 0, primaryStage);
            SUN.setTranslateZ(5000);
            rotate(SUN, JUPITER_SPIN_IN_HOURS);

            Sphere mercury = createPlanet(MERCURY_SIZE, MERCURY_SUN_DISTANCE, "/mercurymap.jpg", MERCURY_SPIN_IN_HOURS, MERCURY_SPIN_IN_DAYS, primaryStage);
            Sphere venus = createPlanet(VENUS_SIZE, VENUS_SUN_DISTANCE, "/venusmap.jpg", VENUS_SPIN_IN_HOURS, VENUS_SPIN_IN_DAYS, primaryStage);
            Sphere earth = createPlanet(EARTH_SIZE, EARTH_SUN_DISTANCE, "/earthmap1k.jpg", EARTH_SPIN_IN_HOURS, EARTH_SPIN_IN_DAYS, primaryStage);
            Sphere mars = createPlanet(MARS_SIZE, MARS_SUN_DISTANCE, "/mars_1k_color.jpg", MARS_SPIN_IN_HOURS, MARS_SPIN_IN_DAYS, primaryStage);
            Sphere jupiter = createPlanet(JUPITER_SIZE, JUPITER_SUN_DISTANCE, "/jupiter2_1k.jpg", JUPITER_SPIN_IN_HOURS, JUPITER_SPIN_IN_DAYS, primaryStage);
            Sphere saturn = createPlanet(SATURN_SIZE, SATURN_SUN_DISTANCE, "/saturnmap.jpg", SATURN_SPIN_IN_HOURS, SATURN_SPIN_IN_DAYS, primaryStage);
            Sphere uranus = createPlanet(URANUS_SIZE, URANUS_SUN_DISTANCE, "/uranusmap.jpg", URANUS_SPIN_IN_HOURS, URANUS_SPIN_IN_DAYS, primaryStage);
            Sphere neptune = createPlanet(NEPTUNE_SIZE, NEPTUNE_SUN_DISTANCE, "/neptunemap.jpg", NEPTUNE_SPIN_IN_HOURS, NEPTUNE_SPIN_IN_DAYS, primaryStage);

            return new Group(neptune, uranus, saturn, jupiter, mars, earth, venus, mercury, SUN);
        }

        private Image getImageFromResource(String resourcePath) {
            return new Image(Objects.requireNonNull(this.getClass().getResourceAsStream(resourcePath)));
        }

        private void rotateAroundSun(Sphere planet, int spinInDaysAroundSun) {
            Rotate rotate = new Rotate();
            rotate.setAxis(Rotate.Y_AXIS);
            rotate.setPivotZ(planet.getTranslateZ()*-2);

            // Adding the transformation to rectangle
            planet.getTransforms().addAll(rotate);

            // Create a Timeline for the rotation animation
            Timeline timeline = getTimeline(spinInDaysAroundSun, rotate);

            // Play the animation
            timeline.play();
        }

        public static Timeline getTimeline(int spinInDaysAroundSun, Rotate rotate) {
            Timeline timeline = new Timeline(
                    new KeyFrame(
                            Duration.millis((double) spinInDaysAroundSun / MERCURY_SPIN_IN_DAYS),
                            event -> {
                                // Increment the rotation angle
                                rotate.setAngle(rotate.getAngle() + 0.05);
                            }
                    )
            );

            // Set the cycle count to indefinite for continuous rotation
            timeline.setCycleCount(Timeline.INDEFINITE);
            return timeline;
        }


        private Sphere createPlanet(double size, double distance, String texturePath, int spinInHours, int spinAroundSunInDays, Stage primaryStage) {
            Sphere planet = new Sphere(relativeSize(size));
            planet.setTranslateZ(distance);

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseMap(getImageFromResource(texturePath));
            planet.setMaterial(material);

//        rotate(planet, spinInHours);
            rotateAroundSun(planet, spinAroundSunInDays);
            return planet;
        }

        private void initStage(Stage primaryStage) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());

            this.width = primaryStage.getWidth();
        }

        private double relativeSize(double value) {
            return value * (1440d / width);
        }

        public static void rotate(Sphere planet, int totalSpinInHours) {
            RotateTransition rotateTransition = new RotateTransition();
            rotateTransition.setNode(planet);
            rotateTransition.setDuration(Duration.seconds(totalSpinInHours));
            rotateTransition.setAxis(Rotate.Y_AXIS);
            rotateTransition.setFromAngle(0);
            rotateTransition.setToAngle(360);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.play();
        }
    }


    public class UpdateTextInformationFromAPI extends Thread {

        private Label coordLabel;
        private Label crewLabel;
        private int delayInMillis;

        public UpdateTextInformationFromAPI(Label coordLabel, Label crewLabel, int delayInMillis) {
            this.coordLabel = coordLabel;
            this.crewLabel = crewLabel;
            this.delayInMillis = delayInMillis;
        }

        @Override
        public void run() {
            try {
                // Infinite loop to keep updating information
                while (true) {
                    // Sleep for the specified delay
                    Thread.sleep(delayInMillis);

                    // Update UI components using Platform.runLater to ensure it runs on the JavaFX Application Thread
                    // This is necessary because UI updates should be done on the JavaFX Application Thread
                    // Inside Platform.runLater, you can safely modify JavaFX UI components
                    javafx.application.Platform.runLater(() -> {
                        coordLabel.setText(ISSTracker.updateISSPosition());
                    });

//                    System.out.println("Thread running. Updating...");
                }
            } catch (InterruptedException ex) {
                // Handle InterruptedException by throwing a RuntimeException
                throw new RuntimeException(ex);
            }
        }
    }

}