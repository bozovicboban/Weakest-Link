import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LogLabel extends Label {
    public LogLabel(String str, Color color){
        super();
        setText(str);
        setTextFill(color);
        setFont(Font.font(getFont().getFamily(), FontWeight.BOLD, getFont().getSize()));
    }
}
