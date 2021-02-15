package sample;

import javafx.beans.property.SimpleStringProperty;

public class TableClass {

    private SimpleStringProperty TCArticle;
    private  SimpleStringProperty TCmodel;
    private SimpleStringProperty TCresultat;
    private SimpleStringProperty TCdescription;
    private  SimpleStringProperty TCdate;
    private  SimpleStringProperty TCheure;

    public TableClass(String TCArticle, String TCmodel, String TCresultat, String TCdescription, String TCdate,String TCheure) {
        this.TCArticle = new SimpleStringProperty (TCArticle);
        this.TCmodel = new SimpleStringProperty(TCmodel);
        this.TCresultat = new SimpleStringProperty(TCresultat);
        this.TCdescription = new SimpleStringProperty(TCdescription);
        this.TCdate = new SimpleStringProperty(TCdate);
        this.TCheure = new SimpleStringProperty(TCheure);
    }

    public String getTCArticle() {
        return TCArticle.get();
    }

    public void setTCArticle(String TCArticle) {
        this.TCArticle = new SimpleStringProperty(TCArticle);
    }

    public String getTCmodel() {
        return TCmodel.get();
    }

    public void setTCmodel(String TCmodel) {
        this.TCmodel = new SimpleStringProperty(TCmodel);
    }

    public String getTCresultat() {
        return TCresultat.get();
    }

    public void setTCresultat(String TCresultat) {
        this.TCresultat = new SimpleStringProperty(TCresultat);
    }

    public String getTCdescription() {
        return TCdescription.get();
    }

    public void setTCdescription(String TCdescription) {
        this.TCdescription = new SimpleStringProperty(TCdescription);
    }

    public String getTCdate() {
        return TCdate.get();
    }

    public void setTCdate(String TCdate) {
        this.TCdate = new SimpleStringProperty(TCdate);
    }


    public String getTCheure() {
        return TCheure.get();
    }

    public void setTCheure(String TCheure) {
        this.TCheure = new SimpleStringProperty(TCheure);
    }
}
