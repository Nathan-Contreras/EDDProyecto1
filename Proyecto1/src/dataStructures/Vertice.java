package dataStructures;

import java.awt.Rectangle;

public class Vertice {
        static boolean adyacenciaPesoReal = false;
    static boolean actualizarPesoReal = true;
    ///////////////////////////////
    private String nombreVertice;
    protected int numeroVerticesEntrantes;
    protected int numeroVerticesSalientes;
    Vertice siguienteVerticeEnLista;
    Adyacente listaAdyacenciaSaliente;//lista a donde se dirigen
    Adyacente listaAdyacenciaEntrante;//lista de los que llegan

    public Vertice(Vertice vertice) {
        //se crea un vertice a partir de las caracteristicas visuales de otro vertice
        this.nombreVertice = vertice.nombreVertice;
        this.numeroVerticesEntrantes = 0;
        this.numeroVerticesSalientes = 0;
        this.siguienteVerticeEnLista = null;
        this.listaAdyacenciaSaliente = null;
        this.listaAdyacenciaEntrante = null;
        super.x = (int) vertice.getX();
        super.y = (int) vertice.getY();
        this.width = vertice.width;
        this.height = vertice.height;
    }

    public Vertice(String nombre) {
        this.nombreVertice = nombre;
        this.numeroVerticesEntrantes = 0;
        this.numeroVerticesSalientes = 0;
        this.siguienteVerticeEnLista = null;
        this.listaAdyacenciaSaliente = null;
        this.listaAdyacenciaEntrante = null;
    }

    public boolean sinRelaciones() {
        if (numeroVerticesEntrantes == 0 && numeroVerticesSalientes == 0) {
            return true;
        }
        return false;
    }

    /**
     * Inserta un aycente al final de la lista de adyacentes
     *
     * @param verticeDestino
     * @param peso
     * @param dirigido
     * @return
     */
    public boolean agregarAdyacencia(Vertice verticeDestino, double peso, boolean dirigido) {
        if (verticeDestino == this) {
            return false;//no se puede enlazar el vertice con el mismo...no por ahora
        }
        if (isInAdyacentes(verticeDestino)) {
            return false;//si este vertice ya se encuentra en los adyacentes sale del proc
        }
        if (adyacenciaPesoReal) {
            peso = getPesoReal(verticeDestino);
        }
        boolean agregado = false;
        Adyacente q = new Adyacente(verticeDestino, peso, dirigido);//se crea el adyacente
        if (this.listaAdyacenciaSaliente == null) {//si no hay lista de adyacentes
            this.listaAdyacenciaSaliente = q;//este es el primer adyacente
            verticeDestino.numeroVerticesEntrantes++;
            this.numeroVerticesSalientes++;
            q.vertice.agregarAdyacenciaEntrante(this, peso);
            agregado = true;
        } else {
            //Ubicarse al final de la lista de adyacentes
            Adyacente ult = this.listaAdyacenciaSaliente;
            while (ult.sig != null) {
                ult = ult.sig;
            }
            ///////////////
            ult.sig = q; //Unimos el último nodo con el nuevo nodo
            q.vertice.agregarAdyacenciaEntrante(this, peso);
            verticeDestino.numeroVerticesEntrantes++;
            this.numeroVerticesSalientes++;
            agregado = true;
        }
        if (agregado) {
            Adyacente bus = verticeDestino.buscarAdyacencia(this.getNombreVertice());//busca si se encuentra este vertice en el
            //vertice final
            //si es asi..entonces el adyacente se convierte en no dirigido, es decir, doble via
            if (bus != null) {
                q.dirigido = false;
                bus.dirigido = false;
            } else if (!dirigido) {
                //se debe añadir este Vertice a la lista de adyacencia del vertice final;
                verticeDestino.agregarAdyacencia(this, peso, dirigido);
            }
        }
        return agregado;
    }

    public boolean eliminarAdyacencia(String nombreVerticeAdyacente) {
        boolean eliminado = false;
        Adyacente adyacenteliminado = null;//guardo el adyacente que elimino
        if (listaAdyacenciaSaliente != null) {
            if (listaAdyacenciaSaliente.vertice.getNombreVertice().equals(nombreVerticeAdyacente) && listaAdyacenciaSaliente.sig == null) {
                adyacenteliminado = listaAdyacenciaSaliente;
                numeroVerticesSalientes--;
                listaAdyacenciaSaliente.vertice.numeroVerticesEntrantes--;
                listaAdyacenciaSaliente = null;
                eliminado = true;
            } else if (listaAdyacenciaSaliente.vertice.getNombreVertice().equals(nombreVerticeAdyacente) && listaAdyacenciaSaliente.sig != null) {
                adyacenteliminado = listaAdyacenciaSaliente;
                numeroVerticesSalientes--;
                listaAdyacenciaSaliente.vertice.numeroVerticesEntrantes--;
                listaAdyacenciaSaliente = listaAdyacenciaSaliente.sig;
                eliminado = true;
            } else {
                Adyacente ant = listaAdyacenciaSaliente;
                Adyacente act = listaAdyacenciaSaliente.sig;
                while (act != null) {
                    if (act.vertice.getNombreVertice().equals(nombreVerticeAdyacente)) {
                        adyacenteliminado = act;
                        numeroVerticesSalientes--;
                        act.vertice.numeroVerticesEntrantes--;
                        ant.sig = act.sig;
                        eliminado = true;
                        break;
                    }
                    ant = act;
                    act = act.sig;
                }
            }
            if (eliminado) {
                adyacenteliminado.vertice.eliminarAdyacenciaEntrante(this);//eliminamos este vertice de la lista
                if (!adyacenteliminado.dirigido) {//si este adyacente es dirigido
                    adyacenteliminado.vertice.eliminarAdyacencia(this.getNombreVertice());
                }
                //de llegados del vertice eliminado de esta lista de adyacentes
            }
        }
        return eliminado;
    }

    private boolean agregarAdyacenciaEntrante(Vertice verticeOrigen, double peso) {
        Adyacente q = new Adyacente(verticeOrigen, peso, false);
        if (this.listaAdyacenciaEntrante == null) {
            this.listaAdyacenciaEntrante = q;
            return true;
        } else {
            //Ubicarse al final de la lista de adyacentes
            Adyacente ult = this.listaAdyacenciaEntrante;
            while (ult.sig != null) {
                if (ult.vertice == verticeOrigen) {
                    return false;//existe en la lista
                }
                ult = ult.sig;
            }
            if (ult.vertice == verticeOrigen) {
                return false;//existe en la lista
            }
            ult.sig = q; //Unimos el último nodo con el nuevo nodo
        }
        return false;
    }

    private boolean eliminarAdyacenciaEntrante(Vertice verticeOrigen) {
        if (this.listaAdyacenciaEntrante != null) {
            if (listaAdyacenciaEntrante.vertice == verticeOrigen && listaAdyacenciaEntrante.sig == null) {
                listaAdyacenciaEntrante = null;
                return true;
            } else if (listaAdyacenciaEntrante.vertice == verticeOrigen && listaAdyacenciaEntrante.sig != null) {
                listaAdyacenciaEntrante = listaAdyacenciaEntrante.sig;
                return true;
            } else {
                Adyacente ant = listaAdyacenciaEntrante;
                Adyacente act = listaAdyacenciaEntrante.sig;
                while (act != null) {
                    if (act.vertice == verticeOrigen) {
                        ant.sig = act.sig;
                        return true;
                    }
                    ant = act;
                    act = act.sig;
                }
            }
        }
        return false;
    }

    public boolean quitarRelaciones() {
        Adyacente ady = listaAdyacenciaSaliente;
        while (ady != null) {
            eliminarAdyacencia(ady.vertice.getNombreVertice());
            ady = ady.sig;
        }
        ady = listaAdyacenciaEntrante;
        while (ady != null) {
            eliminarAdyacenciaEntrante(ady.vertice);
            ady.vertice.eliminarAdyacencia(this.getNombreVertice());
            ady = ady.sig;
        }
        return false;
    }

    public boolean isInAdyacentes(Vertice v) {
        if (listaAdyacenciaSaliente != null) {
            Adyacente ady = listaAdyacenciaSaliente;
            while (ady != null) {
                if (ady.vertice == v) {
                    return true;
                }
                ady = ady.sig;
            }
        }
        return false;
    }

    public Adyacente buscarAdyacencia(String nombreVertice) {
        if (listaAdyacenciaSaliente != null) {
            Adyacente ady = listaAdyacenciaSaliente;
            while (ady != null) {
                if (ady.vertice.getNombreVertice().equals(nombreVertice)) {
                    return ady;
                }
                ady = ady.sig;
            }
        }
        return null;
    }

    public boolean modificar_ady(String nombre, String nnombre) {
        Adyacente ba = buscarAdyacencia(nombre);
        if (ba != null) {
            ba.vertice.setNombreVertice(nnombre);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String t = "Ver: " + getNombreVertice() + " GE: " + numeroVerticesEntrantes + " GS: " + numeroVerticesSalientes + "\n";
        Adyacente p = this.listaAdyacenciaSaliente;
        while (p != null) {
            t += " Ady:" + p.toString() + "\n";
            p = p.sig;
        }
        return t;
    }

    /**
     * Calcula el Peso real de un vertice hacia otro teniendo en cuenta la posición en pantalla
     * @param verticeFinal
     * @return 
     */
    public double getPesoReal(Vertice verticeFinal) {
        return Math.sqrt(Math.pow(this.getCenterX() - verticeFinal.getCenterX(), 2) + Math.pow(this.getCenterY() - verticeFinal.getCenterY(), 2));
    }

    /**
     * Actualiza todos los pesos hacia los vertices
     */
    public void actualizarPesos() {
        Adyacente ady = listaAdyacenciaEntrante;
        while (ady != null) {
            Adyacente ady2 = ady.vertice.buscarAdyacencia(this.getNombreVertice());
            if (ady2 != null) {
                ady2.peso = getPesoReal(ady.vertice);
            }
            ady = ady.sig;
        }
        ady = listaAdyacenciaSaliente;
        while (ady != null) {
            ady.peso = getPesoReal(ady.vertice);
            ady = ady.sig;
        }
    }

    public void setX(int x) {
        super.x = x;
        if (adyacenciaPesoReal && actualizarPesoReal) {
            actualizarPesos();
        }
//        this.x = x;
    }

    public void setY(int y) {
        super.y = y;
        if (adyacenciaPesoReal && actualizarPesoReal) {
            actualizarPesos();
        }
        //        this.y = y;
    }

    public void setXY(int x, int y) {
        super.x = x;
        super.y = y;
        if (adyacenciaPesoReal && actualizarPesoReal) {
            actualizarPesos();
        }
        //        this.x=x;this.y=y;
    }

    public void setAddXY(int x, int y) {
        super.x += x;
        super.y += y;
        if (adyacenciaPesoReal && actualizarPesoReal) {
            actualizarPesos();
        }
        //        this.x+=x;this.y+=y;
    }

    public String getNombreVertice() {
        return nombreVertice;
    }

    public void setNombreVertice(String nombreVertice) {
        this.nombreVertice = nombreVertice;
    }
}
