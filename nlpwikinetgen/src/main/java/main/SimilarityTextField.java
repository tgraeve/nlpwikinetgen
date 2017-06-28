package main;

import javafx.scene.control.TextField;

public class SimilarityTextField extends TextField {
	
	public SimilarityTextField() {
		this.setPromptText("0-1");
	}
	
	@Override
	public void appendText(String text) {
		super.appendText(text);
	}
	
    @Override
	public void replaceText(int start, int end, String text) {
        if (this.getText().concat(text).matches("(?!1\\.)^(0|1)(\\.\\d*)?$")) {
            super.replaceText(start, end, text);
        }
    }

    @Override
	public void replaceSelection(String text) {
        if (this.getText().concat(text).matches("(?!1\\.)^(0|1)(\\.\\d*)?$")) {
            super.replaceSelection(text);
        }
    }
}