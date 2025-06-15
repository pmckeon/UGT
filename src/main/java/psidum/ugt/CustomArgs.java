package psidum.ugt;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;

public class CustomArgs {
    @Parameter
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"-tiledfile", "-t"}, description = "Tiled document to parse")
    private String batchTiled;

    @Parameter(names = {"-imagefile", "-i"}, description = "Image file to parse")
    private String batchImage;

    @Parameter(names = {"-destination", "d"}, description = "Destination for output")
    private String destination;

    @Parameter(names = {"-name", "-n}"}, description = "Name for export files")
    private String name;

    public String getBatchTiled() {
        return this.batchTiled;
    }

    public String getBatchImage() {
        return this.batchImage;
    }

    public String getDestination() {
        return this.destination;
    }

    public String getName() {
        return this.name;
    }
}
