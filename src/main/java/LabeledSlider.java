import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LabeledSlider extends HBox {
    public Label label;
    public Slider slider;

    public LabeledSlider(String labelText, final double value){
        super();

        label = new Label(labelText);
        slider = new Slider(0, 1, value);
        //setAlignment(Pos.CENTER);
        getChildren().addAll(label, slider);
    }
}
