package Listas;

public class ListaNodos {

    private ListaNodos sig;//lista doblemente enlazada
    private ListaNodos ant;
    private ValoresNodos nodeValue = new ValoresNodos();

    public ListaNodos(Object obj, Object objrelacion) {
        this.nodeValue.setObj(obj);
        this.nodeValue.setObjrelacion(objrelacion);
    }

    public ListaNodos(Object obj, double rec) {
        this.nodeValue.setObj(obj);
        this.nodeValue.setRec(rec);
    }

    public ListaNodos(Object obj, Object obj2, double rec, boolean estado) {
        this.nodeValue.setObj(obj);
        this.nodeValue.setObjrelacion(obj2);
        this.nodeValue.setRec(rec);
        this.nodeValue.setEstado(estado);
    }

    public ListaNodos getSig() {
        return sig;
    }

    public void setSig(ListaNodos sig) {
        this.sig = sig;
    }

    public ListaNodos getAnt() {
        return ant;
    }

    public void setAnt(ListaNodos ant) {
        this.ant = ant;
    }

    public ValoresNodos getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(ValoresNodos nodeValue) {
        this.nodeValue = nodeValue;
    }

}
