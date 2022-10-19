package pl.sportal.rest;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import org.json.simple.JSONObject;


public class Odpowiedz {

    boolean blad = false;
    boolean uwaga = false;
    String pytanie = null;    
    String komunikat = null;    
    JSONObject dane = null;

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public Odpowiedz() {
    }

    //--------------------------------------------------------------------------
    // Metody prywatne
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    public boolean isBlad() {
        return blad;
    }

    public Odpowiedz setBlad(boolean blad) {
        this.blad = blad;
        return this;
    }

    public boolean isUwaga() {
        return uwaga;
    }

    public Odpowiedz setUwaga(boolean uwaga) {
        this.uwaga = uwaga;
        return this;
    }

    public String getPytanie() {
        return pytanie;
    }

    public Odpowiedz setPytanie(String pytanie) {
        this.pytanie = pytanie;
        return this;
    }

    public String getKomunikat() {
        return komunikat;
    }

    public Odpowiedz setKomunikat(String komunikat) {
        this.komunikat = komunikat;
        return this;
    }

    public JSONObject getDane() {
        return dane;
    }

    public Odpowiedz setDane(JSONObject dane) {
        this.dane = dane;
        return this;
    }        
}
