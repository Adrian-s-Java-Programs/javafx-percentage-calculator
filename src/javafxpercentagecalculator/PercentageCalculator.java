package javafxpercentagecalculator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/*
 * This class creates an input form with two fields, where a user can enter two numbers and calculate their difference, as a percentage.
 * The application accepts integral or floating point numbers, with or without sign.
 * The application is developed using JavaFX 8.
 */

public class PercentageCalculator extends Application {

  /* defining maximum input length */
  final int maxLength = 50;

  /*
   * This variable will contain an output message to be displayed.
   * We want the users to be able to copy the result, so they could paste it where they need it. 
   * Unfortunately, JavaFX 8 does not allow the user to copy a Label's text, so we use a TextField instead of a Label.
   * The TextField for the result will be later made to look like a Label.
   */
  TextField result = new TextField();

  /*
   * This method is used for validating user input.
   * Each input field can contain digit characters (0-9), a dot (for floating point numbers) and an optional + or - sign at the beginning.
   * Input is limited to a number of "maxLength" characters (the optional sign at the beginning is not counted).
   */
  public boolean validateValue(String inputString){

    if( (inputString.length() > maxLength + 1 ) && (inputString.toCharArray()[0]=='-' | inputString.toCharArray()[0]=='+') ) return false;
    if( (inputString.length() > maxLength ) && (inputString.toCharArray()[0]!='-' & inputString.toCharArray()[0]!='+') ) return false;

    Pattern p = Pattern.compile("[-+]?[0-9]+([.][0-9]+)?");
    Matcher m = p.matcher(inputString);
    boolean b = m.matches();
    return b;

  }

  /*
   * Since JavaFX 8 does not come with a built-in method to limit the number of characters typed in a TextField,
   * this method will be used as a workaround.
   * Maximum allowed length is "maxLength" if the given input has no sign at the beginning,
   * or maxLength+1 if the input text starts with '+' or '-' character.
   * If the input exceeds the maximum allowed length, it is shortened to match that length.
   */
  public void limitSize(TextField tf, int maximumLength){

    if ( (tf.getText().length() > 0) && ((tf.getText().toCharArray()[0]=='-') | (tf.getText().toCharArray()[0]=='+')) ) maximumLength++; 

    if ( tf.getText().length() > maximumLength ) {
      String resizedText=tf.getText(0, maximumLength);

      /*
       * Since "limitSize" method will be called inside a change listener, modifying the input field by "limitSize" method will result in triggering 
       * again the same listener, which will re-call the "limitSize" method, thus ending up with another "limitSize" method starting to execute
       * before the first one ended, which will lead to undesired behavior, such as incorrect caret positioning or exceptions thrown in the background. 
       * To prevent this, we use "Platform.runLater" method.
       */
      Platform.runLater(() -> {
        int position = tf.getCaretPosition();
        tf.setText(resizedText);
        tf.positionCaret(position);
      });

      showMessage("Each input field is limited to "+maxLength+" characters (plus optional number sign).");

    }

  }

  /* for high precision operations we use BigDecimal type */
  public String calculatePercentage(BigDecimal initialValue, BigDecimal finalValue){

    MathContext mc = new MathContext(100, RoundingMode.HALF_EVEN);
    String result = "";

    if (initialValue.compareTo(finalValue) == 0) {
      result = "The two values are the same.";
      return result;
    }

    if (initialValue.compareTo(new BigDecimal("0"))==0) {
      result = "Percentage cannot be computed when the initial value is 0.";
      return result;
    }

    if (initialValue.compareTo(finalValue) == -1) {
      BigDecimal bd = ( (finalValue.subtract(initialValue,mc)).divide(initialValue, mc) ).multiply(new BigDecimal("100"), mc);
      result = "An increase by "+bd.stripTrailingZeros().toPlainString()+" %";
      return result;
    }

    if (initialValue.compareTo(finalValue) == 1) {
      BigDecimal bd = ( (initialValue.subtract(finalValue,mc)).divide(initialValue, mc) ).multiply(new BigDecimal("100"), mc);
      result = "A decrease by "+bd.stripTrailingZeros().toPlainString()+" %";
      return result;
    }

    return result;

  }


  public static void main(String[] args){
    Application.launch(args);
  }

  @Override
  public void start(Stage stage){

    /* creating the input TextField for the initial number */
    TextField initialValueField = new TextField();

    /* an empty input field is invalid, so the text cursor should be colored in red */
    initialValueField.setStyle("-fx-text-inner-color: red;");

    /* adding a listener, to be called whenever the text for the initial value changes */
    initialValueField.textProperty().addListener((observable,oldValue,newValue)->{

      /* the input is not allowed to exceed a certain size */
      limitSize(initialValueField, maxLength);

      /* if the input is invalid, it will be colored in red; otherwise, it will be colored in black */
      if(validateValue(initialValueField.getText()) == false)
        initialValueField.setStyle("-fx-text-inner-color: red;");
      else
        initialValueField.setStyle("-fx-text-inner-color: black;");

    });

    /* Creating the input TextField for the new number */
    TextField newValueField = new TextField();

    /* an empty input field is invalid, so the text cursor should be colored in red */
    newValueField.setStyle("-fx-text-inner-color: red;");

    /* adding a listener, to be called whenever the text for the new value changes */
    newValueField.textProperty().addListener((observable,oldValue,newValue)->{

      /* the input is not allowed to exceed a certain size */
      limitSize(newValueField, maxLength);

      /* if the input is invalid, it will be colored in red; otherwise, it will be colored in black */
      if(validateValue(newValueField.getText()) == false)
        newValueField.setStyle("-fx-text-inner-color: red;");
      else
        newValueField.setStyle("-fx-text-inner-color: black;");

    });

    initialValueField.setPrefColumnCount(30);
    newValueField.setPrefColumnCount(30);

    /* creating a Submit button, with S as its mnemonic */
    Button submitButton = new Button("_Submit");

    /* setting the Submit button as default button, to be called if the user presses Enter key */
    submitButton.setDefaultButton(true);

    /* adding EventHandler to the button */
    submitButton.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent e){

        String message="";
        int numberOfInvalidFields = 0;

        if(validateValue(initialValueField.getText()) == false){
          message ="Invalid input for the first value.";
          numberOfInvalidFields++;
        }

        if(validateValue(newValueField.getText()) == false){
          message ="Invalid input for the second value.";
          numberOfInvalidFields++;
        }

        if(numberOfInvalidFields==2)
          message="Invalid input for both values.";

        if(validateValue(initialValueField.getText())&validateValue(newValueField.getText())) {
          message = calculatePercentage ( new BigDecimal(initialValueField.getText()), new BigDecimal(newValueField.getText()) );
        }

        showMessage(message);

      }
    });

    /* create a Swap Values button, with W as its mnemonic (S is already taken, as mnemonic for the Submit button) */
    Button swapButton = new Button("S_wap Values");

    /* adding EventHandler to the button */
    swapButton.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent e){
        String tempText = initialValueField.getText();
        initialValueField.setText(newValueField.getText());
        newValueField.setText(tempText);
        result.setText("");
        result.setVisible(false);
      }
    });

    /* creating a Clear button, with C as its mnemonic */
    Button clearButton = new Button("_Clear");

    /* setting the Clear button as cancel button, to be called if the user presses Escape key */
    clearButton.setCancelButton(true);

    /* adding EventHandler to the button */
    clearButton.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent e){
        initialValueField.clear();
        newValueField.clear();
        result.setText("");
        result.setVisible(false);
      }
    });

    /* creating an HBox */
    HBox buttonBox = new HBox();

    /* adding children to the HBox */
    buttonBox.getChildren().addAll(submitButton, swapButton, clearButton);

    /* setting the vertical spacing between children to 15px */
    buttonBox.setSpacing(15);

    /* creating a VBox */
    VBox root = new VBox();

    /* creating a GridPane */
    GridPane gPane = new GridPane();
    gPane.addRow(1, new Label("Initial Value:"), initialValueField);
    gPane.addRow(2, new Label("New Value:"), newValueField);

    /* we make the result TextField look like a Label that will be made visible when needing to display a message */
    result.setStyle("-fx-background-color:transparent; text-align: 0px; -fx-padding: 0;");
    result.setEditable(false);
    result.setVisible(false);

    /* adding the children to the VBox */
    root.getChildren().addAll(gPane, buttonBox, result);

    /* setting the vertical spacing between children to 15px */
    root.setSpacing(15);

    /* Setting the Size of the VBox */
    root.setMinSize(800, 250);

    /* setting the style for the VBox */
    root.setStyle("-fx-padding: 10;"
                + "-fx-border-width: 2;" 
                + "-fx-border-insets: 5;"
                + "-fx-border-radius: 5;"
                + "-fx-border-color: #1E90FF;");

    /* creating the Scene */
    Scene scene = new Scene(root);

    /* adding the scene to the Stage */
    stage.setScene(scene);

    /* setting the title of the Stage */
    stage.setTitle("Percentage calculator");

    /* show the Stage */
    stage.show();

  }

  public void showMessage(String message){
    result.setText(message);
    result.setVisible(true);
  }

}
