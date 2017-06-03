package main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.event.ChangeEvent;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class DataBuilderController implements Initializable {
	@FXML
	private Button btnConfigFile;
	@FXML
	private Button btnOutputFolder;
	@FXML
	private TextField tfConfigFile;
	@FXML
	private TextField tfOutputFolder;
	@FXML
	private CheckBox cbWholeWiki;
	@FXML
	private TextField tfCategory;
	@FXML
	private TreeView<String> tvOptions;
	@FXML
	private Pane paneParam;
	
	
	public void loadConfigFile(ActionEvent event) {
		FileChooser fc = new FileChooser();
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		fc.getExtensionFilters().addAll(new ExtensionFilter("Text File", "*.txt"));
		File file = fc.showOpenDialog(null);
		
		if (file != null) {
			tfConfigFile.setText(file.getAbsolutePath());
		} else {
			System.out.println("Config File selection failed.");
		}
	}
	
	public void loadOutputFolder(ActionEvent event) {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setInitialDirectory(new File(System.getProperty("user.dir")));
		File dir = dc.showDialog(null);
		
		if (dir != null) {
			tfOutputFolder.setText(dir.getAbsolutePath());
		} else {
			System.out.println("Output directory selection failed.");
		}
	}
	
	public void toggleSource(ActionEvent event) {
		if (cbWholeWiki.isSelected()) {
			tfCategory.setDisable(true);
		} else {
			tfCategory.setDisable(false);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		TreeItem<String> root = new TreeItem<> ("Root");
		
		TreeItem<String> simpleFilter = new TreeItem<> ("Simple Filter");
			TreeItem<String> wikiMinorFlag = new TreeItem<> ("Wiki Minor Flag");
			
		root.getChildren().addAll(simpleFilter);
		simpleFilter.getChildren().addAll(wikiMinorFlag);
		
		
		tvOptions.setRoot(root);
		tvOptions.setShowRoot(false);

	}
	
	public void tvSelection(Event e) throws IOException {
		TreeItem<String> item = tvOptions.getSelectionModel().getSelectedItem();
		
		if (item.getValue() != null) {
			paneParam.getChildren().clear();
			paneParam.getChildren().add(FXMLLoader.load(getClass().getResource("/main/"+ item.getValue().replaceAll(" ", "") +".fxml")));
		} else {
			paneParam.getChildren().clear();
		}
	}
}
