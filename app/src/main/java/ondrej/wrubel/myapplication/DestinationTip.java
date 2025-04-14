package ondrej.wrubel.myapplication;

public class DestinationTip {
    private final String name;
    private final String description;

    public DestinationTip(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Gettery a settery
    public String getName() { return name; }
    public String getDescription() { return description; }
}
