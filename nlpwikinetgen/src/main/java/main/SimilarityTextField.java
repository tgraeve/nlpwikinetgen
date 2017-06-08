package main;

import javafx.scene.control.TextField;

public class SimilarityTextField extends TextField {
	
	public SimilarityTextField() {
		this.setPromptText("0-1");
	}
	
	public void appendText(String text) {
		super.appendText(text);
	}
	
    public void replaceText(int start, int end, String text) {
        if (this.getText().concat(text).matches("(?!1\\.)^(0|1)(\\.\\d*)?$")) {
            super.replaceText(start, end, text);
        }
    }

    public void replaceSelection(String text) {
        if (this.getText().concat(text).matches("(?!1\\.)^(0|1)(\\.\\d*)?$")) {
            super.replaceSelection(text);
        }
    }
}