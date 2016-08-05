package uk.ac.susx.tag.neighbours;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import uk.ac.susx.tag.neighbours.exception.InvalidFileFormatException;

import java.io.*;
import java.util.*;

public class NeighbourProcessor {

    private Map<String, List<Neighbour>> neighbourMap;

    private Integer neighbourCount;

    public NeighbourProcessor() {
        neighbourMap = new HashMap<String, List<Neighbour>>();
    }

    //TODO: how to deal with exceptions?
    public void buildNeighbourMap(String pathToFile) throws IOException, InvalidFileFormatException {
       buildNeighbourMap(new File(pathToFile));
    }

    public void buildNeighbourMap(String pathToFile, int neighbourCount)  {
        this.neighbourCount = neighbourCount;
        buildNeighbourMap(new File(pathToFile));
    }

    public Map<String, List<Neighbour>> buildNeighbourMap(File file, int neighbourCount) {
        this.neighbourCount = neighbourCount;
        return buildNeighbourMap(file);

    }

    public Map<String, List<Neighbour>> buildNeighbourMap(File file)  {
        List<String> fileContents = null;
        try {
            fileContents = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO: needs more work. current implementation fails valid twitter file
        /*
        if (!FileFormatValidator.isFileInAValidFormat(fileContents) ) {
            throw new InvalidFileFormatException("Supplied file cannot be parsed due to invalid format. File name: " + file.getName());
        }
        */

        for (String line : fileContents) {
            populateNeighbourMap(line);
        }
        return neighbourMap;
    }

    private void populateNeighbourMap(String line) {
        String[] lineItems = line.split("\t");
        //lineItems = removeTags(lineItems);
        String term = lineItems[0];
        lineItems = Arrays.copyOfRange(lineItems, 1, lineItems.length);
        List<Neighbour> neighbours = new LinkedList<>();
        int indexLength;
        if (neighbourCount == null || neighbourCount > lineItems.length/2) { // 1 neighbour is made of 2 line items
            indexLength = lineItems.length;
        } else {
            indexLength = neighbourCount*2; //because of the index & the fact that 1 neighbour is a pair of items
        }

        for (int i = 0; i<indexLength; i++) {
            neighbours.add(new Neighbour(lineItems[i], Double.valueOf(lineItems[i+1])));
            i++;
        }
        neighbourMap.put(term, neighbours);
    }

    private String[] removeTags(String[] lineItems) {
        int indexLength;
        if (neighbourCount == null || neighbourCount > lineItems.length/2) { // 1 neighbour is made of 2 line items
            indexLength = lineItems.length;
        } else {
            indexLength = neighbourCount*2+1; //because of the index & the fact that 1 neighbour is a pair of items
        }

        for(int i =0; i<indexLength; i++) {
            lineItems[i] = StringUtils.substringBeforeLast(lineItems[i], "/");
        }
        return Arrays.copyOfRange(lineItems, 0, indexLength);
    }

    public Map<String, List<Neighbour>> getNeighbourMap() {
        return neighbourMap;
    }

    public void setNeighbourCount(Integer neighbourCount) {
        this.neighbourCount = neighbourCount;
    }
}
