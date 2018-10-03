package application.view;

//Created by: Calvin Tang and Joseph Morales
//Includes GSon


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.gson.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class MainController {

	@FXML Button addBtn;
	@FXML Button deleteBtn;
	@FXML Button editBtn;
	@FXML Button cancelBtn;
	@FXML Button doneBtn;
	
	@FXML TextField nameField;
	@FXML TextField artistField;
	@FXML TextField albumField;
	@FXML TextField yearField;
	
	@FXML ListView<Song> songList;
	
	@FXML Label detailTitle;
	@FXML Label nameLabel;
	@FXML Label artistLabel;
	@FXML Label albumLabel;
	@FXML Label yearLabel;
	@FXML Label tips;
	
	
	private ObservableList<Song> songs;
	
	String tempName;
	String tempArtist;
	String tempAlbum;
	String tempYear;
	
	boolean isEditing = false;
	boolean isAdding = false;
	
	Gson gson;
	JsonParser parser;
	JsonElement root;
	JsonObject obj;
	JsonArray list;
	
	public void start(Stage mainStage) throws JsonIOException, JsonSyntaxException, FileNotFoundException {          
		
		
		songs = FXCollections.observableArrayList();

		gson = new Gson();
		parser = new JsonParser();
		root = parser.parse(new FileReader("src/songs.json"));
		obj = root.getAsJsonObject();
		list = obj.getAsJsonArray("songs");
		for(int i = 0; i < list.size();i++) {
			songs.add(gson.fromJson(list.get(i), Song.class));
		}

		songList.setItems(songs); 		
		
		if(songs.size() == 0) {
			editBtn.setDisable(true);
			deleteBtn.setDisable(true);
			tips.setText("Tip: Press the add button to add a new song.");
		}
		sortSongs();
		
		updateListInfo();
		
		// select the first item
		songList.getSelectionModel().select(0);
	
	 
	    showItem(mainStage);
	    songList
	       .getSelectionModel()
	       .selectedIndexProperty()
	       .addListener(
	          (obs, oldVal, newVal) -> 
	              showItem(mainStage));
	      
	}
	
	private void showItem(Stage mainStage) {  
		Song content = songList.getSelectionModel().getSelectedItem();
		if(content != null) {
			nameField.setText(content.name);
			artistField.setText(content.artist);
			albumField.setText(content.album);
			if(content.year == 0) {
				yearField.setText("");
			} else {
				yearField.setText(content.year + "");
			}
			nameLabel.setText(content.name);
			artistLabel.setText(content.artist);
			albumLabel.setText(content.album);
			if(content.year == 0) {
				yearLabel.setText("");
			} else {
				yearLabel.setText(content.year + "");
			}
			
		} 
	}
	
	
	public void add(ActionEvent e) throws FileNotFoundException, IOException, ParseException {
		Button b = (Button)e.getSource();
		if(b == addBtn) {
			isAdding = true;
			onlyCancelAndDoneAllowed();
			setDetailToEmpty();
			detailTitle.setText("Add information to the fields");
			tips.setText("");
		} else {
			System.out.println("Wrong");
		}
	}
	
	public void delete(ActionEvent e) {
		Button b = (Button)e.getSource();
		if(b == deleteBtn) {
			if(songs.size() < 1) {
				showAlert("","No songs to delete.");
			} else {
				
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Delete Confirmation");
				alert.setContentText("Are you sure you want to delete this song?");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					
					Song currentSong = songList.getSelectionModel().getSelectedItem();
					
					int indexOfSong = getIndexJson(currentSong.name,currentSong.artist);
					
					songs.remove(currentSong);
					list.remove(indexOfSong);
					writeToJson();
				
					if(songs.size() > 0) {
						setDetailToCurrentSong();
					} else {
						setDetailToEmpty();

					}
				} 
				
				if(songs.size() == 0) {
					editBtn.setDisable(true);
					deleteBtn.setDisable(true);
					tips.setText("Tip: Press the add button to add a new song.");
				}
			}
		} else {
			System.out.println("wrong");
		}
	}
	
	public void edit(ActionEvent e) {
		Button b = (Button)e.getSource();
		if(b == editBtn) {
			if(songs.size() < 1) {
				showAlert("","No song selected to edit.");
			} else {
				isEditing = true;
				onlyCancelAndDoneAllowed();
				tempName = nameField.getText();
				tempArtist = artistField.getText();
				tempAlbum = albumField.getText();
				tempYear = yearField.getText();
				detailTitle.setText("Edit the fields");
			}
		} else {
			System.out.println("wrong");
		}
	}
	
	public void cancel(ActionEvent e) {
		Button b = (Button)e.getSource();		
		if(b == cancelBtn) {
			if(isEditing) {
				if(songs.size() > 0) {
					nameField.setText(tempName);
					artistField.setText(tempArtist);
					albumField.setText(tempAlbum);
					yearField.setText(tempYear);
				} 
				isEditing = false;
			}
			if(isAdding) {
				if(songs.size() > 0) {
					setDetailToCurrentSong();
				} else {
					setDetailToEmpty();
				}

				if(songs.size() == 0) {
					editBtn.setDisable(true);
					deleteBtn.setDisable(true);
					tips.setText("Tip: Press the add button to add a new song.");
				}
				
				isAdding = false;
			}
			detailTitle.setText("Song Info");
			onlyCancelAndDoneUnallowed();
		} else {
			System.out.println("wrong");
		}
	}
	
	public void done(ActionEvent e) throws FileNotFoundException, IOException, ParseException {
		Button b = (Button)e.getSource();
		if(b == doneBtn) {
			
			String currentName = nameField.getText();
			String currentArtist = artistField.getText();
			String currentAlbum = albumField.getText();
			String currentYear = yearField.getText();
			
			if(isEditing) {
				
				if(currentName.equals("") || currentArtist.equals("")) {
					showAlert("Missing info","Both the name of the song and the artist is required! Album and year are optional.");
				} else if(!isInteger(currentYear) && !currentYear.equals("")) {
					showAlert("Incorrect year","Year field must be a number");
				} else if (!noDuplicates(currentName,currentArtist,songList.getSelectionModel().getSelectedItem(),true)) {
					showAlert("Duplicate","There already exists a song with this name and artist!");
				} else {
					Song currentSong = songList.getSelectionModel().getSelectedItem();
					currentSong.name = currentName;
					currentSong.artist = currentArtist;
					currentSong.album = currentAlbum;
					if(currentYear.equals("")) {
						currentSong.year = 0;
					} else {
						currentSong.year = Integer.parseInt(currentYear);
					}
				
					int index = getIndexJson(tempName,tempArtist);
					JsonObject jsonSong = (JsonObject) list.get(index);
					jsonSong.addProperty("name", currentName);
					jsonSong.addProperty("artist", currentArtist);
					jsonSong.addProperty("album", currentAlbum);
					if(currentYear.equals("")) {
						jsonSong.addProperty("year", 0);
					} else {
						jsonSong.addProperty("year", Integer.parseInt(currentYear));
					}
				
					writeToJson();
					isEditing = false;
					updateListInfo();
					detailTitle.setText("Song Info");
					sortSongs();
					onlyCancelAndDoneUnallowed();
				}
			}
			if(isAdding) {
			
				if(currentName.equals("") || currentArtist.equals("")) {
					showAlert("Missing info","Both the name of the song and the artist is required! Album and year are optional.");
				} else if(!isInteger(currentYear) && !currentYear.equals("")) {
					showAlert("Incorrect year","Year field must be a number");
				} else if (!noDuplicates(currentName,currentArtist,null,false)) {
					System.out.println("here boy");
					showAlert("Duplicate","There already exists a song with this name and artist!");
				}else {
					Song newSong = new Song(currentName,currentArtist,currentAlbum);
					if(currentYear.equals("")) {
						newSong.year = 0;
					} else {
						newSong.year = Integer.parseInt(currentYear);
					}
					songs.add(newSong);
					
					JsonObject jsonSong = new JsonObject();
					jsonSong.addProperty("name", currentName);
					jsonSong.addProperty("artist", currentArtist);
					jsonSong.addProperty("album", currentAlbum);
					if(currentYear.equals("")) {
						jsonSong.addProperty("year", 0);
					} else {
						jsonSong.addProperty("year", Integer.parseInt(currentYear));
					}
					
					list.add(jsonSong);
					
					writeToJson();
					isAdding = false;
					songList.getSelectionModel().select(newSong);
					updateListInfo();
					detailTitle.setText("Song Info");
					sortSongs();
					onlyCancelAndDoneUnallowed();
					
					if(songs.size() > 0) {
						editBtn.setDisable(false);
						deleteBtn.setDisable(false);
						tips.setText("");
					}
					
				}
				
			}
		} else {
			System.out.println("wrong");
		}
	}
	
	public int getIndexJson(String name, String artist) {
		JsonObject tempObj;
		for(int i = 0;i<list.size();i++) {
			tempObj = (JsonObject) list.get(i);
			String objName = tempObj.get("name").getAsString();
			String objArtist = tempObj.get("artist").getAsString();
			if(objName.equals(name) && objArtist.equals(artist)) {
				return i;
			}
		}
		return -1;
	}
	
	public void sortSongs() {
		Collections.sort(songs, new Comparator<Song>() {
			@Override
			public int compare(Song song1, Song song2) {
				int result = song1.name.compareToIgnoreCase(song2.name);
				if(result == 0) {
					result = song1.artist.compareToIgnoreCase(song2.artist);
				}
				return result;
			}
		});
		
	}
	
	public boolean noDuplicates(String name,String artist,Song currentSong,boolean editing) {
		//true for no duplicate song name and artist
		//false for found duplicate song name and artist
		
		Song tempSong = null;
		if(editing) {
			for(int i = 0;i<songs.size();i++) {
				tempSong = songs.get(i);
				if(tempSong.name.equals(name) && tempSong.artist.equals(artist) && tempSong != currentSong) {
					return false;
				}
			}
		} else {
			for(int i = 0;i<songs.size();i++) {
				tempSong = songs.get(i);
				if(tempSong.name.equals(name) && tempSong.artist.equals(artist)) {
					return false;
				}
			}
		}
		
		return true;
		
	}
	
	public void showAlert(String title, String message) {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
	
	public void writeToJson() {
		try(FileWriter writer = new FileWriter("src/songs.json")) {
			Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
            gsonBuilder.toJson(root, writer);
            writer.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
	}
	
	public void updateListInfo() {
		songList.setCellFactory(param -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
            	super.updateItem(song, empty);
            	if (song != null) {
            	   setText(song.name + " | " + song.artist);
            	} else {
            	   setText("");   
            	}
            }
        });
	}
	
	public void setDetailToEmpty() {
		nameField.setText("");
		artistField.setText("");
		albumField.setText("");
		yearField.setText("");
		nameLabel.setText("");
		artistLabel.setText("");
		albumLabel.setText("");
		yearLabel.setText("");
	}
	
	public void setDetailToCurrentSong() {
		Song currentSong = songList.getSelectionModel().getSelectedItem();
		nameField.setText(currentSong.name);
		artistField.setText(currentSong.artist);
		albumField.setText(currentSong.album);
		if(currentSong.year == 0) {
			yearField.setText("");
			yearLabel.setText("");
		} else {
			yearField.setText(currentSong.year + "");
			yearLabel.setText(currentSong.year + "");
		}
		nameLabel.setText(currentSong.name);
		artistLabel.setText(currentSong.artist);
		albumLabel.setText(currentSong.album);

	}
	
	public static boolean isInteger(String str) {  
	  try  
	  {  
	    int i = Integer.parseInt(str);
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public void onlyCancelAndDoneAllowed() {
		nameField.setDisable(false);
		artistField.setDisable(false);
		albumField.setDisable(false);
		yearField.setDisable(false);
		nameLabel.setOpacity(0);
		artistLabel.setOpacity(0);
		albumLabel.setOpacity(0);
		yearLabel.setOpacity(0);
		nameField.setOpacity(1);
		artistField.setOpacity(1);
		albumField.setOpacity(1);
		yearField.setOpacity(1);
		doneBtn.setDisable(false);
		cancelBtn.setDisable(false);
		addBtn.setDisable(true);
		deleteBtn.setDisable(true);
		editBtn.setDisable(true);
		songList.setDisable(true);	
	}
	
	public void onlyCancelAndDoneUnallowed() {
		nameField.setDisable(true);
		artistField.setDisable(true);
		albumField.setDisable(true);
		yearField.setDisable(true);
		nameLabel.setOpacity(1);
		artistLabel.setOpacity(1);
		albumLabel.setOpacity(1);
		yearLabel.setOpacity(1);
		nameField.setOpacity(0);
		artistField.setOpacity(0);
		albumField.setOpacity(0);
		yearField.setOpacity(0);
		doneBtn.setDisable(true);
		cancelBtn.setDisable(true);
		addBtn.setDisable(false);
		deleteBtn.setDisable(false);
		editBtn.setDisable(false);
		songList.setDisable(false);	
	}
	
}
