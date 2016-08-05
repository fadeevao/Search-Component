package uk.ac.susx.tag.neighbours;


public class Neighbour {

    private Double similarity;
    private String term;

    public Neighbour(String term, Double similarity) {
        this.term = term;
        this.similarity = similarity;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public String getTerm() {
        return term;
    }


}
