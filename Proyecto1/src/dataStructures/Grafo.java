package dataStructures;

import co.bs.list.BSList;
import co.bs.list.BSNodeList;
import java.awt.Color;
import java.awt.Point;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Grafo {

    Vertice cab;
    Vertice fin;
    private final String sepDato = "@";
    private final String sepDato2 = "*";
    BSList listEtiquetas = new BSList();//resultado de la lista de etiquetas generada por dijkstra
    private BSBuilderGraph builder = null;
    BSList objetosrevisado = null;//lista de objetos comprobados

    private boolean visibleExpansion = false;//establece si se muestra la expansion minima

    public enum expansionMinima {
        Kruskal, Prim
    };
    private expansionMinima metodoExpansion = expansionMinima.Kruskal;

    private boolean proceso = false;//es para indicar que se esta realizando un proceso
    //y no realizar continuas actualizaciones cuando este se esté ejecutando, sino cuando termine

    public boolean isVacio() {
        return cab == null;
    }

    public Grafo(javax.swing.JPanel contenedor) {
        builder = new BSBuilderGraph(contenedor, this);
    }

    public void setAreaGrafica(javax.swing.JPanel contenedor) {
        if (builder != null) {
            builder.setArea(contenedor);
        }
    }

    public Grafo() {
        builder = null;
    }

    public void eliminarVertices() {
        cab = null;
        fin = null;
        if (builder != null) {
            builder.setExpansionMinima(this.getKruskal());
            builder.setCi(new Point(0, 0));
        }
    }

    public void eliminarAdyacencias() {
        setVisibleExpansion(false);
        if (cab != null) {
            Vertice v = cab;
            while (v != null) {
                v.numeroVerticesEntrantes = 0;
                v.numeroVerticesSalientes = 0;
                v.listaAdyacenciaSaliente = null;
                v.listaAdyacenciaEntrante = null;
                v = v.siguienteVerticeEnLista;
            }
        }
    }

    private boolean isNombreVerticeValido(String nombre) {
        if ((nombre.trim().equals("") | nombre.equals(sepDato)
                | nombre.equals(sepDato2))) {
            return false;
        }
        return true;
    }

    public boolean agregarVertices(String nombres) {
        String inicios[] = nombres.trim().split(",");

        for (int i = 0; i < inicios.length; i++) {
            if (isNombreVerticeValido(inicios[i]) == false || buscarVertice(inicios[i]) != null) {
                continue;
            }
            Vertice q = new Vertice(inicios[i]);
            if (cab == null) {
                cab = q;
                fin = q;
            } else {
                fin.siguienteVerticeEnLista = q;
                fin = q;
            }
            if (builder != null) {
                builder.agregarVerticeGrafico(q);
            }
        }
        if (builder != null) {
            builder.updateAndRepaint();
        }
        return true;
    }

    public boolean agregarVerticeCaracteristico(Vertice vertice) {
        //se agrega un vertice a partir de las caracteristicas visuales de otro
        if (isNombreVerticeValido(vertice.getNombreVertice()) == false || buscarVertice(vertice.getNombreVertice()) != null) {
            return false;
        }
        Vertice q = new Vertice(vertice);
        if (cab == null) {
            cab = q;
            fin = q;
        } else {
            fin.siguienteVerticeEnLista = q;
            fin = q;
        }
        return true;
    }

    private boolean agregarVerticeYAdyacente(String nombreVerticeInicio, String nombreVerticeFinal, double peso, boolean dirigido) {
        //es unico para crear grafos faciles, solo usado internamente por los procedimientos kruskal y prim
        //este procedimiento crea el adyacente y el vertice de inicio si no existe
        nombreVerticeFinal = nombreVerticeFinal.replace(',', " ".charAt(0));

        Vertice vi = buscarVertice(nombreVerticeInicio);
        if (vi == null) {//si no existe el vertice de inicio, entonces lo creamos
            agregarVertices(nombreVerticeInicio);
            vi = buscarVertice(nombreVerticeInicio);
        }
        Vertice vf = buscarVertice(nombreVerticeFinal);//buscamos el vertice final
        if (vi != null) {
            if (vf == null) {
                if (agregarVertices(nombreVerticeFinal) == false) {
                    return false;
                }
                vf = buscarVertice(nombreVerticeFinal);
            }
            if (agregarAdyacencia(vi, vf, peso, dirigido)) {
                if (builder != null) {
                    builder.agregarAdyacenteGrafico(vi, vf, dirigido);
                }
            }
            return true;
        }
        return false;
    }

    public boolean agregarAdyacencia(Vertice verticeInicio, Vertice verticeFinal, double peso, boolean dirigido) {
        boolean valor = verticeInicio.agregarAdyacencia(verticeFinal, peso, dirigido);
        if (!proceso) {//si no se está realizando un proceso de añadir varios adyacentes
            if (builder != null) {//si tenemos un visor de grafo
                if (isVisibleExpansion()) {//y se está mostrando el kruskal
                    builder.setExpansionMinima(this.getKruskal());//refrescamos el kruskal grafico
                }
            }
        }
        return valor;
    }

    public boolean agregarAdyacencia(String nombreVerticeInicio, String nombreVerticeFinal, double peso, boolean dirigido) {
        //este procedimiento crea el adyacente si no existe
        nombreVerticeFinal = nombreVerticeFinal.replace(',', " ".charAt(0));
        Vertice vi = buscarVertice(nombreVerticeInicio);
        Vertice vf = buscarVertice(nombreVerticeFinal);
        if (vi != null) {
            if (vf == null) {
                if (agregarVertices(nombreVerticeFinal) == false) {
                    return false;
                }
                vf = buscarVertice(nombreVerticeFinal);
            }
            if (agregarAdyacencia(vi, vf, peso, dirigido)) {
                if (builder != null) {
                    if (isVisibleExpansion()) {
                        //no se aceptan operaciones si esta visible kruskal
                        builder.setExpansionMinima(this.getKruskal());
                    } else {
                        builder.agregarAdyacenteGrafico(vi, vf, dirigido);
                    }

                }
            }
            return true;
        }
        return false;
    }

    public boolean eliminarAdyacente(String nombreVertice, String nombreVerticeAdyacencia) {
        boolean estado = false;
        Vertice ve = buscarVertice(nombreVertice);
        if (ve != null) {
            estado = ve.eliminarAdyacencia(nombreVerticeAdyacencia);
        }
        if (estado) {//si se eliminó algo
            if (builder != null) {//y tenemos un visor
                builder.listaResaltados.vaciar();//eliminados la lista de resaltados
                if (isVisibleExpansion()) {//si estamos mostrando el kruskal
                    //hay que actualizar el kruskal
                    builder.setExpansionMinima(this.getKruskal());
                }
            }
        }
        return estado;

    }

    boolean eliminarAdyacente(Vertice nomVer, Vertice nomAdy) {
        boolean estado = nomVer.eliminarAdyacencia(nomAdy.getNombreVertice());
        if (estado) {//si se eliminó algo
            if (builder != null) {//y tenemos un visor
                builder.listaResaltados.vaciar();//eliminados la lista de resaltados
                if (isVisibleExpansion()) {//si estamos mostrando el kruskal
                    //hay que actualizar el kruskal
                    builder.setExpansionMinima(this.getKruskal());
                }
            }
        }
        return estado;
    }

    public Vertice buscarVertice(String verticeBusqueda) {
        Vertice p = cab;
        while (p != null) {
            if (p.getNombreVertice().equals(verticeBusqueda)) {
                return p;
            } else {
                p = p.siguienteVerticeEnLista;
            }
        }
        return null;
    }

    public boolean actualizarVertice(String nombreVerticeBusqueda, String nuevoNombreVertice) {
        boolean estado = false;
        Vertice bv = buscarVertice(nombreVerticeBusqueda);
        nuevoNombreVertice = nuevoNombreVertice.replace(',', " ".charAt(0));
        Vertice bf = buscarVertice(nuevoNombreVertice);
        if (bv != null && bf == null && isNombreVerticeValido(nuevoNombreVertice)) {
            bv.setNombreVertice(nuevoNombreVertice);
            if (builder != null) {
                builder.setTamano(bv);
            }
            estado = true;
        }
        if (estado && builder != null) {
            if (isVisibleExpansion()) {
                builder.setExpansionMinima(this.getKruskal());
            }
            builder.updateAndRepaint();
        }
        return estado;
    }

    public boolean actualizarAdyacente(String nombreVertice, String nombreAdyacente, String nuevoNombreAdyacente) {
        boolean estado = false;
        Vertice ve = buscarVertice(nombreVertice);
        if (ve != null) {
            nuevoNombreAdyacente = nuevoNombreAdyacente.replace(',', " ".charAt(0));
            if (isNombreVerticeValido(nuevoNombreAdyacente)) {
                estado = ve.modificar_ady(nombreAdyacente, nuevoNombreAdyacente);
            }
        }
        if (estado && builder != null) {
            if (isVisibleExpansion()) {
                builder.setExpansionMinima(this.getKruskal());
            }
            builder.updateAndRepaint();
        }
        return estado;
    }

    public boolean eliminarVertice(String nombre) {
        boolean estado = false;
        //    Vertice bv=buscar_ve.rtice(ver_ini);
        if (cab != null) {
            if (cab.getNombreVertice().equals(nombre) && cab.siguienteVerticeEnLista == null) {
                //if (cab.sinRelaciones()) {
                cab.quitarRelaciones();
                cab = null;
                fin = null;
                estado = true;
                //}
            } else if (cab.getNombreVertice().equals(nombre) && cab.siguienteVerticeEnLista != null) {
                //if (cab.sinRelaciones()) {
                cab.quitarRelaciones();
                cab = cab.siguienteVerticeEnLista;
                estado = true;
                //}
            } else {
                Vertice ant = cab;
                Vertice act = cab.siguienteVerticeEnLista;
                while (act != null) {
//                    if (act.nombreVertice.equals(nombre) && act.sinRelaciones()) {
                    if (act.getNombreVertice().equals(nombre)) {
                        act.quitarRelaciones();
                        ant.siguienteVerticeEnLista = act.siguienteVerticeEnLista;
                        if (act == fin) {
                            fin = ant;
                        }
                        estado = true;
                        break;
                    }
                    ant = act;
                    act = act.siguienteVerticeEnLista;
                }

            }
        }
        if (estado && builder != null) {
            builder.listaResaltados.vaciar();
            if (isVisibleExpansion()) {
                //hay que actualizar el kruskal
                builder.setExpansionMinima(this.getKruskal());
            }
        }
        return false;
    }

    public boolean isVertice2AdyacenteVertice1(String nombreVertice1, String nombreVertice2) {
        Vertice bus = buscarVertice(nombreVertice1);
        if (bus != null) {
            Vertice bus2 = buscarVertice(nombreVertice2);
            if (bus2 != null) {
                return bus.isInAdyacentes(bus2);
            }
        }
        return false;
    }

    public String recorridoProfundidad(String nombreVerticeInicio) {
        Vertice inicio = buscarVertice(nombreVerticeInicio);
        if (inicio == null) {
            return null;
        }
        String msg = "Recorrido en Profundidad de " + inicio.getNombreVertice();
        objetosrevisado = new BSList();
        msg += "\n" + recorridoProfundidad(inicio);
        return msg;
    }

    private String recorridoProfundidad(Vertice verticeInicio) {
        String msg = null;
        if (objetosrevisado.buscar(verticeInicio) == null) {//SI EL OBJETO NO FUE REVISADO
            objetosrevisado.insertar(verticeInicio);//LO INSERTO EN LOS REVISADOS
            msg = verticeInicio.getNombreVertice();//el mensaje actual es el nombre del vertice
            Adyacente adyacente = verticeInicio.listaAdyacenciaSaliente;//
            while (adyacente != null) {//recorro la lista de adyacentes del vertice
                String rec = recorridoProfundidad(adyacente.vertice);//y cada uno pasa a ser recorrido
                if (rec != null) {
                    msg += " , " + rec;
                }
                adyacente = adyacente.sig;
            }
        }
        return msg;
    }

    public String recorridoAnchura(String nombreNodoInicio) {
        Vertice inicio = buscarVertice(nombreNodoInicio);//realizo la busqueda del nombre del vertice pasado
        if (inicio == null) {
            return null;
        }
        String msg = null;
        objetosrevisado = new BSList();
        BSList listavertices = new BSList();//necesito una lista de vertices
        listavertices.insertar(inicio);//convierto el Vertice actual en una lista de vertices
        objetosrevisado.insertar(inicio);

        Vertice inicio_arco = null;//variable que guarda el primer vertice del arco evaluado
        //se entiene por arco la cantidad de aristas que se encuentra de un nodo inicial
        while (!listavertices.isVacia()) {
            Vertice va=  (Vertice) listavertices.extraerPrimero().getNodeValue().getObj();
            //ordenar el mensaje
            if (inicio_arco == va) {
                msg += " | " + va.getNombreVertice();
                inicio_arco = null;
            } else if (msg == null) {
                msg = "| " + va.getNombreVertice();
            } else {
                msg += " , " + va.getNombreVertice();
            }
            ////////////////////
            Adyacente ady = va.listaAdyacenciaSaliente;
            while (ady != null) {
                if (objetosrevisado.buscar(ady.vertice) == null) {
                    objetosrevisado.insertar(ady.vertice);
                    listavertices.insertar(ady.vertice);

                    if (inicio_arco == null) {
                        inicio_arco = ady.vertice;
                    }
                }
                ady = ady.sig;
            }
        }
        return "Recorrido en anchura de " + inicio.getNombreVertice() + "\n" + msg + " |";
    }

    public String recorridoTopologico() {
        BSList cola = new BSList();//creamos nuestra cola de vertices
        BSList lista = new BSList(); //creamos nuestra li    sta de vertices
        //que contendran los vertices pero con el numero de entradas modificado
        Vertice q = cab;
        String recorrido = "";
        while (q != null) {
            //recorremos todos los vertices del grafo
            //creamos un objeto que guardara el vertice con
            //el numero de grados de entrada mofificado
            //aunque la clase es Adyacente, no tiene nada que ver con ella
            //solo es para usar la propiedad de peso que realmente
            //sera el numero de grados de entrada
            //Adyacente nuevo_vertice = new Adyacente(q, q.numeroVerticesEntrantes);
            ///////////////////////////////////////////////////
            lista.insertar(q, q.numeroVerticesEntrantes);//añadimos el nuevo vertice a la linked list
            //guardamos en una cola todos los vertices que no tengan entrada
            if (q.numeroVerticesEntrantes == 0) {
                cola.insertar(q);
            }
            q = q.siguienteVerticeEnLista;
        }
        while (!cola.isVacia()) {
            BSNodeList nl = cola.extraerPrimero();//obtenemos y eliminamos el vertice de la cola
            Vertice nv = (Vertice) nl.getNodeValue().getObj();
            Adyacente adya = nv.listaAdyacenciaSaliente;//obtenemos la lista de adyacentes del anterior vertice
            while (adya != null) {
                //////debemos buscar cual es el numero de grado de entrada
                ///del vertice adyacente actual
                BSNodeList bus = lista.buscar(adya.vertice);
                //en este momento ya tenemos el vertice con grado modificado
                bus.getNodeValue().setRec(bus.getNodeValue().getRec() - 1);//le quitamos un grado de entrada;
                if (bus.getNodeValue().getRec() == 0) {
                    cola.insertar(bus.getNodeValue().getObj());
                }
                adya = adya.sig;
            }
            recorrido += nv.getNombreVertice() + " ";
        }
        return recorrido;
    }

    public String dijkstra(String inicio, boolean porPeso) {
        Vertice vinicio = buscarVertice(inicio);
        //si no existen el vertice, no hago el recorrdio
        if (vinicio == null) {
            return "ruta no encontrada";
        }
        return dijkstra(vinicio, porPeso);
    }

    //el ultimo parametro es el vertice al que se quiere llegar
    //lo utilizo para la visualizacion de un camino
    String dijkstra(Vertice inicio, boolean porPeso) {
        BSList listRevisados = new BSList();//es la lista de los vertices ya revisados
        listEtiquetas = new BSList();//es la lista que guardará las etiquetas
        listEtiquetas.insertar(new Etiqueta(inicio, inicio, null, 0, inicio.getNombreVertice()));//inserto la etiqueta del vertice de inicio
        int nr = 0;//es el numero de vertices revisados
        BSNodeList neti = null;//nodo para hacer recorrido en la lista
        Etiqueta etiqueta = null; //utilizada para el casting de los nodos etiquetas
        do {
            //debo buscar el menor recorrido de la lista de etiquetas
            neti = listEtiquetas.getPtr();//obtengo la cabeza de las etiquetas para poder hacer el recorrido
            BSNodeList netimenor = null;//tengo la etiqueta menor vacia
            Etiqueta etimenor = null;//es la etiqueta de la linea anterior pero ya pasada por casting
            while (neti != null) {
                etiqueta = (Etiqueta) neti.getNodeValue().getObj();//obtengo la etiqueta del recorrido
                if (!etiqueta.revisado) {
                    if (netimenor == null) {//si esta etiqueta no esta revisada y no hay etiqueta menor
                        //quiere decir que como es la primera, entonces por ahora es el menor recorrido
                        netimenor = neti;//nodo de etiqueta menor
                        etimenor = etiqueta;//etiqueta menor
                    } else if (etiqueta.recorrido < etimenor.recorrido) {//si esta etiqueta es menor
                        netimenor = neti;
                        etimenor = etiqueta;
                    }
                }
                neti = neti.getSig();
            }
            //////En este momento ya tenemos la etiqueta con el menor recorrido///////////////////////////////////////////////////
            //System.out.println("Menor="+etimenor.toString());
            etimenor.revisado = true;//la marcamos como revisada
            listRevisados.insertar(etimenor.vertice);//inserto el VERTICE, no la etiqueta, en la lista de revisados
            nr++;//incremento el numero de revisados
            Adyacente ady = etimenor.vertice.listaAdyacenciaSaliente;//obtengo la lista de adyacentes de vertice con etiqueta menor
            while (ady != null) {//recorremos la lista de adyacentes del vertice de la etiqueta encontrada con menor recorrido
                //hay que verificar si el vertice adyacente ya fue revisado, si lo fue, no se debe seguir
                if (!listRevisados.contiene(ady.vertice)) {//si este vertice no ha sido revisado
                    Etiqueta nuevaetiqueta = new Etiqueta(etimenor.principal, ady.vertice, etimenor.vertice,
                            etimenor.recorrido + ((porPeso) ? ady.peso : 1), etimenor.ruta + "," + ady.vertice.getNombreVertice());//
                    //hay que buscar si la etiqueta de este vertice ha sido insertada previamente para verificar su recorrido
                    neti = listEtiquetas.getPtr();//obtengo la cabeza de la lista de etiquetas para iniciar la iteracion o recorrido de lista
                    boolean insertarEtiqueta = true;///Validamos si la nueva etiqueta la podemos ingresar a la lista
                    while (neti != null) {
                        etiqueta = (Etiqueta) neti.getNodeValue().getObj();
                        if (etiqueta.vertice == nuevaetiqueta.vertice) {//si es la etiqueta del mismo vertice
                            //System.out.println("hacia "+etiqueta.vertice.nombreVertice);
                            if (nuevaetiqueta.recorrido < etiqueta.recorrido) {//y la actual tiene un recorrido menor
                                //System.out.println("Etiqueta sobreescrita"+nuevaetiqueta.recorrido+"<"+etiqueta.recorrido);
                                neti.getNodeValue().setObj(nuevaetiqueta);//la sobreescribimos
                                insertarEtiqueta = false;
                                ////si se encontró una menor entonces debo eliminar todas las etiquetas que tengan un recorrido en empate
                                //sabemos que no es posible que sea la cabeza, porque siempre es el vertice de inicio
                                BSNodeList naeliminar = neti.getSig();//creamos otra variable de recorrido o iteraccion
                                while (naeliminar != null) {
                                    Etiqueta etiEliminar = (Etiqueta) naeliminar.getNodeValue().getObj();//obtengo la etiqueta del recorrido
                                    if (etiEliminar.vertice == nuevaetiqueta.vertice) {//si encuentro otra etiqueta con el mismo vertice
                                        nr++;//entonces como la voy a eliminar incremento el numero de revisados
                                        //System.out.println("Eliminando ");
                                        // System.out.println(etiEliminar.toString());
                                        //JOptionPane.showMessageDialog(null, etiEliminar.toString());
                                        naeliminar.getAnt().setSig(naeliminar.getSig());
                                        if (naeliminar.getSig() != null) {
                                            naeliminar.getSig().setAnt(naeliminar.getAnt());
                                        } else {//si el elimado es el fin
                                            listEtiquetas.setFin(naeliminar.getAnt());
                                        }
                                    }
                                    naeliminar = naeliminar.getSig();
                                }
                            } else if (nuevaetiqueta.recorrido > etiqueta.recorrido) {
                                //System.out.println("Etiqueta no incluyente:"+nuevaetiqueta.recorrido+">"+etiqueta.recorrido);
                                insertarEtiqueta = false;
                            } else if (nuevaetiqueta.recorrido == etiqueta.recorrido) {
                                //System.out.println("Igual recorrido"+nuevaetiqueta.recorrido +","+etiqueta.recorrido);
                                insertarEtiqueta = true;
                            }
                            break;
                        }
                        neti = neti.getSig();
                    }
                    if (insertarEtiqueta) {//sino encontro otra etiqueta insertamos la nueva etiqueta en la lista
                        //System.out.println("Insertada " + nuevaetiqueta.toString() );
                        listEtiquetas.insertar(nuevaetiqueta);
                    }
                }
                ady = ady.sig;
            }
        } while (nr < listEtiquetas.getTamano());//mientras el numero de revisados sea menor a los insertados
        listEtiquetas.extraerPrimero();//quito la etiqueta principal, ya que no quiero mostrarla
        return listEtiquetas.toString();
    }

    public String floyd(boolean porPeso) {
        String msg = "";
        Vertice v = cab;
        while (v != null) {
            String dj = dijkstra(v.getNombreVertice(), porPeso);
            msg += ((dj.equals("") ? "" : dj));
            v = v.siguienteVerticeEnLista;
        }
        return msg;
    }

    public Grafo getKruskal() {
        Grafo grafoKruskal = new Grafo();
        if (cab != null) {
            BSList listaAdyacentes = new BSList();
            Vertice ncab = cab;
            while (ncab != null) {
                Adyacente ady = ncab.listaAdyacenciaSaliente;
                while (ady != null) {
                    listaAdyacentes.insertarPorOrdenRec(ncab, ady.vertice, ady.peso, ady.dirigido);
                    ady = ady.sig;
                }
                ncab = ncab.siguienteVerticeEnLista;
            }
            while (!listaAdyacentes.isVacia()) {
                BSNodeList ady = listaAdyacentes.extraerPrimero();
                Vertice vinicio = (Vertice) ady.getNodeValue().getObj();
                Vertice vfin = (Vertice) ady.getNodeValue().getObjrelacion();
                if (!grafoKruskal.isVerticeRamaDeImplementacionAnchura(vinicio, vfin, 1)) {
                    if (!grafoKruskal.isVerticeRamaDeImplementacionAnchura(vfin, vinicio, 1)) {
                        grafoKruskal.agregarVerticeYAdyacente(vinicio.getNombreVertice(), vfin.getNombreVertice(), ady.getNodeValue().getRec(), ady.getNodeValue().isEstado());
                    }
                }
            }
        }
        return grafoKruskal;
    }

    public Grafo getPrim() {
        Grafo grafoPrim = new Grafo();
        BSList listaVisitados = new BSList();//es para no incluir nuevamente
        //los adyacentes de los visitados, si llegan nuevamente como vertices finales
        BSList listaAdyacentes = new BSList();
        if (cab != null) {
            listaAdyacentes.insertarPorOrdenRec(null, cab, 0, false);//inserto el grupo formado
            //por el vertice inicial, que viene de ninguno, con recorrido cero, y no es dirigido
            while (!listaAdyacentes.isVacia()) {
                /////////////////////////
                BSNodeList ady = listaAdyacentes.extraerPrimero();//devuelve siempre el primero
                Vertice vinicio = (Vertice) ady.getNodeValue().getObj();
                Vertice vfin = (Vertice) ady.getNodeValue().getObjrelacion();
                /////////////////////////
                if (!listaVisitados.contiene(vfin)) {
                    boolean expandir = false;
                    if (vinicio == null) {//LA PRIMERA VEZ QUE EXTRAIGA UN OBJETO,SERA EL INICIO NULO, SOLO HAY QUE AGREGAR EL FIN
                        grafoPrim.agregarVertices(vfin.getNombreVertice());
                        expandir = true;
                    } else {
                        listaVisitados.insertar(vfin);
                        if (!grafoPrim.isVerticeRamaDeImplementacionAnchura(vinicio, vfin, 1)) {
                            if (!grafoPrim.isVerticeRamaDeImplementacionAnchura(vfin, vinicio, 1)) {
                                grafoPrim.agregarVerticeYAdyacente(vinicio.getNombreVertice(), vfin.getNombreVertice(), ady.getNodeValue().getRec(),
                                        ady.getNodeValue().isEstado());
                                expandir = true;
                            }
                        }
                    }
                    if (expandir) {
                        Adyacente lady = vfin.listaAdyacenciaSaliente;
                        while (lady != null) {
                            listaAdyacentes.insertarPorOrdenRec(vfin, lady.vertice, lady.peso, lady.dirigido);
                            lady = lady.sig;
                        }
                    }
                }
            }
        }
        return grafoPrim;
    }

    public boolean isVerticeRamaDeImplementacionAnchura(Vertice verInicio, Vertice vertAEncontrar, int minRec) {
        ////////los vertices pasados en estos parametros, necesariamente no será parte de este mismo grafo...
        //por tanto solo seran necesarios sus nombres, para la correspondiente busqueda
        if (verInicio == null || vertAEncontrar == null) {
            return false;
        }
        String nomVerInicio = verInicio.getNombreVertice(), nomVertAEncontrar = vertAEncontrar.getNombreVertice();
        //esta es una implementacion de anchura para saber si un vertice se encuentra en alguna rama de otro vertice
        //el parametro de MinRec indica cual es el minimo numero de arcos que debe haber recorrido
        //para devolver verdadero si se llega al "VerticeAEncontrar"
        Vertice inicio = buscarVertice(nomVerInicio);//realizo la busqueda del nombre del vertice pasado
        if (inicio == null) {
            return false;
        }
        int numArcos = 0;//es la cantidad de arcos expandidos en el recorrido de anchura
        BSList listavertices = new BSList();//necesito una lista de vertices
        listavertices.insertar(inicio);//convierto el Vertice actual en una lista de vertices
        objetosrevisado = new BSList();
        objetosrevisado.insertar(inicio);
        Vertice inicio_arco = null;//variable que guarda el primer vertice del arco evaluado
        //se entiene por arco la cantidad de aristas que se encuentra de un nodo inicial
        while (!listavertices.isVacia()) {
            Vertice verticeExtraido = (Vertice) listavertices.extraerPrimero().getNodeValue().getObj();
            if (inicio_arco == verticeExtraido) {
                inicio_arco = null;
                numArcos++;
            }
            ////////////////////
            Adyacente ady = verticeExtraido.listaAdyacenciaSaliente;
            while (ady != null) {
                if (objetosrevisado.buscar(ady.vertice) == null) {
                    if (ady.vertice.getNombreVertice().equalsIgnoreCase(nomVertAEncontrar)) {//si en una rama esta el vertice a encontrar
                        if (numArcos >= minRec) {//si el numero de arcos es mayor o igual al numero de arcos requeridos
                            return true;
                        }
                    } else {
                        objetosrevisado.insertar(ady.vertice);
                        listavertices.insertar(ady.vertice);
                    }
                    if (inicio_arco == null) {
                        inicio_arco = ady.vertice;
                    }
                }
                ady = ady.sig;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String t = "Contenido del Grafo: \n";
        Vertice p = cab;
        while (p != null) {
            t += p.toString() + "\n";
            p = p.siguienteVerticeEnLista;
        }
        return t;
    }

    public void allVerWithAll() {
        proceso = true;
        if (cab != null) {
            Vertice tcab = cab;
            while (tcab != null) {
                Vertice tact = cab;
                while (tact != null) {
                    if (tact != tcab) {
                        agregarAdyacencia(tcab, tact, 1, false);
                    }
                    tact = tact.siguienteVerticeEnLista;
                }
                tcab = tcab.siguienteVerticeEnLista;
            }
        }
        proceso = false;
        updateGraphics();
    }

    /**
     * **********************************************
     */
    //Guardar grafo
    public void archivoGuardar(String nomarchivo) throws IOException {
        FileOutputStream archivo = null;
        try {
            archivo = new FileOutputStream(nomarchivo);
            DataOutputStream escritura = new DataOutputStream(archivo);

            escritura.writeBoolean(this.isVisibleExpansion());
            escritura.writeBoolean(builder.dibujarCuadricula);
            escritura.writeInt(builder.getCi().x);//coordenadas de inicio
            escritura.writeInt(builder.getCi().y);
            //System.out.println(ci.x +","+ci.y);
            Vertice v = cab;
            ///guardo los vertices
            while (v != null) {
                escritura.writeUTF(v.getNombreVertice());
                escritura.writeInt((int) v.getX());
                escritura.writeInt((int) v.getY());
                // System.out.print(v.nombreVertice+"("+v.x+","+v.y+")");
                v = v.siguienteVerticeEnLista;
            }
            escritura.writeUTF(sepDato);//termine de guardar los vertices
            v = cab;
            while (v != null) {
                if (v.listaAdyacenciaSaliente != null) {
                    escritura.writeUTF(v.getNombreVertice());//guardo el vertice
                    //System.out.println(v.nombreVertice);
                    Adyacente ady = v.listaAdyacenciaSaliente;
                    while (ady != null) {
                        escritura.writeUTF(ady.vertice.getNombreVertice());//con sus correspondientes adyacentes y pesos
                        escritura.writeDouble(ady.peso);
                        escritura.writeBoolean(ady.dirigido);
                        ///System.out.println("|"+ady.nomAdy.nombreVertice+","+ady.peso+"|");
                        ady = ady.sig;
                    }
                    escritura.writeUTF(sepDato2);//guardo el separador
                    //System.out.println(sepDato2);
                }

                v = v.siguienteVerticeEnLista;
            }
            escritura.writeUTF(sepDato);
            //System.out.println(sepDato);
            escritura.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ioe) {
        }
        try {
            archivo.close();
        } catch (IOException ex) {

        }
    }

    public void archivoCargar(String nomarchivo) throws IOException {
        builder.cargandoArchivo = true;
        builder.dibujarInmediato = false;
        FileInputStream archivo = null;
        boolean tmpvisibleKruskal = false;
        try {
            archivo = new FileInputStream(nomarchivo);
            DataInputStream lectura = new DataInputStream(archivo);
            tmpvisibleKruskal = lectura.readBoolean();
            builder.dibujarCuadricula = lectura.readBoolean();
            Point tci = new Point(0, 0);
            tci.x = lectura.readInt();//coordenadas de inicio
            tci.y = lectura.readInt();//coordenadas de inicio

            builder.setCi(new Point(tci));

            cab = null;
            fin = null;
            ///guardo los vertices
            String nombre = null;
            while (true) {
                nombre = lectura.readUTF();
                if (nombre.equals(sepDato)) {
                    break;//cuando obtenga el separador se detiene
                }
                Point coordenadasVertice = new Point();
                coordenadasVertice.x = lectura.readInt();
                coordenadasVertice.y = lectura.readInt();
                builder.coordenadasVertice = coordenadasVertice;
                agregarVertices(nombre);
            }
            if (cab == null) {
                archivo.close();
            } else {
                String nadya = null;
                double padya = 0;
                while (true) {
                    nombre = lectura.readUTF();
                    if (nombre.equals(sepDato)) {
                        break;
                    }
                    while (true) {
                        nadya = lectura.readUTF();
                        if (nadya.equals(sepDato2)) {
                            break;
                        }
                        padya = lectura.readDouble();
                        boolean dir = lectura.readBoolean();
                        agregarAdyacencia(nombre, nadya, padya, dir);
                    }

                }
                archivo.close();
                if (isVisibleExpansion()) {
                    builder.setExpansionMinima(this.getKruskal());
                }
            }
        } catch (IOException ioe) {
        }
        try {
            archivo.close();
            if (isVisibleExpansion()) {
                builder.setExpansionMinima(this.getKruskal());
            }
        } catch (NullPointerException npe) {
        } catch (IOException ex) {

        }
        builder.dibujarInmediato = true;
        builder.cargandoArchivo = false;
        builder.updateGraphics();
        builder.repaint();
    }

    public boolean isDibujarDirigido() {
        return builder.dibujarDirigido;
    }

    public void setDibujarDirigido(boolean dibujarDirigido) {
        this.builder.dibujarDirigido = dibujarDirigido;
    }

    public void repaint() {
        builder.repaint();
    }

    public boolean isDibujarinmediato() {
        return builder.dibujarInmediato;
    }

    public void setDibujarinmediato(boolean dibujarinmediato) {
        this.builder.dibujarInmediato = dibujarinmediato;
    }

    public void updateGraphics() {
        builder.updateAndRepaint();
    }

    public boolean isDibujarCuadricula() {
        return builder.dibujarCuadricula;
    }

    public void setDibujarCuadricula(boolean dibujarCuadricula) {
        this.builder.dibujarCuadricula = dibujarCuadricula;
        updateGraphics();
        repaint();
    }

    public void interfazTerminarArrastre(int x, int y, int button) {
        builder.interfazTerminarArrastre(x, y, button);
    }

    public void interfazClickOIniciarArrastre(int x, int y, int button) {
        builder.interfazClickOIniciarArrastre(x, y, button);
    }

    public void interfazArrastre(int x, int y, boolean limitar) {
        builder.interfazArrastre(x, y, limitar);
    }

    public String interfazMovimientoMouse(int x, int y) {
        return builder.interfazMovimientoMouse(x, y);
    }

    public void interfazTeclaPulsada(int tecla) {
        builder.interfazTeclaPulsada(tecla);
    }

    public void setCi(Point point) {
        builder.setCi(point);
    }

    public void organizarAnchuraVisual(String nombreVertice) {
        builder.organizarPorAnchura(nombreVertice);
    }

    public void setPesoGrafico(int peso) {
        if (peso > 0) {
            builder.pesoAdyacenteGrafico = peso;
        }
    }

    public boolean isVisibleExpansion() {
        return visibleExpansion;
    }

    public void setVisibleExpansion(boolean visibleExpansion) {
        this.visibleExpansion = visibleExpansion;
        if (builder != null) {
            builder.setMetodoExpansion(metodoExpansion);
            if (visibleExpansion) {
                if (metodoExpansion == expansionMinima.Kruskal) {
                    builder.colorExpansion = Color.RED;
                    builder.setExpansionMinima(this.getKruskal());
                } else {
                    builder.colorExpansion = Color.orange;
                    builder.setExpansionMinima(this.getPrim());
                }

            } else {
                builder.setExpansionMinima(null);
            }
        }
    }

    public void setMostrarSoloExpansion(boolean mostrarSoloExpansion) {
        if (builder != null) {
            builder.setMostrarSoloExpansion(mostrarSoloExpansion);
        }
    }

    public void setPesoReal(boolean estado) {
        Vertice.adyacenciaPesoReal = estado;
        if (estado) {
            Vertice tmp = cab;
            while (tmp != null) {
                Adyacente ady = tmp.listaAdyacenciaSaliente;
                while (ady != null) {
                    ady.peso = tmp.getPesoReal(ady.vertice);
                    ady = ady.sig;
                }
                tmp = tmp.siguienteVerticeEnLista;
            }
            if (isVisibleExpansion() && builder != null) {
                builder.setExpansionMinima(this.getKruskal());
            }
            updateGraphics();
        }

    }

    public expansionMinima getMetodoExpansion() {
        return metodoExpansion;
    }

    public void setMetodoExpansion(expansionMinima metodoExpansion) {
        this.metodoExpansion = metodoExpansion;
        setVisibleExpansion(visibleExpansion);

    }
}

class Etiqueta {

    Vertice principal = null;
    Vertice vertice = null;
    Vertice origen = null;
    double recorrido = 0;
    boolean revisado = false;
    String ruta = "";

    public Etiqueta(Vertice principal, Vertice vertice, Vertice origen, double recorrido, String ruta) {
        this.principal = principal;
        this.vertice = vertice;
        this.origen = origen;
        this.recorrido = recorrido;
        this.revisado = false;
        this.ruta = ruta;
    }

    @Override
    public String toString() {
        //return "De "+((origen==null)?"-":origen.nombreVertice)+"→"+vertice.nombreVertice+"**"+ruta+"="+recorrido;
        //return "De "+principal.nombreVertice+"→"+vertice.nombreVertice+"**"+ruta+"="+recorrido;
        //A → B |A, B|  Peso 1
        return principal.getNombreVertice() + " → " + vertice.getNombreVertice() + " |" + ruta + "|  Peso " + (int) recorrido;
    }
}