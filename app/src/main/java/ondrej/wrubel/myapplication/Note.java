package ondrej.wrubel.myapplication;

public class Note {
    private int id;
    private String title;
    private String description;
    private long createdAt;      // čas vytvoření (timestamp)
    private double latitude;     // zeměpisná šířka
    private double longitude;    // zeměpisná délka

    public Note(int id, String title, String description, long createdAt, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Konstruktor pro novou poznámku – id bude přiděleno v DB
    public Note(String title, String description, long createdAt, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Gettery a settery
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getCreatedAt() { return createdAt; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    // Pole createdAt, latitude a longitude nastavujeme pouze při vytvoření poznámky – setters obvykle nepoužívat při úpravách textu
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
