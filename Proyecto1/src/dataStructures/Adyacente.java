package dataStructures;

public class Adyacente {

    Vertice vertice;//cambio en la clase para el manejo de punteros de memoria
    double peso;//el peso es de este tipo para acercarlo a la realidad....
    Adyacente sig;
    boolean dirigido = true;

    public Adyacente(Vertice nombre, double peso) {
        this.vertice = nombre;
        this.peso = peso;
        this.sig = null;
    }

    public Adyacente(Vertice nombre, double peso, boolean dirigido) {
        this.vertice = nombre;
        this.peso = peso;
        this.sig = null;
        this.dirigido = dirigido;
    }

    @Override
    public String toString() {
        return vertice.getNombreVertice() + ": " + (int) peso + "/" + ((dirigido) ? "Dirigido" : "Ponderado");
    }
}