package controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFormController{

    public MenuBar menuBar;
    public TextArea txtArea;
    public AnchorPane anchorPane;
    public Button btnAddNew;
    public Button btnSave;
    public Button btnCut;
    public Button btnCopy;
    public Button btnPaste;
    public Label lblWordCount;
    public TextField txtFind;
    public TextField txtReplace;
    public Button btnReplace;
    public ToggleButton btnRegex;
    public ToggleButton btnCaseSensitive;
    public Button btnUp;
    public Button btnDown;
    public Label lblFindCount;



    Path currentPath;
    boolean txtChanged=false;
    Matcher matcher;
    ArrayList<Integer> referBack = new ArrayList();
    ArrayList<Integer> referForward = new ArrayList();


    public void initialize(){

        MenuItem aNew = new MenuItem("New");
        MenuItem open = new MenuItem("open");
        MenuItem save = new MenuItem("Save As");
        MenuItem print = new MenuItem("Print");
        MenuItem exit = new MenuItem("Exit");

        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        MenuItem selectAll = new MenuItem("Select All");

        menuBar.getMenus().get(0).getItems().add(aNew);
        menuBar.getMenus().get(0).getItems().add(open);
        menuBar.getMenus().get(0).getItems().add(save);
        menuBar.getMenus().get(0).getItems().add(print);
        menuBar.getMenus().get(0).getItems().add(exit);
        menuBar.getMenus().get(0).getItems().remove(0);

        menuBar.getMenus().get(1).getItems().add(cut);
        menuBar.getMenus().get(1).getItems().add(copy);
        menuBar.getMenus().get(1).getItems().add(paste);
        menuBar.getMenus().get(1).getItems().add(selectAll);
        menuBar.getMenus().get(1).getItems().remove(0);

        aNew.setOnAction(event ->{

            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/MainForm.fxml"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.setX(800.0);
            stage.setY(500.0);
            stage.show();


        });

        open.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open a file");
            fileChooser.getExtensionFilters().
                    add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File file = fileChooser.showOpenDialog(null);
            try {
                readData(file);
            } catch (IOException e) {
                e.printStackTrace();
            }


        });
        save.setOnAction(event -> {
            FileChooser fileChooser=new FileChooser();
            fileChooser.setTitle("Select a Destination");
            File file = fileChooser.showSaveDialog(null);
            try {
                saveFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        exit.setOnAction(event -> {
            System.exit(0);
        });

        selectAll.setOnAction(event -> {
            txtArea.selectAll();
        });

        cut.setOnAction(event -> {
            btnCut.fire();
        });

        copy.setOnAction(event -> {
            btnCopy.fire();
        });

        paste.setOnAction(event -> {
            btnPaste.fire();
        });

        MenuItem about=menuBar.getMenus().get(2).getItems().get(0);

        about.setOnAction(event -> {
            Stage stage = new Stage();
            try {
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/AboutForm.fxml"))));
                stage.setTitle("About");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(txtArea.getScene().getWindow());
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }


        });

        txtArea.textProperty().addListener((observable, oldValue, newValue) -> {
            Stage stage =(Stage) txtArea.getScene().getWindow();
            if (stage.getTitle().charAt(0)!='*'){
                stage.setTitle("*"+stage.getTitle());
            }
            setWordCount();
            txtChanged=true;
            findWords();

        });

        txtFind.textProperty().addListener((observable, oldValue, newValue) -> {
            txtChanged=true;
            findWords();

        });

        btnDown.setOnAction(event -> {
            findWords();
        });

    }


    private void foundedWords() {
        int count =0;
        if(matcher!=null){
            while(matcher.find()){
                count++;
            }
            matcher.reset();
            lblFindCount.setText(String.valueOf(count));
        }
    }

    private void findWords() {
        if (!txtFind.getText().isEmpty()){
            if (txtChanged){
                int flag=0;
                if (!btnCaseSensitive.isSelected()){
                    flag=flag | Pattern.CASE_INSENSITIVE;
                }
                if (!btnRegex.isSelected()){
                    flag=flag | Pattern.LITERAL;
                }
                matcher = Pattern.compile(txtFind.getText(),flag).matcher(txtArea.getText());
                foundedWords();
                txtChanged=false;
                System.gc();
            }
            if (referForward.size()>2){
                txtArea.selectRange(referForward.get(referForward.size()-4),referForward.get(referForward.size()-3));
                referForward.remove(referForward.size()-4);
                referForward.remove(referForward.size()-3);

            }else if(matcher.find()){
                referForward.clear();
                txtArea.selectRange(matcher.start(),matcher.end());
                referBack.add(matcher.start());
                referBack.add(matcher.end());
            }else {
                matcher.reset();
            }
        }

    }

    private void setWordCount() {
        if(txtArea.getText().isEmpty()){
            lblWordCount.setText(String.valueOf(0));
            return;
        }
        int count=0;
        Matcher matcher = Pattern.compile("\\S+").matcher(txtArea.getText());
        while(matcher.find()){
            count++;
        }
        lblWordCount.setText(String.valueOf(count));
    }

    private void saveFile(File file) throws IOException {
        Path path = Paths.get(String.valueOf(file));
        OutputStream outputStream = Files.newOutputStream(path);
        String newString=txtArea.getText();
        outputStream.write(newString.getBytes());
        Stage stage =(Stage)txtArea.getScene().getWindow();
        stage.setTitle(file.getName());
        currentPath=path;
    }

    private void readData(File file) throws IOException {
        if(file!=null){
            Path path = Paths.get(String.valueOf(file));
            currentPath=path;
            InputStream inputStream = Files.newInputStream(path);
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            String fileContent=new String(fileBytes);
            txtArea.setText(fileContent);

            Window window = txtArea.getScene().getWindow();
            Stage stage = (Stage) window;
            stage.setTitle(file.getName());
        }


    }

    public void btnPasteOnAction(ActionEvent actionEvent) {
        Clipboard pasteClip=Clipboard.getSystemClipboard();
        if(txtArea.getSelectedText().isEmpty()){
            int caretPosition = txtArea.getCaretPosition();
            txtArea.insertText(caretPosition,pasteClip.getString());
        } else{
            txtArea.setText(txtArea.getText().replace(txtArea.getSelectedText(),pasteClip.getString()));
        }

    }

    public void btnCopyOnAction(ActionEvent actionEvent) {
        if(txtArea.getSelectedText()!=null){
            Clipboard systemClipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(txtArea.getSelectedText());
            systemClipboard.setContent(clipboardContent);
            txtArea.deselect();
        }
    }

    public void btnCutOnAction(ActionEvent actionEvent) {
        if(txtArea.getSelectedText()!=null){
            Clipboard systemClipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(txtArea.getSelectedText());
            systemClipboard.setContent(clipboardContent);
            txtArea.setText(txtArea.getText().replaceAll(clipboardContent.getString(),""));
            txtArea.deselect();
        }
    }

    public void btnSaveOnAction(ActionEvent actionEvent) throws IOException {
        if (((Stage)txtArea.getScene().getWindow()).getTitle().equals("*Untitled")){
            menuBar.getMenus().get(0).getItems().get(2).fire();
        }else {
            try{
                OutputStream outputStream = Files.newOutputStream(currentPath);
                outputStream.write(txtArea.getText().getBytes());
                Stage stage =(Stage)txtArea.getScene().getWindow();
                stage.setTitle(currentPath.getFileName().toString());
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void btnAddNewOnAction(ActionEvent actionEvent) {
        txtArea.clear();
    }

    public void btnCaseSensitiveOnAction(ActionEvent actionEvent) {
        txtChanged=true;
        findWords();
    }

    public void btnRegexOnAction(ActionEvent actionEvent) {
        txtChanged=true;
        findWords();
    }

    public void btnReplaceOnAction(ActionEvent actionEvent) {
        if (!txtFind.getText().isEmpty()){
            txtArea.setText(txtArea.getText().replaceAll(txtFind.getText(),txtReplace.getText()));
        }

    }

    public void btnUpOnAction(ActionEvent actionEvent) {
        if (referForward.size()==0){
            referForward.add(referBack.get(referBack.size()-2));
            referForward.add(referBack.get(referBack.size()-1));
        }

        txtArea.selectRange(referBack.get((referBack.size())-4),referBack.get((referBack.size()-3)));
        referForward.add(referBack.get((referBack.size()-4)));
        referForward.add(referBack.get((referBack.size()-3)));

        referBack.remove(referBack.size()-4);
        referBack.remove(referBack.size()-3);


    }
}
