/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Listas;

public class Lista {
    //Esta clase es para manejar conjunto de controles

    ListaNodos ptr;
    ListaNodos fin;
    private int tamano = 0;

    public boolean isVacia() {
        return ptr == null;
    }

    public Lista() {
    }

    public boolean eliminar(Object obj) {
        if (ptr != null) {
            ListaNodos nl = ptr;
            while (nl != null) {
                if (nl.getNodeValue().getObj() == obj) {
                    if (ptr.getSig() == null) {
                        ptr = null;
                        fin = null;
                    } else if (ptr.getSig() != null) {
                        ptr = ptr.getSig();
                        ptr.setAnt(null);
                    } else if (nl == fin) {
                        fin.getAnt().setSig(null);
                        fin = fin.getAnt();
                    } else {
                        nl.getAnt().setSig(nl.getSig());
                        nl.getSig().setAnt(nl.getAnt());
                    }
                    tamano--;
                    return true;
                }
                nl = nl.getSig();
            }
        }
        return false;
    }

    public ListaNodos buscar(Object obj) {
        if (obj == null) {
            return null;
        }

        if (ptr != null) {
            ListaNodos act = ptr;
            while (act != null) {
                if (act.getNodeValue().getObj() == obj) {
                    return act;
                }
                act = act.getSig();
            }
        }
        return null;
    }

    public ListaNodos buscarbyRel(Object obj) {
        if (obj == null) {
            return null;
        }

        if (ptr != null) {
            ListaNodos act = ptr;
            while (act != null) {
                if (act.getNodeValue().getObjrelacion() == obj) {
                    return act;
                }
                act = act.getSig();
            }
        }
        return null;
    }

    public boolean eliTodosContenganRelacion(Object obj) {
        if (ptr != null) {
            ListaNodos nl = getPtr();
            while (nl != null) {
                if (nl.getNodeValue().getObjrelacion() == obj) {
                    if (ptr.getSig() == null) {
                        ptr = null;
                        fin = null;
                        nl = null;
                        continue;
                    } else if (ptr.getSig() != null) {
                        ptr = ptr.getSig();
                        ptr.setAnt(null);
                        nl = ptr;
                        continue;
                    } else if (nl == fin) {
                        fin.getAnt().setSig(null);
                        fin = fin.getAnt();
                    } else {
                        nl.getAnt().setSig(nl.getSig());
                        nl.getSig().setAnt(nl.getAnt());
                    }
                    tamano--;
                }
                nl = nl.getSig();
            }
        }
        return false;
    }

    public boolean contiene(Object obj) {
        return !(null == buscar(obj));
    }

    public boolean contieneRelacion(Object obj) {
        return !(null == buscarbyRel(obj));
    }

    public boolean contieneGrupo(Object obj, Object objRel) {
        ListaNodos n = buscar(obj);
        if (n == null) {
            return false;
        }
        if (n.getNodeValue().getObjrelacion() == objRel) {
            return true;
        } else {
            return false;
        }
    }

    public boolean estaObjeto1AntesObjeto2(Object obj, Object obj2) {
        ListaNodos n = buscar(obj);
        if (n != null) {
            if (n.getSig() != null) {
                if (n.getSig().getNodeValue().getObj() == obj2) {
                    return true;
                }
            }
        }
        return false;
    }

    public ListaNodos extraerPrimero() {
        if (ptr != null) {
            BSNodeList objeto = ptr;
            ptr = ptr.getSig();
            if (ptr == null) {
                fin = null;
            }
            tamano--;
            return objeto;
        }
        return null;
    }

    public void insertar(Object obj, Object objrelacion) {
        ListaNodos nl = new ListaNodos(obj, objrelacion);
        if (ptr == null) {
            ptr = nl;
            fin = nl;
        } else {
            fin.setSig(nl);
            nl.setAnt(fin);
            fin = fin.getSig();
        }
        tamano++;
    }

    public void insertar(Object obj, int rec) {
        ListaNodos nl = new ListaNodos(obj, rec);
        if (ptr == null) {
            ptr = nl;
            fin = nl;
        } else {
            fin.setSig(nl);
            nl.setAnt(fin);
            fin = fin.getSig();
        }
        tamano++;
    }

    public void insertar(Object obj) {
        insertar(obj, 1);
    }

    public void insertarSinRepetir(Object obj) {
        insertarSinRepetir(obj, 1);
    }

    public boolean insertarSinRepetir(Object obj, int rec) {
        ListaNodos b = buscar(obj);
        if (b == null) {//si no existe el objeto
            insertar(obj, rec);
            return true;
        } else {
            return false;
        }
    }

    public void insertarPorOrdenRec(Object obj, Object obj2, double rec, boolean estado) {
        //inserta objetos ordenados por una variable de recorrido
        if (obj == null && obj2 == null) {
            return;
        }
        ListaNodos nuevo = new ListaNodos(obj, obj2, rec, estado);
        if (ptr == null) {
            ptr = nuevo;
            fin = nuevo;
        } else {
            ListaNodos tcab = getPtr();
            while (tcab != null) {
                if (nuevo.getNodeValue().getRec() <= tcab.getNodeValue().getRec()) {//si el nodo es menor que el del recorrido
                    if (tcab == ptr) {
                        nuevo.setSig(ptr);
                        ptr.setAnt(nuevo);
                        ptr = nuevo;
                        break;
                    } else {
                        if (tcab.getAnt().getNodeValue().getRec() <= nuevo.getNodeValue().getRec()) {
                            tcab.getAnt().setSig(nuevo);
                            nuevo.setAnt(tcab.getAnt());
                            nuevo.setSig(tcab);
                            tcab.setAnt(nuevo);
                            break;
                        }
                    }
                }
                tcab = tcab.getSig();
            }
            if (tcab == null) {
                fin.setSig(nuevo);
                nuevo.setAnt(fin);
                fin = nuevo;
            }
        }
        tamano++;
    }

    public ListaNodos getPtr() {
        return ptr;
    }

    public void setPtr(ListaNodos nuevoPtr) {
        if (nuevoPtr != null) {
            ptr = nuevoPtr;
            nuevoPtr.setAnt(null);
            ListaNodos rec = ptr;
            tamano = 0;
            while (rec != null) {
                tamano++;
                if (rec.getSig() == null) {
                    fin = rec;
                }
                rec = rec.getSig();
            }
        }
    }

    public ListaNodos getFin() {
        return fin;
    }

    public void setFin(ListaNodos nuevoFin) {
        if (nuevoFin != null) {
            fin = nuevoFin;
            fin.setSig(null);
            ListaNodos rec = fin;
            tamano = 0;
            while (rec != null) {
                tamano++;
                if (rec.getAnt() == null) {
                    ptr = rec;
                }
                rec = rec.getAnt();
            }
        }
    }

    public Object getObject(int pos) {
        ListaNodos ret = null;
        if (ptr != null) {
            if (ptr.getSig() == null) {
                return ptr.getNodeValue().getObj();
            } else {
                ListaNodos tmptr = ptr;
                int rec = 0;
                do {
                    rec++;
                    if (rec == pos) {
                        return tmptr.getNodeValue().getObj();
                    }
                    tmptr = tmptr.getSig();
                } while (tmptr != null);
            }
        }
        return ret;
    }

    public Object getObjectCasting(int pos) {
        ListaNodos nl = (ListaNodos) getObject(pos);
        return nl.getNodeValue().getObj();
    }

    @Override
    public String toString() {
        String cad = "";
        ListaNodos n = ptr;
        while (n != null) {
            cad += n.getNodeValue().getObj().toString() + "\n";
            n = n.getSig();
        }
        return cad;
    }

    public String toString2() {
        String cad = "";
        ListaNodos n = ptr;
        while (n != null) {
            cad += n.getNodeValue().getObjrelacion().toString() + "\n";
            n = n.getSig();
        }
        return cad;
    }

    public String toString3() {
        String cad = "";
        ListaNodos n = ptr;
        while (n != null) {
            cad += n.getNodeValue().getRec() + "\n";
            n = n.getSig();
        }
        return cad;
    }

    public int getTamano() {
        return tamano;
    }

    public void vaciar() {
        ptr = null;
        fin = null;
        this.tamano = 0;
    }

}
