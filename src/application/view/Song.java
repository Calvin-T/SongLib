package application.view;

public class Song {

	public String name;
	public String artist;
	public String album;
	public int year;
	
	public Song(String name,String artist, String album, int year) {
		this.name = name;
		this.artist = artist;
		this.album = album;
		this.year = year;
	}
	
	public Song(String name,String artist, int year) {
		this(name,artist,"",year);
	}
	
	public Song(String name,String artist, String album) {
		this(name,artist,album,0);
	}
	
	public Song(String name,String artist) {
		this(name,artist,"",0);
	}
	
}
