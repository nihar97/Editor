package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;

/**
 * Created by We on 01-03-2016.
 */
public class Editor extends Application {
    private static int WINDOW_WIDTH = 500;
    private static int WINDOW_HEIGHT = 500;
    private static TextList openedFile = new TextList();
    private TextList fileContents = new TextList();
    private Group root = new Group();
    private double mousePressedX;
    private double mousePressedY;
    public Rectangle myCursor;
    private Render renderingObject;
    private Text[] textArray;
    private String fileName;
    private double usableScreenWidth;
    private final int scrollBarWidth = 20;
    private ScrollBar scrollBar;
    public OperationStack operationStack = new OperationStack();

    private class KeyEventHandler implements EventHandler<KeyEvent> {
        int textCenterX;
        int textCenterY;

        private static final int STARTING_FONT_SIZE = 12;
        private static final int STARTING_TEXT_POSITION_X = 250;
        private static final int STARTING_TEXT_POSITION_Y = 250;

        /**
         * The Text to display on the screen.
         */
        private Text displayText = new Text(STARTING_TEXT_POSITION_X, STARTING_TEXT_POSITION_Y, "");
        private int fontSize = STARTING_FONT_SIZE;
        private Group keyroot;
        private String fontName = "Verdana";


        public KeyEventHandler(final Group kroot, int windowWidth, int windowHeight) {
            textCenterX = windowWidth / 2;
            textCenterY = windowHeight / 2;
            myCursor = new Rectangle();
            this.keyroot = kroot;
            // Initialize some empty text and add it to root so that it will be displayed.
            displayText = new Text(textCenterX, textCenterY, "");
            // Always set the text origin to be VPos.TOP! Setting the origin to be VPos.TOP means
            // that when the text is assigned a y-position, that position corresponds to the
            // highest position across all letters (for example, the top of a letter like "I", as
            // opposed to the top of a letter like "e"), which makes calculating positions much
            // simpler!
            displayText.setTextOrigin(VPos.TOP);
            displayText.setFont(Font.font(fontName, fontSize));
//            fileContents.addFirst(displayText);
            renderingObject = new Render(fileContents, WINDOW_WIDTH - scrollBarWidth, WINDOW_HEIGHT, fontName, fontSize);
            myCursor.setHeight(Math.round(displayText.getLayoutBounds().getHeight()));
            myCursor.setWidth(1);
            myCursor.setX(0);
//            textArray = renderingObject.coordinate();
            // All new Nodes need to be added to the root in order to be displayed.
//            root.getChildren().addAll(textArray);
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            String characterTyped = keyEvent.getCharacter();
            if (keyEvent.getEventType() == KeyEvent.KEY_TYPED && !keyEvent.isShortcutDown() &&
                    !characterTyped.equals("\n") && !characterTyped.equals("\r")) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.
                Text toAdd = new Text(characterTyped);
                if (fileContents.size() == 0 && characterTyped.charAt(0) != 8) {
                    fileContents.addFirst(toAdd);
                    renderingObject.setFileList(fileContents);
                    textArray = renderingObject.coordinate();
                    myCursor.setX(5 + toAdd.getLayoutBounds().getWidth());
                    myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                    operationStack.undoStack.push(toAdd.getText(), "insert", new double[]{myCursor.getX(), myCursor.getY()});
                    operationStack.canRedo = false;
                    scrollBar.setMax(checkScrollBar());

//                    changeCursor();
                    keyroot.getChildren().addAll(textArray);
                } else if (characterTyped.charAt(0) == 8 && fileContents.size() != 0) {
                    TextList.TextNode toDelete;
                    if (fileContents.size() == 1) {
                        toDelete = fileContents.removeLast();
                        myCursor.setX(5);
                        myCursor.setY(0);
                    } else {
                        toDelete = fileContents.backspace();
                        myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth());
                        myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                    }

                    keyroot.getChildren().remove(toDelete.ch);
                    operationStack.undoStack.push(toDelete.ch.getText(), "delete", new double[]{myCursor.getX(), myCursor.getY()});


                    renderingObject.setFileList(fileContents);
                    if (textArray.length != 1) {
                        textArray = renderingObject.coordinate();
                    }
                    operationStack.canRedo = false;
                    scrollBar.setMax(checkScrollBar());
//                    TextList.TextNode temp = renderingObject.findClosestTextObjectWithoutCursor(myCursor.getX(), myCursor.getY()).prev;
//                    myCursor.setX(temp.ch.getX());
//                    myCursor.setY(temp.ch.getY());
////                    changeCursor();
//                    keyroot.getChildren().addAll(textArray);
                } else if (characterTyped.charAt(0) != 8) {
                    fileContents.addAtCursor(toAdd);
                    keyroot.getChildren().remove(myCursor);
//                    root.getChildren().removeAll(textArray);
                    renderingObject.setFileList(fileContents);
                    textArray = renderingObject.coordinate();
//                    TextList.TextNode temp = renderingObject.findClosestTextObjectWithoutCursor(myCursor.getX(), myCursor.getY()).next;
                    myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth());
                    myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                    operationStack.undoStack.push(toAdd.getText(), "insert", new double[]{myCursor.getX(), myCursor.getY()});
                    operationStack.canRedo = false;
                    scrollBar.setMax(checkScrollBar());
//                    changeCursor();
                    keyroot.getChildren().add(toAdd);
                    keyroot.getChildren().add(myCursor);
                }
//                if (fileContents.size() > 0) {
//                    // Ignore control keys, which have non-zero length, as well as the backspace
//                    // key, which is represented as a character of value = 8 on Windows.
//                    displayText.setText(characterTyped);
//                    keyEvent.consume();
//                }

                // centerText();
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    myCursor.setY(fileContents.cursor.next.ch.getY() + scrollBar.getValue());
                    double[] coordinates;
                    coordinates = renderingObject.cursorPosition(myCursor.getX(), myCursor.getY());
                    myCursor.setX(coordinates[0]);
                    myCursor.setY(coordinates[1]);
                } else if (code == KeyCode.DOWN) {
                    myCursor.setY(fileContents.cursor.next.ch.getY() + fileContents.cursor.next.ch.getLayoutBounds().getHeight() * 2 + scrollBar.getValue());
                    double[] coordinates;
                    coordinates = renderingObject.cursorPosition(myCursor.getX(), myCursor.getY());
                    myCursor.setX(coordinates[0]);
                    myCursor.setY(coordinates[1]);
                } else if (code == KeyCode.LEFT) {
                    myCursor.setX(fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth() + fileContents.cursor.next.prev.ch.getX());
                    myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                    fileContents.cursor.next = fileContents.cursor.next.prev;
                } else if (code == KeyCode.RIGHT) {
                    if (fileContents.cursor.next.ch.getText() != "") {
                        myCursor.setX(fileContents.cursor.next.ch.getLayoutBounds().getWidth() + fileContents.cursor.next.ch.getX());
                        myCursor.setY(fileContents.cursor.next.ch.getY() + scrollBar.getValue());
                        fileContents.cursor.next = fileContents.cursor.next.next;
                    }
                } else if (code == KeyCode.ENTER) {
                    Text toAdd = new Text("\n");
                    fileContents.addAtCursor(toAdd);
//                    root.getChildren().removeAll(textArray);
                    renderingObject.setFileList(fileContents);
                    textArray = renderingObject.coordinate();
                    operationStack.undoStack.push(toAdd.getText(), "insert", new double[]{myCursor.getX(), myCursor.getY()});
                    operationStack.canRedo = false;
                    scrollBar.setMax(checkScrollBar());
                    keyroot.getChildren().add(toAdd);
                    myCursor.setX(5);
//                    myCursor.setY(fileContents.cursor.next.ch.getY() + fileContents.cursor.next.ch.getLayoutBounds().getHeight());
                    myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                } else if (keyEvent.isShortcutDown()) {
                    if (code == KeyCode.S) {
                        try {
                            FileWriter writer = new FileWriter(fileName);
                            for (Text t : fileContents) {
                                writer.write(t.getText());
                            }
                            writer.close();
                            operationStack.canRedo = false;
                        } catch (IOException IOexception) {
                            System.out.println("Unable to save because:  " + IOexception);
                        }
                    }
                    if (code == KeyCode.EQUALS) {
                        fontSize += 4;
                        renderingObject.setFontSize(fontSize);
                        textArray = renderingObject.coordinate();
                        myCursor.setHeight(textArray[0].getLayoutBounds().getHeight());
                        myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getHeight());
                        myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                        scrollBar.setMax(checkScrollBar());
                        operationStack.canRedo = false;
                    }
                    if (code == KeyCode.MINUS) {
                        fontSize -= 4;
                        renderingObject.setFontSize(fontSize);
                        textArray = renderingObject.coordinate();
                        myCursor.setHeight(textArray[0].getLayoutBounds().getHeight());
                        myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getHeight());
                        myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                        scrollBar.setMax(checkScrollBar());
                        operationStack.canRedo = false;
                    }

                    if (code == KeyCode.Z) {
                        OperationStack.StackNode undoedObject = operationStack.undo();
                        operationStack.canRedo = true;
                        if (undoedObject.type == "insert") {
                            TextList.TextNode toDelete = fileContents.backspace();
                            myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth());
                            myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                            keyroot.getChildren().remove(toDelete.ch);
                            renderingObject.setFileList(fileContents);
                            textArray = renderingObject.coordinate();
                            scrollBar.setMax(checkScrollBar());
                        } else if (undoedObject.type == "delete") {
                            Text toAdd = new Text(undoedObject.character);
                            fileContents.addAtCursor(toAdd);
//                            root.getChildren().removeAll(textArray);
                            renderingObject.setFileList(fileContents);
                            textArray = renderingObject.coordinate();
                            double[] newPos = renderingObject.cursorPositionWithoutCursor(undoedObject.cursorPos[0], undoedObject.cursorPos[1]);
                            myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth());
                            myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                            scrollBar.setMax(checkScrollBar());
                            keyroot.getChildren().add(toAdd);
                        }
                    }
                    if (code == KeyCode.Y) {
                        if (operationStack.canRedo) {
                            OperationStack.StackNode undoedObject = operationStack.redo();
                            if (undoedObject.type == "delete") {
                                TextList.TextNode toDelete = fileContents.backspace();
                                myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth());
                                myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                                keyroot.getChildren().remove(toDelete.ch);
                                renderingObject.setFileList(fileContents);
                                textArray = renderingObject.coordinate();
                                scrollBar.setMax(checkScrollBar());
                            } else if (undoedObject.type == "insert") {
                                Text toAdd = new Text(undoedObject.character);
                                fileContents.addAtCursor(toAdd);
//                                root.getChildren().removeAll(textArray);
                                renderingObject.setFileList(fileContents);
                                textArray = renderingObject.coordinate();
                                double[] newPos = renderingObject.cursorPositionWithoutCursor(undoedObject.cursorPos[0], undoedObject.cursorPos[1]);
                                myCursor.setX(fileContents.cursor.next.prev.ch.getX() + fileContents.cursor.next.prev.ch.getLayoutBounds().getWidth());
                                myCursor.setY(fileContents.cursor.next.prev.ch.getY() + scrollBar.getValue());
                                scrollBar.setMax(checkScrollBar());
                                keyroot.getChildren().add(toAdd);
                            }
                        }
                    }
                    if (code == KeyCode.P) {
                        System.out.println(myCursor.getX() + ", " + myCursor.getY());
                    }


                }
            }
        }


    }

    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private boolean blackOrNot = true;

        private CursorBlinkEventHandler() {
            changeColor();
        }

        private void changeColor() {
            if (blackOrNot) {
                myCursor.setFill(Color.BLACK);
            } else {
                myCursor.setFill(Color.WHITE);
            }
            blackOrNot = !blackOrNot;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /**
         * A Text object that will be used to print the current mouse position.
         */

        MouseClickEventHandler() {
            // For now, since there's no mouse position yet, just create an empty Text object.
            // We want the text to show up immediately above the position, so set the origin to be
            // VPos.BOTTOM (so the x-position we assign will be the position of the bottom of the
            // text).
            mousePressedX = 0;
            mousePressedY = 0;
            // Add the positionText to root, so that it will be displayed on the screen.

        }

        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            mousePressedX = mouseEvent.getX();
            mousePressedY = mouseEvent.getY() + scrollBar.getValue();
            myCursor.setX(renderingObject.cursorPosition(mousePressedX, mousePressedY)[0]);
            myCursor.setY(renderingObject.cursorPosition(mousePressedX, mousePressedY)[1]);

        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a TextNode that will be the parent of all things displayed on the screen.

        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnMouseClicked(new MouseClickEventHandler());
        primaryStage.setTitle("Editor");
        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setPrefHeight(WINDOW_HEIGHT);
        scrollBar.setMin(0);
        usableScreenWidth = WINDOW_WIDTH - scrollBarWidth;
        scrollBar.setLayoutX(usableScreenWidth);
        Group textRoot = new Group();
        root.getChildren().add(textRoot);
        root.getChildren().add(scrollBar);
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(textRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        textRoot.getChildren().add(myCursor);
        makeCursorBlink();
        openFile(this.getParameters().getRaw().get(0), textRoot);
        scrollBar.setMax(checkScrollBar());
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                WINDOW_WIDTH = newValue.intValue();
                scrollBar.setLayoutX(WINDOW_WIDTH - scrollBarWidth);
                renderingObject.setWindowWidth(WINDOW_WIDTH - (int) scrollBarWidth);
                textArray = renderingObject.coordinate();

            }
        });

        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                WINDOW_HEIGHT = newValue.intValue();
                scrollBar.setPrefHeight(WINDOW_HEIGHT);
                renderingObject.setWindowHeight(WINDOW_HEIGHT);
                textArray = renderingObject.coordinate();
            }
        });

        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                // newValue describes the value of the new position of the scroll bar. The numerical
                // value of the position is based on the position of the scroll bar, and on the min
                // and max we set above. For example, if the scroll bar is exactly in the middle of
                // the scroll area, the position will be:
                //      scroll minimum + (scroll maximum - scroll minimum) / 2
                // Here, we can directly use the value of the scroll bar to set the height of Josh,
                // because of how we set the minimum and maximum above.
                textRoot.setLayoutY(-newValue.intValue());
            }
        });


        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public void makeCursorBlink() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorBlink = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorBlink);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void openFile(String fileName, Group root) {
        this.fileName = fileName;
        File inputFile = new File(fileName);
        try {

            // Check to make sure that the input file exists!
            FileReader reader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int intRead = -1;
            // Keep reading from the file input read() returns -1, which means the end of the file
            // was reached.
            if ((intRead = bufferedReader.read()) != -1) {
                String charRead = String.valueOf(Character.toChars(intRead));
                openedFile.addFirst(new Text(charRead));
            }
            if (intRead == -1) {
                openedFile = new TextList();
            } else {
                while ((intRead = bufferedReader.read()) != -1) {
                    String charRead = String.valueOf(Character.toChars(intRead));

                    openedFile.addAtCursor(new Text(charRead));
                }
            }

            bufferedReader.close();
        } catch (FileNotFoundException fileNotFoundException) {
            try {
                boolean fileCreated = inputFile.createNewFile();
                if (fileCreated) {
                    openedFile = new TextList();
                }
            } catch (IOException ioException) {
                System.out.println("Error when saving; exception was: " + ioException);
            }
        } catch (IOException ioException) {
            System.out.println("Error when copying; exception was: " + ioException);
        }
        fileContents = openedFile;
        if (fileContents.size() != 0) {
            renderingObject.setFileList(fileContents);
            textArray = renderingObject.coordinate();
            myCursor.setX(textArray[textArray.length - 1].getX() + textArray[textArray.length - 1].getLayoutBounds().getWidth());
            myCursor.setY(textArray[textArray.length - 1].getY());
            root.getChildren().addAll(textArray);
        }
    }

    public double checkScrollBar() {
        if (textArray != null) {
            if (textArray[textArray.length - 1].getY() <= WINDOW_HEIGHT) {
                return 0;
            } else return textArray[textArray.length - 1].getY() - WINDOW_HEIGHT;
        }
        return 0;
    }


    public static void main(String[] args) {
//        testTrippyShit test = new testTrippyShit();
//        test.openFile(args[0]);
        launch(args);


    }
}
