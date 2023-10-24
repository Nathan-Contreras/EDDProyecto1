/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataStructures;

import dataStructures.Grafo.expansionMinima;
import Listas.Lista;
import Listas.ListaNodos;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

class builderGraph {

    private Grafo grafo = null;//grafo original
    private Grafo grafoExpansion = null;//grafo anterior con expansion minima
    private boolean mostrarSoloExpansion = false;//especifica si solo se muestra el grafo de expansion
    private expansionMinima metodoExpansion = expansionMinima.Kruskal;
    //es decir se colocan invisibles las adyacencias que no fueron elegidas
    Color colorExpansion = Color.RED;
    private Color colorFondo = Color.BLACK;
    //////////////////////
    //////////////////////
    //////////////////////de aqui en Adelante sera toda la parte grafica del grafo
    boolean cargandoArchivo = false;
    Point coordenadasVertice = null;//utilizada en la carga del archivo
    boolean dibujarInmediato = false;
    boolean dibujarCuadricula = false;
    //boolean siempremostrarflecha=false;
    boolean dibujarDirigido = false;
    private JPanel componenteParaDibujar;//contenedor del juego
    private Image imagenParaDibujar;//temporal para el doble buffer
    private Graphics graficoDelComponente = null;
    private Graphics2D graficoDeLaImagen = null;//grafico de la imagen interna
    //////////////////////
    int pesoAdyacenteGrafico = 1;
    private Vertice verticeEnArrastre = null;
    private Point coordenadaInicialVerticeEnArrastre = new Point(0, 0);//COORDENADAS VISUALES INICIALES DE UN VERTICE EN ARRASTRE
    private boolean limitarArrastre = false;
    //////////////////////
    private boolean moverArea = false;//se esta moviendo el componenteParaDibujar
    private Point cm = null;//coordenada de inicio de mover componenteParaDibujar
    private Point ci = new Point(0, 0);//estas son las coordenadas de inicio del componenteParaDibujar grafica;
    private Rectangle cuadroSeleccion = null;
    private boolean enCuadroDeSeleccion = false;
    private Lista listaVSel = new Lista();//lista de vertices seleccionados
    private Point coordenadasDeCuadroDeSeleccion = null;//coordenadas de cuadro seleccion
    private final int AJUST_INC = 10;//incremento de tamaño del componenteParaDibujar de dibujo
    ////////////////////////////////////
    Lista listaResaltados = new Lista();//lista de vertices y adyacencias para resaltar
    private Vertice verticeFinalDijkstra = null;
    private Vertice verticeBajoElMouse = null;
    ////////////////////////////////////
    //////////////////////////////
    Font fuenteDeLetraParaElVertice = new java.awt.Font("Arial", Font.BOLD, 20);
    FontMetrics metricaFuenteVertice = null;

    public builderGraph(javax.swing.JPanel contenedor, Grafo grafo) {
        this.grafo = grafo;
        setArea(contenedor);
    }

    void agregarVerticeGrafico(Vertice v) {
        setTamano(v);
        setPosicionRandom(v);
        dibujarVertice(v);
    }

    void setTamano(Vertice v) {
        Rectangle2D medidas = metricaFuenteVertice.getStringBounds(v.getNombreVertice(), graficoDeLaImagen);
        if (v.getNombreVertice().trim().equals("")) {
            v.height = 35;
            v.width = 35;
        } else {
            v.height = (int) medidas.getHeight() + 10;
            v.width = (int) medidas.getWidth() + 10;
        }
    }

    private void setPosicionRandom(Vertice v) {
        if (cargandoArchivo) {
            v.setXY(coordenadasVertice.x, coordenadasVertice.y);
        } else {
            do {
                int x = (-ci.x) + (int) ((Math.random() * (componenteParaDibujar.getWidth() - (v.width))));
                int y = (-ci.y) + (int) ((Math.random() * (componenteParaDibujar.getHeight() - (v.height))));
                v.setXY(x, y);
            } while (isIntersectVertice(v) == true);
        }
    }

    private void dibujarVertice(Vertice v) {
        if (cargandoArchivo) {
            return;
        }
        graficoDeLaImagen.setColor(Color.blue);
        graficoDeLaImagen.fillRect(getCi().x + (int) v.getX(), getCi().y + (int) v.getY(), v.width, v.height);
        if (listaResaltados.contiene(v)) {
            graficoDeLaImagen.setColor(Color.WHITE);
        } else if (v == verticeEnArrastre | listaVSel.contiene(v)) {
            graficoDeLaImagen.setColor(Color.green);
        } else {
            graficoDeLaImagen.setColor(Color.red);
        }
        graficoDeLaImagen.fillRect(getCi().x + (int) v.getX() + 2, getCi().y + (int) v.getY() + 2, v.width - 4, v.height - 4);
        if (graficoDeLaImagen.getColor() == Color.white) {
            graficoDeLaImagen.setColor(Color.black);
        } else {
            graficoDeLaImagen.setColor(Color.white);
        }

        graficoDeLaImagen.setFont(fuenteDeLetraParaElVertice);
        graficoDeLaImagen.drawString(v.getNombreVertice(), getCi().x + (int) +(v.getCenterX() - (metricaFuenteVertice.stringWidth(v.getNombreVertice()) / 2)), getCi().y + (int) v.getCenterY() + 5);
    }

    synchronized void updateGraphics() {//se crea nuevamente el grafico\
        if (graficoDeLaImagen != null) {
            graficoDeLaImagen.setColor(colorFondo);
            graficoDeLaImagen.fillRect(0, 0, componenteParaDibujar.getWidth(), componenteParaDibujar.getHeight());
        }

        boolean previews = dibujarInmediato;
        dibujarInmediato = false;
        if (isDibujarCuadricula()) {
            drawGrid();
        }

        if (grafo.cab != null) {
            Vertice vtmp = null;
            vtmp = grafo.cab;
            while (vtmp != null) {
                Adyacente atmp = vtmp.listaAdyacenciaSaliente;
                while (atmp != null) {
                    if (!(vtmp == vinicio && atmp.vertice == vfin)) {
                        agregarAdyacenteGrafico(vtmp, atmp.vertice, atmp.dirigido);
                    }
                    atmp = atmp.sig;
                }
                vtmp = vtmp.siguienteVerticeEnLista;
            }
            ////dibujo la linea de seleccion de corte de ultimo
            if (vinicio != null) {
                agregarAdyacenteGrafico(vinicio, vfin, dirigidaLineaRepintada);
            }
            /////
            vtmp = grafo.cab;
            while (vtmp != null) { //dibujo todos los vertices  
                dibujarVertice(vtmp);
                vtmp = vtmp.siguienteVerticeEnLista;
            }
        }
        if (cuadroSeleccion != null) {
            graficoDeLaImagen.setStroke(new BasicStroke(3));
            graficoDeLaImagen.setColor(Color.lightGray);
            graficoDeLaImagen.drawRect(cuadroSeleccion.x + ci.x, cuadroSeleccion.y + ci.y, cuadroSeleccion.width, cuadroSeleccion.height);
        }

        dibujarInmediato = previews;
    }

    void drawGrid() {
        Font fv2 = new java.awt.Font("Arial", 0, 10);
        int sep = 50;
        graficoDeLaImagen.setStroke(new BasicStroke(1));
        graficoDeLaImagen.setColor(Color.PINK);
        int modx = ci.x % sep;
        int mody = ci.y % sep;

        graficoDeLaImagen.setFont(fv2);
        for (int x = modx; x < componenteParaDibujar.getWidth(); x += sep) {
            graficoDeLaImagen.drawLine(x, 0, x, componenteParaDibujar.getHeight());
            graficoDeLaImagen.drawString(String.valueOf((-ci.x) + x), x, 10);
            for (int y = mody; y < componenteParaDibujar.getHeight(); y += sep) {
                graficoDeLaImagen.drawLine(0, y, componenteParaDibujar.getWidth(), y);
            }
        }
        for (int y = mody; y < componenteParaDibujar.getHeight(); y += sep) {
            graficoDeLaImagen.drawString((-ci.y + y) + "", 0, y);
        }

        graficoDeLaImagen.drawString(ci.x + "," + ci.y, componenteParaDibujar.getWidth() / 2, componenteParaDibujar.getHeight() / 2);
    }

    void repaint() {//se redibuja la imagen guardada
        if (graficoDelComponente == null) {
            return;
        }
        if (dibujarInmediato) {
            graficoDelComponente.drawImage(imagenParaDibujar, 0, 0, null);
        }
    }

    private boolean isIntersectVertice(Vertice v) {
        if (grafo.cab != null) {
            Vertice tcab = grafo.cab;
            while (tcab != null) {
                try {
                    if (tcab != v && v.intersects(tcab)) {
                        return true;
                    }
                } catch (Exception e) {
                }
                tcab = tcab.siguienteVerticeEnLista;
            }
        }
        return false;
    }

    private Point getCoordenadasBorde(Vertice v1, Vertice v2) {
        int borde = 3;
        //primero hallo el angulo de inclinacion de la linea
        double tang = Math.abs((v2.getCenterY() - v1.getCenterY()) / (v2.getCenterX() - v1.getCenterX()));
        double anchoangulo = ((v2.getHeight() / 2) / tang);
        double var = 0;

        long x2 = 0, y2 = 0;//coordenadas finales
        if (anchoangulo >= (v2.width / 2)) {
            if (v1.getCenterX() <= v2.getCenterX() & v1.getCenterY() >= v2.getCenterY()) {//si el vertice 1 esta al SurOeste
                x2 = (int) (v2.getMinX() - borde);
                var = tang * (v2.getCenterX() - x2);
                y2 = (int) (v2.getCenterY() + var);
                //   System.out.println("mayor suroeste");
            } else if (v1.getCenterX() <= v2.getCenterX() & v1.getCenterY() <= v2.getCenterY()) {//NOROESTE
                x2 = (int) (v2.getMinX() - borde);
                var = tang * (v2.getCenterX() - x2);
                y2 = (int) (v2.getCenterY() - var);
                //  System.out.println("mayor Noroeste");
            } else if (v1.getCenterX() >= v2.getCenterX() & v1.getCenterY() <= v2.getCenterY()) {//NORESTE
                x2 = (int) (v2.getMaxX() + borde);
                var = tang * (x2 - v2.getCenterX());
                y2 = (int) (v2.getCenterY() - var);
                //  System.out.println("mayor NorEste");
            } else {//SurEste
                x2 = (int) (v2.getMaxX() + borde);
                var = tang * (x2 - v2.getCenterX());
                y2 = (int) (v2.getCenterY() + var);
                //  System.out.println("mayor SurEste");
            }
        } else {
            if (v1.getCenterX() <= v2.getCenterX() & v1.getCenterY() >= v2.getCenterY()) {//si el vertice 1 esta al SurOeste
                y2 = (int) (v2.getMaxY() + borde);
                var = ((y2 - v2.getCenterY()) / tang);
                x2 = (int) (v2.getCenterX() - var);
                //  System.out.println("a tamaño SurOeste");
            } else if (v1.getCenterX() <= v2.getCenterX() & v1.getCenterY() <= v2.getCenterY()) {//noroeste
                y2 = (int) (v2.getMinY() - borde);
                var = ((y2 - v2.getCenterY()) / tang);
                x2 = (int) (v2.getCenterX() + var);
                //  System.out.println("a tamaño NorOeste");
            } else if (v1.getCenterX() >= v2.getCenterX() & v1.getCenterY() <= v2.getCenterY()) {//NORESTE
                y2 = (int) (v2.getMinY() - borde);
                var = ((y2 - v2.getCenterY()) / tang);
                x2 = (int) (v2.getCenterX() - var);
                //  System.out.println("a tamaño NorEste");
            } else {//Sur Este
                y2 = (int) (v2.getMaxY() + borde);
                var = ((y2 - v2.getCenterY()) / tang);
                x2 = (int) (v2.getCenterX() + var);
                //  System.out.println("a tamaño SurEste");
            }
        }
        return new Point((int) x2, (int) y2);
    }

    void agregarAdyacenteGrafico(Vertice v1, Vertice v2, boolean dirigido) {
        //si hay un grafo de expansion minima
        if (grafoExpansion != null) {
            //y la linea de adyacencia no esta en el grafo de expansion minima
            if (grafoExpansion.isVertice2AdyacenteVertice1(v1.getNombreVertice(), v2.getNombreVertice())) {
                graficoDeLaImagen.setColor(colorExpansion);//especifico que la linea tendra el color del metodo de expansion
                //return;
            } else {
                if (mostrarSoloExpansion) {
                    return;
                }
            }
        }
        Point pf = null;//punto final
        if (dirigido == true) {
            pf = getCoordenadasBorde(v1, v2);
        } else {
            pf = new Point((int) v2.getCenterX(), (int) v2.getCenterY());
        }
        java.awt.Graphics2D g2d = (java.awt.Graphics2D) graficoDeLaImagen;
        BasicStroke bs = new BasicStroke(1f);
        g2d.setStroke(bs);
        if (graficoDeLaImagen.getColor() == colorExpansion) {
            //si el color de dibujo es de la expansion minima es porque esta linea
            //hace parte del grafo de expansion
            bs = new BasicStroke(3f);//dibujo la expansion mas fuerte
            g2d.setStroke(bs);
        } else if (listaResaltados.estaObjeto1AntesObjeto2(v1, v2)) {
            graficoDeLaImagen.setColor(Color.white);
            bs = new BasicStroke(3f);
            g2d.setStroke(bs);
        } else if (v1 == vinicio && v2 == vfin) {
            graficoDeLaImagen.setColor(Color.CYAN);
            bs = new BasicStroke(3f);
            g2d.setStroke(bs);
        } else if (dirigido == false) {
            graficoDeLaImagen.setColor(Color.white);
        } else {
            graficoDeLaImagen.setColor(Color.green);
        }
        if (v1 == verticeEnArrastre) {
            graficoDeLaImagen.setColor(Color.yellow);
        } else if (v2 == verticeEnArrastre) {
            graficoDeLaImagen.setColor(Color.ORANGE);
        }

        graficoDeLaImagen.drawLine(getCi().x + (int) (v1.getCenterX()), getCi().y + (int) (v1.getCenterY()),
                getCi().x + pf.x, getCi().y + pf.y);
        if (graficoDeLaImagen.getColor() == Color.CYAN) {
            graficoDeLaImagen.setColor(Color.YELLOW);
            graficoDeLaImagen.setFont(new Font("Arial black", Font.BOLD, 17));
            FontMetrics mpeso = graficoDeLaImagen.getFontMetrics();
            Rectangle2D medidas = mpeso.getStringBounds((int) peso_adyacencia + "", graficoDeLaImagen);
            graficoDeLaImagen.drawString((int) peso_adyacencia + "", (int) ((v1.getCenterX() + v2.getCenterX()) / 2) + ci.x - ((int) (medidas.getWidth() / 2)),
                    (int) ((v1.getCenterY() + v2.getCenterY()) / 2) + ci.y);
        }
        graficoDeLaImagen.setColor(Color.MAGENTA);

        if (dirigido == false) {
            repaint();
            return;
        }

        //Para dibujar la fecha..
        //Ubicamos el final de la flecha fuera del rectangulo
        double ang = 0.0, angSep = 0.0;
        double tx, ty;
        int tamPunta = 0;

        Point punto1 = new Point((int) v1.getCenterX(), (int) v1.getCenterY());
        Point punto2 = new Point(pf.x, pf.y);
        //tamaño de la punta de la flecha
        tamPunta = 15;
        /* (la coordenadas de la ventana es al revez)
         calculo de la variacion de "x" y "y" para hallar el angulo
         **/
        ty = -(punto1.y - punto2.y) * 1.0;
        tx = (punto1.x - punto2.x) * 1.0;
        //angulo
        ang = Math.atan(ty / tx);
        if (tx < 0) {// si tx es negativo aumentar 180 grados
            ang += Math.PI;
        }
        //puntos de control para la punta
        //p1 y p2 son los puntos de salida
        Point p1 = new Point(), p2 = new Point(), punto = punto2;
        //angulo de separacion
        angSep = 20.0;
        p1.x = (int) (punto.x + tamPunta * Math.cos(ang - Math.toRadians(angSep)));
        p1.y = (int) (punto.y - tamPunta * Math.sin(ang - Math.toRadians(angSep)));
        p2.x = (int) (punto.x + tamPunta * Math.cos(ang + Math.toRadians(angSep)));
        p2.y = (int) (punto.y - tamPunta * Math.sin(ang + Math.toRadians(angSep)));
        //dibuja la linea de extremo a extremo
        //g.drawLine(punto1.x, punto1.y, punto.x, punto.y);
        //dibujar la punta
        int[] pun1 = new int[]{getCi().x + punto.x, getCi().x + p1.x, getCi().x + p2.x};
        int[] pun2 = new int[]{getCi().y + punto.y, getCi().y + p1.y, getCi().y + p2.y};
        graficoDeLaImagen.fillPolygon(pun1, pun2, 3);

        repaint();
    }

    void interfazTeclaPulsada(int tecla) {
        if (tecla == KeyEvent.VK_DELETE) {
            if (!listaVSel.isVacia()) {
                BSNodeList v = listaVSel.getPtr();
                while (v != null) {
                    grafo.eliminarVertice(((Vertice) v.getNodeValue().getObj()).getNombreVertice());
                    v = v.getSig();
                }
                cuadroSeleccion = null;//si doy clic entonces ya no tengo cuadro de seleccion
                listaVSel.vaciar();//vacio la lista de seleccionados
                enCuadroDeSeleccion = false;
                updateAndRepaint();
            }
        }
    }

    synchronized void interfazClickOIniciarArrastre(int x, int y, int boton) {
        Rectangle cc = new Rectangle((-ci.x) + x, (-ci.y) + y, 10, 10);//un rectangulo con 
        //las coordenadas reales para comprobar con que se intersecta al momento de dar click
        if (boton == 1) {

            if (verticeBajoElMouse != null) {//si hay un vertice debajo del mouse
                if (listaVSel.getTamano() == 1) {//si hay un vertice seleccionado
                    Vertice verticeInicio = (Vertice) listaVSel.getPtr().getNodeValue().getObj();
                    if (verticeInicio != verticeBajoElMouse) {//y el seleccionado no es igual al posicionado
                        grafo.dijkstra(verticeInicio, true);
                        verticeFinalDijkstra = verticeBajoElMouse;
                        visualizarDijkstra();//compruebo el resultado
                        updateAndRepaint();//mando a dibujar todo
                        coordenadaInicialVerticeEnArrastre.x = x;
                        coordenadaInicialVerticeEnArrastre.y = y;
                        verticeEnArrastre = verticeBajoElMouse;
                        //                      }
                    }
                    return;
                }
            }

            //si tengo un cuadro de seleccion y doy click normal
            if (cuadroSeleccion != null) {
                cuadroSeleccion = null;//si doy clic entonces ya no tengo cuadro de seleccion
                listaVSel.vaciar();//vacio la lista de seleccionados
                enCuadroDeSeleccion = false;
                updateGraphics();
                repaint();
            }
            /////para iniciar arrastrar un vertice, o para seleccionar destino del Dijkstra
            if (verticeBajoElMouse != null) {
                coordenadaInicialVerticeEnArrastre.x = x;
                coordenadaInicialVerticeEnArrastre.y = y;
                verticeEnArrastre = verticeBajoElMouse;
                return;
            } else {
                verticeEnArrastre = null;
            }
            /////////para eliminar una linea de adyacencia
            if (vinicio != null && vfin != null) {
                grafo.eliminarAdyacente(vinicio, vfin);
                listaResaltados.vaciar();
                vinicio = vfin = null;
                updateGraphics();
                repaint();
                interfazMovimientoMouse(x, y);//INVOCO EL MISMO METODO, para que busque otra linea
                //en la misma posicion
                return;
            }
            /////////////////////
            verticeEnArrastre = null;
            moverArea = true;//SI NO ENTRO EN NADA Y LLEGA A ESTE PUNTO QUIERE DECIR QUE SE VA A EMPEZAR A MOVER
            //EL AREA GRAFICA
            cm = new Point(x, y);//GUARDO LAS COORDENADAS INICIALES DE MOVIDA....
            componenteParaDibujar.setCursor(new Cursor(Cursor.MOVE_CURSOR));//CAMBIO EL CURSOR
        } else if (boton == 3) {//iniciar cuadro de seleccion
            if (enCuadroDeSeleccion) {//si el cursor esta en el cuadro de seleccion
                //se empiezan a mover los vertices seleccionados
                //System.out.println("mover seleccion");
                cm = new Point(x, y);
            } else {
                if (cuadroSeleccion != null) {//si se da clic derecho y hay un rectangulo de seleccion
                    if (!(cc.intersects(cuadroSeleccion))) {//y no se da clic dentro del cuadro
                        //se empieza otra seleccion
                        coordenadasDeCuadroDeSeleccion = new Point((-ci.x) + x, (-ci.y) + y);
                        cuadroSeleccion = new Rectangle(coordenadasDeCuadroDeSeleccion.x, coordenadasDeCuadroDeSeleccion.y, 0, 0);
                        listaVSel.vaciar();
                        enCuadroDeSeleccion = false;
                    }
                } else {
                    //se inicia el arrastre de un cuadro de seleccion
                    coordenadasDeCuadroDeSeleccion = new Point((-ci.x) + x, (-ci.y) + y);
                    cuadroSeleccion = new Rectangle(coordenadasDeCuadroDeSeleccion.x, coordenadasDeCuadroDeSeleccion.y, 0, 0);
                }
            }
        }
    }

    void interfazArrastre(int x, int y, boolean limitar) {
        limitarArrastre = limitar;

        if (enCuadroDeSeleccion) {
            //  System.out.println("Arrastre en cuadro de seleccion");
            Point copyci = new Point(ci);//copia de las coordenadas de incio
            if (limitar) {
                //cuadro seleccion tiene coordenadas reales, debo converWWtirlas en visuales
                //coordenada real=coordenada visual-inicio
                //coordenada visual=coordenada real+inicio
                //System.out.println((x-cm.x) + "," + (y-cm.y));
                if (x - cm.x < 0) {//si se mueve a la izquierda
                    if (cuadroSeleccion.x + ci.x + (x - cm.x) < 0) {//si el movimiento hace que la coordenada visual
                        //del cuadro de seleccion sea menor que cero
                        ci.x += AJUST_INC;
                        //x=0;
                        cuadroSeleccion.x = -ci.x;
                    }
                } else if (x - cm.x > 0) {
                    if (cuadroSeleccion.x + ci.x + cuadroSeleccion.width + (x - cm.x) > componenteParaDibujar.getWidth()) {
                        ci.x -= AJUST_INC;
                        cuadroSeleccion.x = (componenteParaDibujar.getWidth() - cuadroSeleccion.width) - ci.x;
                        //                    cuadroSeleccion.x=x-ci.x;
                    }
                }
                if (y - cm.y < 0) {
                    if (cuadroSeleccion.y + ci.y + (y - cm.y) < 0) {
                        ci.y += AJUST_INC;
                        //y=0;
                        cuadroSeleccion.y = -ci.y;
                    }
                } else if (y - cm.y > 0) {
                    if (cuadroSeleccion.y + ci.y + cuadroSeleccion.height + (y - cm.y) > componenteParaDibujar.getHeight()) {
                        ci.y -= AJUST_INC;
                        cuadroSeleccion.y = (componenteParaDibujar.getHeight() - cuadroSeleccion.height) - ci.y;
                        //                    cuadroSeleccion.y=y-ci.y;
                    }
                }
            }
            Point corrido = new Point();
            //calculo el movimiento de las coordenadas de incio
            //si en algun eje es cero quiere decir que en ese eje no se agrando el componenteParaDibujar
            //corrido.x=ci.x-copyci.x;
            //corrido.y=ci.y-copyci.y;
            corrido.x = copyci.x - ci.x;
            corrido.y = copyci.y - ci.y;
            if (corrido.x == 0) {//si no hubo movimiento de componenteParaDibujar en el eje x
                cuadroSeleccion.x += (x - cm.x);//muevo al cuadro de seleccion los puntos de diferencia
                corrido.x = x - cm.x;//y especifico cuanto se corrio
            }
            if (corrido.y == 0) {
                cuadroSeleccion.y += (y - cm.y);
                corrido.y = y - cm.y;
            }
//////////////////////////////////////
            BSNodeList scab = listaVSel.getPtr();
            while (scab != null) {
                Vertice v = (Vertice) scab.getNodeValue().getObj();
                v.setAddXY(corrido.x, corrido.y);
                scab = scab.getSig();
            }
            cm.x = x;
            cm.y = y;//se especifican las coordenadas del anterior movimiento
            updateGraphics();
            graficoDeLaImagen.drawString(cuadroSeleccion.x + "," + cuadroSeleccion.y,
                    cuadroSeleccion.x + cuadroSeleccion.width + AJUST_INC + ci.x,
                    cuadroSeleccion.y + ci.y);
            repaint();

        } else if (cuadroSeleccion != null && listaVSel.isVacia()) {//si tengo un cuadro de seleccion, todavia no se han seleccionado los vertices
            //System.out.println("redimensionando cuadro de seleccion");
            if (limitar) {
                if (x < 0) {
                    ci.x += AJUST_INC;
                    x = 0;
                }
                if (y < 0) {
                    ci.y += AJUST_INC;
                    y = 0;
                }
                if (x > componenteParaDibujar.getWidth()) {
                    ci.x -= AJUST_INC;
                    x = componenteParaDibujar.getWidth();
                }
                if (y > componenteParaDibujar.getHeight()) {
                    ci.y -= AJUST_INC;
                    y = componenteParaDibujar.getHeight();
                }
            }
            //coordenada real=coordenada visual-inicio
            //dibujando cuadro de seleccion
            if (x - ci.x <= coordenadasDeCuadroDeSeleccion.x) {
                cuadroSeleccion.x = x - ci.x;
            } else {
                cuadroSeleccion.x = coordenadasDeCuadroDeSeleccion.x;
            }
            cuadroSeleccion.width = Math.abs(x - ci.x - coordenadasDeCuadroDeSeleccion.x);
            if (y - ci.y <= coordenadasDeCuadroDeSeleccion.y) {
                cuadroSeleccion.y = y - ci.y;
            } else {
                cuadroSeleccion.y = coordenadasDeCuadroDeSeleccion.y;
            }
            cuadroSeleccion.height = Math.abs(y - ci.y - coordenadasDeCuadroDeSeleccion.y);//////////////
            updateAndRepaint();
            /////////////////////////////////////////////////////
        } else if (verticeEnArrastre != null) {
            //System.out.println("Arrastre vertice");
            int w = verticeEnArrastre.width / 2;
            int h = verticeEnArrastre.height / 2;
            if (limitar) {
                //DEBO ajustar las coordenadas visuales iniciales del vertice en arrastre
                //porque estoy moviendo el componenteParaDibujar grafica
                if (x - w <= 0) {
                    ci.x += AJUST_INC;
                    coordenadaInicialVerticeEnArrastre.x += AJUST_INC;
                    x = w;
                }
                if (y - h <= 0) {
                    ci.y += AJUST_INC;
                    coordenadaInicialVerticeEnArrastre.y += AJUST_INC;
                    y = h;
                }
                if (x + w >= componenteParaDibujar.getWidth()) {
                    ci.x -= AJUST_INC;
                    coordenadaInicialVerticeEnArrastre.x -= AJUST_INC;
                    x = componenteParaDibujar.getWidth() - w;
                }
                if (y + h >= componenteParaDibujar.getHeight()) {
                    ci.y -= AJUST_INC;
                    coordenadaInicialVerticeEnArrastre.y -= AJUST_INC;
                    y = componenteParaDibujar.getHeight() - h;
                }
            }
            verticeEnArrastre.setXY(x - ci.x - w, y - ci.y - h);
            if (Vertice.adyacenciaPesoReal && grafoExpansion != null) {
                if (metodoExpansion == expansionMinima.Kruskal) {
                    this.setExpansionMinima(grafo.getKruskal());
                } else if (metodoExpansion == expansionMinima.Prim) {
                    this.setExpansionMinima(grafo.getPrim());
                }
            } else {
                updateAndRepaint();
            }
//            arrastre(x-1, y, limitar);
        } else if (moverArea) {
            //System.out.println("Moviendo componenteParaDibujar");
            ci.x += (x - cm.x);
            ci.y += (y - cm.y);
            cm.x = x;
            cm.y = y;
            updateGraphics();
            repaint();
        }

    }

    void interfazTerminarArrastre(int x, int y, int boton) {
        componenteParaDibujar.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        moverArea = false;
        //       System.out.println(listaVSel.isVacia());

        ////////////////////////////////
        Point cics = null;//coordenadas de inicio de cuadro de seleccion ajustadas
        Point cfcs = null;//coordenadas finales del cuadro de seleccion ajustadas
        if (!listaVSel.isVacia()) {//termino un arrastre y no esta la lista vacia
            //guardo las coordenadas iniciales del cuadro de seleccion
            cics = new Point(cuadroSeleccion.x, cuadroSeleccion.y);
            cfcs = new Point(cuadroSeleccion.x + cuadroSeleccion.width, cuadroSeleccion.y + cuadroSeleccion.height);
            //quiere decir que se estaba arrastrando los vertices seleccionados
            //debo verificar si la posicion en donde quedaron no intersecta ningun otro;

            int borde = 10;
            boolean intersectoAlguno = false;
            Vertice v = null;//vertice para comprobar posicion de interseccion
            BSNodeList lv = listaVSel.getPtr();//lista de vertices seleccionados
            while (lv != null) {//recorre todos los vertices seleccionados
                v = (Vertice) lv.getNodeValue().getObj();
                Vertice vc = grafo.cab;
                while (vc != null) {
                    if (v.intersects(vc) && vc != v) {//si el vertice de seleccion intersecta otro vertice
                        //le colocare las coordenadas iniciales
                        intersectoAlguno = true;
                        Point cvi = (Point) lv.getNodeValue().getObjrelacion();
                        v.setXY(cvi.x, cvi.y);
                        break;
                    }
                    vc = vc.siguienteVerticeEnLista;
                }
                lv.getNodeValue().setObjrelacion(new Point((int) v.getX(), (int) v.getY()));
                //lv.setObjrelacion(new Point(v.x, v.y));
                lv = lv.getSig();
            }

            if (intersectoAlguno) {//si alguno en el arrastre intersecto , ajusto el cuadro de selccion
                //reajusto las dimensiones del cuadro de seleccion
                lv = listaVSel.getPtr();//lista de vertices seleccionados
                v = (Vertice) lv.getNodeValue().getObj();
                cics = new Point((int) v.getX(), (int) v.getY());
                cfcs = new Point((int) v.getMaxX(), (int) v.getMaxY());
                while (lv != null) {
                    v = (Vertice) lv.getNodeValue().getObj();
                    if (v.getX() < cics.x) {
                        cics.x = (int) v.getX();
                    }
                    if (v.getY() < cics.y) {
                        cics.y = (int) v.getY();
                    }
                    if (v.getMaxX() > cfcs.x) {
                        cfcs.x = (int) v.getMaxX();
                    }
                    if (v.getMaxY() > cfcs.y) {
                        cfcs.y = (int) v.getMaxY();
                    }
                    v = v.siguienteVerticeEnLista;
                    lv = lv.getSig();
                }
                cuadroSeleccion.x = cics.x - borde;
                cuadroSeleccion.y = cics.y - borde;
                cuadroSeleccion.width = Math.abs(cfcs.x - cics.x) + 2 * borde;
                cuadroSeleccion.height = Math.abs(cfcs.y - cics.y) + 2 * borde;
                updateGraphics();
                repaint();
                ////////////////////////////////
            }
        } else if (cuadroSeleccion != null) {//si hay cuadro de seleccion
            if (grafo.cab != null) {//si hay vertices
                listaVSel.vaciar();
                Vertice v = grafo.cab;

                int bordSel = 10;

                while (v != null) {//comprueba que vertices estan en el cuadro
                    if (v.intersects(cuadroSeleccion)) {
                        listaVSel.insertar(v, new Point((int) v.getX(), (int) v.getY()));//los inserta en la lista de seleccionados
                        //y lo relaciona con las coordenadas de inicio
                        if (listaVSel.getTamano() == 1) {//si se inserto el primero elemento
                            //construyo las coodenadas iniciales del cuadro de seleccion
                            cics = new Point((int) v.getX(), (int) v.getY());
                            cfcs = new Point((int) v.getMaxX(), (int) v.getMaxY());
                        } else {
                            if (v.getX() < cics.x) {
                                cics.x = (int) v.getX();
                            }
                            if (v.getY() < cics.y) {
                                cics.y = (int) v.getY();
                            }
                            if (v.getMaxX() > cfcs.x) {
                                cfcs.x = (int) v.getMaxX();
                            }
                            if (v.getMaxY() > cfcs.y) {
                                cfcs.y = (int) v.getMaxY();
                            }
                        }
                    }
                    v = v.siguienteVerticeEnLista;
                }
                if (listaVSel.getTamano() == 0) {//si no se seleccionaron vertices
                    cuadroSeleccion = null;//elimino de memoria el cuadro de seleccion
                } else {
                    //le coloco las coordenadas ajustadas
                    cuadroSeleccion.x = cics.x - bordSel;
                    cuadroSeleccion.y = cics.y - bordSel;
                    cuadroSeleccion.width = Math.abs(cfcs.x - cics.x + 2 * bordSel);
                    cuadroSeleccion.height = Math.abs(cfcs.y - cics.y + 2 * bordSel);
                }
            }
            updateGraphics();
            repaint();
            return;
        }

        if (verticeEnArrastre == null) {
            return;
        }

        if (grafo.cab != null) {
            Vertice v = grafo.cab;
            while (v != null) {
                if (v != verticeEnArrastre && v.intersects(verticeEnArrastre)) {
                    interfazArrastre(coordenadaInicialVerticeEnArrastre.x, coordenadaInicialVerticeEnArrastre.y, false);//el falso
                    //en la linea, quiere decir que no va a limitar el arrastre fina
                    //es decir lo va a colocar sin ajustes de pantalla
                    if (grafoExpansion == null || (grafoExpansion != null && !mostrarSoloExpansion)) {
                        if (grafo.agregarAdyacencia(verticeEnArrastre, v, pesoAdyacenteGrafico, dibujarDirigido)) {
                            updateGraphics();
                            repaint();
                        }
                    }
                    verticeEnArrastre = null;
                    return;
                }
                v = v.siguienteVerticeEnLista;
            }
        }
        if (!limitarArrastre) {//sino se esta limitando el arrastre
            interfazArrastre(x, y, false);//lo ubica en esta posicion en donde se solto
        }
        verticeEnArrastre = null;
    }
    Vertice vinicio = null, vfin = null;
    double peso_adyacencia = 0;
    boolean dirigidaLineaRepintada = false;
    boolean repintarLineaPosicionada = false;//especifica que la adyacencia de vinicio a vfin sera
    //colocada con otro grosor de linea

    String infoVertice(int x, int y) {
        Rectangle vc = new Rectangle((-ci.x) + x, (-ci.y) + y, 10, 10);
        Vertice v = null;
        if (grafo.cab != null) {
            v = grafo.cab;
            while (v != null) {
                if (v.intersects(vc)) {
                    verticeBajoElMouse = v;
                    vinicio = null;
                    vfin = null;
                    componenteParaDibujar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    return v.toString();
                }
                v = v.siguienteVerticeEnLista;
            }
        }
        verticeBajoElMouse = null;
        return null;
    }

    String interfazMovimientoMouse(int x, int y) {
////////////////////////////////////////////////////////
        if (listaVSel.getTamano() != 0) {//si hay vertices seleccionados con el cuadro de seleccion
            Rectangle cs = new Rectangle((-ci.x) + x, (-ci.y) + y, 10, 10);
            if (cs.intersects(cuadroSeleccion)) {//si esta en el cuadro de seleccion
                enCuadroDeSeleccion = true;//lo dejo especificado
                componenteParaDibujar.setCursor(new Cursor(Cursor.MOVE_CURSOR));//cambio el cursor
                //para que visualice que se puede mover la seleccion
                return listaVSel.getTamano() + " elemento(s) en selección";
            } else {
                enCuadroDeSeleccion = false;

            }
        }
///////////////////////////////////////////////////////
        String msg = infoVertice(x, y);//informacion del vertice en esa posicion
        if (msg != null) {
            return msg; //si esta arriba de un vertice no continua con la comprobacion de posicion
        }/////////////////////////////////////////////////////
        vinicio = null;//vertice de inicio
        vfin = null;//vertice final en la flecha de adyacencia

        Vertice v = null;
        if (grafo.cab != null) {
            if (mostrarSoloExpansion) {
                return "";
            }
            v = grafo.cab;
            while (v != null) {
                if (v.listaAdyacenciaSaliente != null) {
                    Adyacente ady = v.listaAdyacenciaSaliente;
                    while (ady != null) {
                        double xx = (double) (ady.vertice.getCenterX() - v.getCenterX());
                        double m = 0;
                        if (xx != 0) {//si la posicion en x vertical no es cero
                            m = ((double) (ady.vertice.getCenterY() - v.getCenterY()) / xx);//halla la pendiente
                        }
                        double c = v.getCenterY();
                        //y=mx+c
                        //y-mx-c=0
                        //int resultado=Math.abs((int)(y-(m*(x-v.getCenterX()))-c));
                        //en esta linea de codigo se le resta v.getCenterX() porque debe restar cero
                        //en el primer punto
                        int resultado = 0;
                        if (xx == 0 && (x - ci.x == v.getCenterX())) {
                            resultado = 0;
                        } else {
                            resultado = Math.abs((int) ((-ci.y + y) - (m * ((-ci.x) + x - v.getCenterX())) - c));
                        }
                        //distancia de vertice a vertice
                        double dvv = Math.sqrt(Math.pow(v.getCenterX() - ady.vertice.getCenterX(), 2)
                                + Math.pow(v.getCenterY() - ady.vertice.getCenterY(), 2));
                        //distancia de la posicion al vertice 1
                        double dvp = Math.sqrt(Math.pow(v.getCenterX() - (x - ci.x), 2) + Math.pow(v.getCenterY() - (y - ci.y), 2));
                        //distancia de la posicion al vertice 2
                        double dvp2 = Math.sqrt(Math.pow(ady.vertice.getCenterX() - (x - ci.x), 2) + Math.pow(ady.vertice.getCenterY() - (y - ci.y), 2));
                        /**
                         * *****
                         */
                        if (resultado <= 2 && (dvp < dvv) && (dvp2 < dvv)) {//si esta en una linea
                            vinicio = v;
                            vfin = ady.vertice;
                            dirigidaLineaRepintada = ady.dirigido;
                            peso_adyacencia = ady.peso;
                            Image ima = new javax.swing.ImageIcon(getClass().getResource("/co/bs/images/cut_red.png")).getImage();
                            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(ima, new Point(0, 0), "el que sABE");
                            componenteParaDibujar.setCursor(cursor);//new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
                            updateAndRepaint();
                            repintarLineaPosicionada = true;
                            return v.getNombreVertice() + "→" + ady.vertice;
                        }
                        ady = ady.sig;
                    }
                }
                v = v.siguienteVerticeEnLista;
            }
            if (repintarLineaPosicionada) {
                repintarLineaPosicionada = false;
                updateAndRepaint();
            }
        }
        componenteParaDibujar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        return "";
    }

    private void visualizarDijkstra() {
        listaResaltados.vaciar();//vacio la lista de resultados
        BSNodeList cabetiqueta = grafo.listEtiquetas.getPtr();//obtengo la cabeza de las etiquetas del Dijkstra
        while (cabetiqueta != null) {
            Etiqueta etiqueta = (Etiqueta) cabetiqueta.getNodeValue().getObj();//obtengo la etiqueta en casting
            if (etiqueta.vertice == verticeFinalDijkstra) {//si la etiqueta es de el vertice final
                String verticesCamino[] = etiqueta.ruta.split(",");//obtengo en un vector los nombres de la los vertices
                //de la ruta
                for (int i = 0; i < verticesCamino.length; i++) {//recorro el vector
                    listaResaltados.insertar(grafo.buscarVertice(verticesCamino[i]));
                    // busco el vertice y lo inserto en la lista de resaltados
                    // System.out.println(verticesCamino[i]);
                }
                break;
            }
            cabetiqueta = cabetiqueta.getSig();
        }
    }

    JPanel getArea() {
        return componenteParaDibujar;
    }

    void setArea(JPanel area) {
        this.componenteParaDibujar = area;
        this.imagenParaDibujar = area.createImage(area.getWidth(), area.getHeight());
        this.graficoDelComponente = area.getGraphics();
        this.graficoDeLaImagen = (Graphics2D) imagenParaDibujar.getGraphics();
        metricaFuenteVertice = area.getFontMetrics(fuenteDeLetraParaElVertice);
        updateAndRepaint();
    }

    Image getTmp() {
        return imagenParaDibujar;
    }
    BSList objetosrevisado = null;

    void organizarPorAnchura(String nombre) {
        //quito los datos de un cuadro en seleccion
        listaVSel.vaciar();
        cuadroSeleccion = null;
        enCuadroDeSeleccion = false;
        ///////////////////////
        //Vertice inicio=buscarVertice(nombre);
        int xr = 0; //es el lugar en x donde va a empezar nuevamente el recorrido
        int xmax = 0;//es el valor de la maxima posicion en donde se ha colocado un vertice

        Vertice inicio = null;
        if (nombre.trim().equals("")) {
            inicio = grafo.cab;
        } else {
            inicio = grafo.buscarVertice(nombre);
        }
        if (inicio == null) {
            return;
        }
        Point cfila = new Point(0, 0);

        //lista de vertices que llegan;
        BSList listallegados = new BSList();//son los vertices que llegan al vertice que se esta recorriendo
        ///lista de vertices sin relaciones;
        ///lista de vertices sin grado de entrada
        objetosrevisado = new BSList();
        BSList listavertices = new BSList();//necesito una lista de vertices
        listavertices.insertar(inicio);//convierto el Vertice actual en una lista de vertices
        objetosrevisado.insertar(inicio);

        final int incx = 60;//separacion en x
        final int incy = 90;

        Vertice inicio_arco = null;//variable que guarda el primer vertice del arco evaluado
        //se entiene por arco la cantidad de aristas que se encuentra de un nodo inicial

        //esto es el recorrido de un vertice en anchura
        Vertice vlle = null;//este vertice es el primer vertice llegado para continuar la anchura
        //extra despues de terminar la anchura total de un vertice

        do {
            while (!listavertices.isVacia()) {
                Vertice veract = (Vertice) listavertices.extraerPrimero().getNodeValue().getObj();
                if (inicio_arco == veract) {
                    cfila.x = xr;
                    cfila.y += incy;
                    veract.setXY(cfila.x, cfila.y);
                    cfila.x += veract.width + incx;
                    inicio_arco = null;

                } else {
                    veract.setXY(cfila.x, cfila.y);
                    cfila.x += veract.width + incx;
                }
                if (cfila.x > xmax) {
                    xmax = cfila.x;
                }

                agregarLlegados(listallegados, veract, objetosrevisado, cfila.y - incy);//agrego en una lista de llegados
                //los llegados de un vertice, si no se encuentran en la otra lista de revisados
                ////////////////////
                Adyacente ady = veract.listaAdyacenciaSaliente;
                while (ady != null) {
                    if (objetosrevisado.buscar(ady.vertice) == null) {
                        //insertamos en los revisado
                        objetosrevisado.insertar(ady.vertice);
                        System.out.println("No encontrado e Insertado " + ady.vertice.getNombreVertice());
                        //eliminamos de los llegados\

                        listallegados.eliminar(ady.vertice);
                        listavertices.insertar(ady.vertice);

                        if (inicio_arco == null) {
                            inicio_arco = ady.vertice;
                        }
                    }
                    ady = ady.sig;
                }
            }//en este momento la lista de vertices no tiene objetos
            //es decir que ya termino la anchura del vertice pasado como parametro
            ////////////////////////////////////////////

            BSNodeList nv = null;
            vlle = null;//no hay vertice insertado porque todavia no se ha extraido
            boolean sw = true;//repeticion activa
            do {
                nv = listallegados.extraerPrimero();//saco el primer Nodo llegado, con el fin de obtener
                //la posicion en y guardada
                if (nv != null) {//si habia nodo
                    vlle = (Vertice) nv.getNodeValue().getObj();//obtengo el objeto del nodo
                    cfila.y = (int) nv.getNodeValue().getRec();//y la posicion para empezar a colocar el vertice
                    if (objetosrevisado.buscar(vlle) == null) {//si este vertice no ha sido revisado
                        listavertices.insertar(vlle);//lo inserto en la lista de vertices de la anchura actual,
                        //y vendria siendo el primer vertice
                        objetosrevisado.insertar(vlle);//lo inserto en la lista de vertices visitados
                        xr = xmax;
                        cfila.y -= 70;
                        inicio_arco = vlle;
                        sw = false;
                        System.out.println("Vertice  para recorrer ahora " + vlle.getNombreVertice());
                    }

                } else {
                    sw = false;
                }
            } while (sw == true);//y voy saca de los llegados hasta que el que saque no haya
            //sido revisado
        } while (vlle != null);

        Vertice v = grafo.cab;
        int rec = 0;
        cfila.x = xmax + incx;
        while (v != null) {
            if (v.sinRelaciones()) {
                rec++;
                if (rec == 5) {
                    cfila.y += incx;
                    rec = 0;
                    cfila.x = xmax + incx;
                }
                v.setXY(cfila.x, cfila.y);
                cfila.x += v.width + incx;

            }
            v = v.siguienteVerticeEnLista;
        }

        ci.x = 0;
        ci.y = 0;
        updateGraphics();
        repaint();
    }

    private boolean agregarLlegados(BSList listallegados, Vertice v, BSList objetosrevisado, int posy) {
        boolean a = false;
        if (v.listaAdyacenciaEntrante != null) {
            Adyacente ady = v.listaAdyacenciaEntrante;
            while (ady != null) {
                if (objetosrevisado.buscar(ady.vertice) == null) {
                    if (listallegados.insertarSinRepetir(ady.vertice, posy)) {
                        System.out.println(ady.vertice.getNombreVertice());
                    }
                    a = true;//se agrego al menos uno
                }
                ady = ady.sig;
            }
        }
        return a;
    }

    Point getCi() {
        return ci;
    }

    void setCi(Point ci) {
        this.ci = ci;
        updateGraphics();
        repaint();
    }

    void moverXinicial(int vxrelativo) {
        ci.x = ci.x + vxrelativo;
        updateGraphics();
        repaint();
    }

    void moverYinicial(int vyrelativo) {
        ci.y = ci.y + vyrelativo;
        updateGraphics();
        repaint();
    }

    boolean isDibujarCuadricula() {
        return dibujarCuadricula;
    }

    void setDibujarCuadricula(boolean dibujarCuadricula) {
        this.dibujarCuadricula = dibujarCuadricula;
        updateGraphics();
        repaint();
    }

    boolean isDibujarinmediato() {
        return dibujarInmediato;
    }

    void setDibujarinmediato(boolean dibujarinmediato) {
        this.dibujarInmediato = dibujarinmediato;
    }

    void updateAndRepaint() {
        updateGraphics();
        repaint();
    }

    void setExpansionMinima(Grafo expansionMinima) {
        this.grafoExpansion = expansionMinima;
        /*   if (kruskal!=null){
         cambiarColor.start();
         }else{
         cambiarColor.stop();
         updateAndRepaint();
         }*/
        updateAndRepaint();
    }
    javax.swing.Timer cambiarColor = new javax.swing.Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            colorExpansion = new Color((int) (Math.random() * 250), (int) (Math.random() * 250), (int) (Math.random() * 255));
            updateAndRepaint();
        }
    });

    public void setMostrarSoloExpansion(boolean mostrar_solo_expansion) {
        this.mostrarSoloExpansion = mostrar_solo_expansion;
        updateAndRepaint();
    }

    public expansionMinima getMetodoExpansion() {
        return metodoExpansion;
    }

    public void setMetodoExpansion(expansionMinima metodoExpansion) {
        this.metodoExpansion = metodoExpansion;
    }
}