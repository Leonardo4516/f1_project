package com.proyectof1.dominio;

public class Vehiculo {
    private String marcaEscuderia;
    private int velocidadMaxima;
    private double desgasteNeumaticos;
    private Piloto piloto;

    
    public Vehiculo(String marcaEscuderia, int velocidadMaxima, double desgasteNeumaticos, Piloto piloto) {
        
        setMarcaEscuderia(marcaEscuderia);
        setVelocidadMaxima(velocidadMaxima);
        setDesgasteNeumaticos(desgasteNeumaticos);
        setPiloto(piloto);

    }


    public String getMarcaEscuderia() {
        return marcaEscuderia;
    }


    public final void setMarcaEscuderia(String marcaEscuderia) {

        if (!(marcaEscuderia == null) && !marcaEscuderia.isEmpty()) {

            this.marcaEscuderia = marcaEscuderia;
            
        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }


    public int getVelocidadMaxima() {
        return velocidadMaxima;
    }


    public final void setVelocidadMaxima(int velocidadMaxima) {
        if (velocidadMaxima > 0) {

            this.velocidadMaxima = velocidadMaxima;
            
        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }


    public double getDesgasteNeumaticos() {
        return desgasteNeumaticos;
    }


    public final void setDesgasteNeumaticos(double desgasteNeumaticos) {
        if (desgasteNeumaticos >= 0.0 && desgasteNeumaticos <= 100.0) {

            this.desgasteNeumaticos = desgasteNeumaticos;
            
        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    public Piloto getPiloto(){
        return piloto;
    }

    public final void setPiloto(Piloto piloto){
        if (piloto != null) {

            this.piloto = piloto;

        } else {

            throw new IllegalArgumentException("Argumento inválido, intente de nuevo.");

        }
    }

    @Override
    public String toString() {

        return marcaEscuderia + " | " + velocidadMaxima + " km/h | Piloto: " + piloto.getNombre();

    }

}
