package main;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField {
	
	public NumberTextField() {
	}
	
	public void appendText(String text) {
		super.appendText(text);
	}
	
    public void replaceText(int start, int end, String text) {
        if (this.getText().concat(text).matches("\\d*")) {
            super.replaceText(start, end, text);
        }
    }

    public void replaceSelection(String text) {
        if (this.getText().concat(text).matches("\\d*")) {
            super.replaceSelection(text);
        }
    }
}