package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.event.ChangeEvent;
import javax.xml.transform.Source;

import org.jetbrains.annotations.TestOnly;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;

import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import dkpro.similarity.algorithms.lexical.ngrams.WordNGramContainmentMeasure;
import dkpro.similarity.algorithms.lexical.ngrams.WordNGramJaccardMeasure;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import info.collide.nlpwikinetgen.builder.*;

public class DataBuilderController implements Initializable {
	
	private DataBuilder db;
	private ObservableList<Node> paramPanes;
	
	@FXML
	private Button btnConfigFile;
	@FXML
	private Button btnOutputFolder;
	@FXML
	private Button btnDataFolder;
	@FXML
	private TextField tfConfigFile;
	@FXML
	private TextField tfOutputFolderDB;
	@FXML
	private TextField tfOutputFolderGB;
	@FXML
	private CheckBox cbWholeWiki;
	@FXML
	private TextField tfCategory;
	@FXML
	private CheckBox cbBuildGraph;
	@FXML
	private CheckBox cbBuildIndex;
	@FXML
	private TreeView<String> tvOptions;
	@FXML
	private StackPane stackParam;
	@FXML
	private VBox WikiMinorFlag;
		@FXML
		private CheckBox cbWikiMinorFlag;
	@FXML
	private VBox CharacterLengthDifference;
		@FXML
		private CheckBox cbCharLengthDiff;
	@FXML
	private VBox WordNGramJaccard;
		@FXML
		private CheckBox cbWordNGramJaccard;
		@FXML
		private TextField tfNJaccard;
		@FXML
		private CheckBox cbWordNGramJaccardLower;
	@FXML
	private VBox WordNGramContainment;
		@FXML
		private CheckBox cbWordNGramContainment;
		@FXML
		private TextField tfNContainment;
	@FXML
	private StackPane stackParamGB;
	@FXML
	private Text tStatus;
	@FXML
	private ProgressBar pbGenerating;
	
	@FXML
	private TreeView<String> tvExFilters;
	
	public void generate(ActionEvent e) throws IOException {
		boolean wholeWiki = cbWholeWiki.isSelected();
		String category = null;
		List<GraphDataComponent> filter = new ArrayList<>();
		
		if (!wholeWiki) {
			category = tfCategory.getText();
		}
		boolean buildGraph = cbBuildGraph.isSelected();
		boolean buildIndex = cbBuildIndex.isSelected();

		try {
			db = new DataBuilder(tfConfigFile.getText(), tfOutputFolderDB.getText(), wholeWiki, category, buildGraph, buildIndex);
			RevisionApi revApi = db.getRevisionAPI();
			
			if (cbWikiMinorFlag.isSelected()) {
				filter.add(new WikiMinorFlag(revApi));
			}
			if (cbCharLengthDiff.isSelected()) {
				filter.add(new CharacterLengthDifference(revApi));
			}
			if (cbWordNGramJaccard.isSelected()) {
				SimilarityCalculator simCal = new SimilarityCalculator(revApi, new WordNGramJaccardMeasure(Integer.parseInt(tfNJaccard.getText()), cbWordNGramJaccardLower.isSelected()));
				simCal.setDescr("WordNGramJaccard_"+Integer.parseInt(tfNJaccard.getText())+"_"+cbWordNGramJaccardLower.isSelected());
				filter.add(simCal);
			}
			if (cbWordNGramContainment.isSelected()) {
				SimilarityCalculator simCal = new SimilarityCalculator(revApi, new WordNGramContainmentMeasure(Integer.parseInt(tfNContainment.getText())));
				simCal.setDescr("WordNGramContainment_"+Integer.parseInt(tfNContainment.getText()));
				filter.add(simCal);
			}
			db.setFilter(filter);
			
			Thread th = new Thread(db);
			th.setDaemon(true);
			th.start();
			pbGenerating.setVisible(true);
			pbGenerating.progressProperty().bind(db.progressProperty());
			tStatus.textProperty().bind(db.messageProperty());
		} catch (WikiApiException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		TreeItem<String> root = new TreeItem<> ("Root");
		
		TreeItem<String> simpleFilter = new TreeItem<> ("Simple Filters");
			TreeItem<String> wikiMinorFlag = new TreeItem<> ("Wiki Minor Flag");
			TreeItem<String> charLengthDiff = new TreeItem<> ("Character Length Difference");

			
		TreeItem<String> similarityFilter = new TreeItem<> ("Similarity Filters");
			TreeItem<String> nGram = new TreeItem<> ("n-gram");
				TreeItem<String> wordNGramJaccard = new TreeItem<> ("Word N-Gram Jaccard");
				TreeItem<String> wordNGramContainment = new TreeItem<> ("Word N-Gram Containment");
	
		root.getChildren().addAll(simpleFilter, similarityFilter);
			simpleFilter.getChildren().addAll(wikiMinorFlag, charLengthDiff);
			similarityFilter.getChildren().addAll(nGram);
				nGram.getChildren().addAll(wordNGramJaccard, wordNGramContainment);
		
		tvOptions.setRoot(root);
		tvOptions.setShowRoot(false);
		
		//TEST
		tfConfigFile.setText("/Users/Tobias/git/nlpwikinetgen/nlpwikinetgen/dbconf.txt");
		tfOutputFolderDB.setText("/Users/Tobias/git/nlpwikinetgen/nlpwikinetgen/data/firstGUIAttempt");
		tfCategory.setText("German_beer_culture");
	}
	
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
		TreeItem<String> root = new TreeItem<> ("Root");
		
		if (dir != null) {
			tfOutputFolderDB.setText(dir.getAbsolutePath());
			tfOutputFolderGB.setText(dir.getAbsolutePath());
			
			for(File file : dir.listFiles((d,name) -> (!name.equals(".DS_Store")))) {
				TreeItem<String> filter = new TreeItem<> (file.getName().split("\\.")[0]);
				root.getChildren().add(filter);
			}
			
			tvExFilters.setRoot(root);
			tvExFilters.setShowRoot(false);
			
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
	
	public void tvSelectionDB(Event e) throws IOException {
		paramPanes = stackParam.getChildren();
		TreeItem<String> item = tvOptions.getSelectionModel().getSelectedItem();
		String fxmlTitle = item.getValue().replaceAll(" ", "").replaceAll("-", "");
		Scene scene = stackParam.getScene();
		VBox boxNew = (VBox) scene.lookup("#"+fxmlTitle);
		VBox boxOld = (VBox) paramPanes.get(paramPanes.size()-1);
		boxOld.setVisible(false);
		boxNew.setVisible(true);
		boxNew.toFront();
	}
	public void tvSelectionGB(Event e) throws IOException {
		paramPanes = stackParamGB.getChildren();
		TreeItem<String> item = tvExFilters.getSelectionModel().getSelectedItem();
		String fxmlTitle = item.getValue().split("_")[0];
		Scene scene = stackParamGB.getScene();
		VBox boxNew = (VBox) scene.lookup("#"+fxmlTitle+"GB");
		VBox boxOld = (VBox) paramPanes.get(paramPanes.size()-1);
		boxOld.setVisible(false);
		boxNew.setVisible(true);
		boxNew.toFront();
	}
}