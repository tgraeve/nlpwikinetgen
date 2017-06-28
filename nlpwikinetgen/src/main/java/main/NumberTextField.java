package main;

import javafx.scene.control.TextField;

public class NumberTextField extends TextField {
	
	public NumberTextField() {
	}
	
	@Override
	public void appendText(String text) {
		super.appendText(text);
	}
	
    @Override
	public void replaceText(int start, int end, String text) {
        if (this.getText().concat(text).matches("\\d*")) {
            super.replaceText(start, end, text);
        }
    }

    @Override
	public void replaceSelection(String text) {
        if (this.getText().concat(text).matches("\\d*")) {
            super.replaceSelection(text);
        }
    }
}