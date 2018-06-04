/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finaleb;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Ander
 */
public class Individual implements Cloneable {

    protected ArrayList<Integer> binario;   //Modelamos el problema como si fuera una mochila, tendremos medicos contratados y no contratados
    protected int asignacion[]; //array que va a guardar la asignacion por ejemplo el paciente 0 con que numero de medico ira
    private double cost; //El coste del individuo generado

    public Individual() {   //Inicializamos los parametros 
        this.asignacion = new int[AsignacionMedicos.numPacientes];  //podriamos inicializarlo en publico desde TSP
        this.binario = new ArrayList();
        this.cost = 0;
    }

    public Individual(ArrayList<Integer> binario, int[] asignacion) { //Constructor que simula un clone, tambien podriamos usar un clone
        this.asignacion = new int[AsignacionMedicos.numPacientes];
        this.binario = new ArrayList();
        this.binario.addAll(binario);
        for (int i = 0; i < asignacion.length; i++) {
            this.asignacion[i] = asignacion[i];
        }
        calculateCost();
    }

    public void generateRandomTour() {    //un tour random para nosotros será contratar medicos como si fuera una mochila
        Random rand = new Random();
        boolean valido = false;
        while (!valido) {
            this.binario.clear();
            for (int j = 0; j < AsignacionMedicos.numMedicos; j++) {
                Integer binario = rand.nextInt(2);
                this.binario.add(binario);   //En el array binario o hay un 0 o un 1 
            }
            valido = comprobarValido(this.binario); //si es el primer individuo que generamos controlamos que sea valido, si no lo es repetimos la creacion de la mochila
        }
        asignarPacientes();    //asignamos los pacientes una vez ya esta rellena la mochila
        calculateCost();    //calculamos el coste de la solucion
    }

    public void asignarPacientes() {    //funcion para asignar pacientes
        int auxiliar[] = new int[AsignacionMedicos.numMedicos];
        int medicoLibre = 0;    //el medico libre sera el medico libre y que mejor distancia tenga con nuestro paciente, ademas de que este contratado
        Random rand = new Random();

        int j = 0;

        ArrayList<Integer> nextPossiblePacient = new ArrayList<Integer>();
        for (j = 0; j < AsignacionMedicos.numPacientes; j++) {
            nextPossiblePacient.add(j);
        }

        for (int i = 0; i < AsignacionMedicos.numPacientes; i++) {
            int index = rand.nextInt(nextPossiblePacient.size()); //asignamos los pacientes por insaculacion, mejora con respecto al metodo anterior de asignacion!!
            int paciente = nextPossiblePacient.get(index);
            nextPossiblePacient.remove(index);
            for (j = 0; j < AsignacionMedicos.numMedicos; j++) {
                if (auxiliar[j] < AsignacionMedicos.pacMax && AsignacionMedicos.distancias[paciente][j] < AsignacionMedicos.distancias[paciente][medicoLibre] && this.binario.get(j) == 1) {
                    medicoLibre = j;
                }
            }
            auxiliar[medicoLibre] = auxiliar[medicoLibre] + 1;  //le sumamos al medico el paciente que le vamos a asignar
            this.asignacion[paciente] = medicoLibre;   //le asignamos al paciente el medico
        }
        clean();
    }

    /*public void asignarPacientes() {    //primera funcion propuesta de asignacion
        int auxiliar[] = new int[AsignacionMedicos.numMedicos];
        int medicoLibre = 0;    //el medico libre sera el medico libre y que mejor distancia tenga con nuestro paciente, ademas de que este contratado

        int j = 0;

        for (int i = 0; i < AsignacionMedicos.numPacientes; i++) {
            for (j = 0; j < AsignacionMedicos.numMedicos; j++) {
                if (auxiliar[j] < AsignacionMedicos.pacMax && AsignacionMedicos.distancias[i][j] < AsignacionMedicos.distancias[i][medicoLibre] && this.binario.get(j) == 1) {
                    medicoLibre = j;
                }
            }
            auxiliar[medicoLibre] = auxiliar[medicoLibre] + 1;  //le sumamos al medico el paciente que le vamos a asignar
            this.asignacion[i] = medicoLibre;   //le asignamos al paciente el medico
        }
        clean();
    }*/
    
    public void clean() {   //una vez asignamos a los pacientes limpiamos los medicos que no tienen asignado ningun paciente
        int j = 0;

        for (int i = 0; i < AsignacionMedicos.numMedicos; i++) {
            boolean encontrado = false;
            for (j = 0; j < AsignacionMedicos.numPacientes; j++) {
                if (asignacion[j] == i) {
                    encontrado = true;
                }
            }
            if (!encontrado) {
                this.binario.set(i, 0);
            }
        }
    }

    public boolean comprobarValido(ArrayList<Integer> binario) {   //funcion para comprobar que podemos asignar todos los pacientes a algun medico, podria ser que generaramos una solucion con 0 medicos por ejemplo 
        int numMedicos = 0;
        boolean valido = false;

        for (int j = 0; j < binario.size(); j++) {  //contamos el numero de medicos
            if (binario.get(j) == 1) {
                numMedicos = numMedicos + 1;
            }
        }

        if (AsignacionMedicos.numPacientes <= numMedicos * AsignacionMedicos.pacMax) {  //si se cumple la formula esque hay suficientes medicos para atender a todos los pacientes
            valido = true;
        }

        return valido;
    }

    public void calculateCost() {   //Para calcular el coste miramos en la matriz de coste las asignaciones que esten a 1 
        this.cost = 0;  //reseteamos el coste cada vez que lo vayamos a calcular
        float distancia = 0;
        float costeContratacion = 0;
        for (int i = 0; i < this.binario.size(); i++) { //coste de contratacion de los medicos contratados
            if (this.binario.get(i) == 1) {
                costeContratacion = costeContratacion + (float) AsignacionMedicos.precio[i] / AsignacionMedicos.rangoprecio;  //dividimos por el rango en el que hemos generado para normalizar y tener todo en la misma escala, asi no hay anda que pondere mas que otra cosa
            }
        }
        int medico = 0;
        for (int i = 0; i < this.asignacion.length; i++) {
            medico = this.asignacion[i];    //medico que le corresponde al paciente
            distancia = distancia + (float) AsignacionMedicos.distancias[i][medico] / AsignacionMedicos.rangodistancias;    //la j es el medico y la i el paciente
        }

        //la funcion es la suma, como queremos minimizar ambas la menor en global sera la mejor porque sera la que menos tiene en ambas
        this.cost = AsignacionMedicos.pondCosteCont * costeContratacion + AsignacionMedicos.pondDistancia * distancia; //podemos poner ponderaciones para que una valga mas que la otra

        if (!comprobarValido(this.binario)) {   //en el enfriamiento simulado cabe la posibilidad de una solucion invalida, si lo son les ponemos un coste desproporcionado
            this.cost = 1000000000; //si ponemos Max.Double habria que poner en el evolve <=, no deberia pasar en el genetico pero por si acaso
        }
    }

    public void addContratacion(int medico) {   //funcion para contratar a un medico
        this.binario.add(medico);   //En el array binario o hay un 0 o un 1 
    }

    public int getMedico(int index) {   //funcion para obtener la ciudad
        return binario.get(index);
    }

    public double getCost() {
        return cost;
    }

    public void cambio(int cambio) {  //metodo auxiliar de enfriamiento simulado para cambiar un bit de la solucion

        if (this.binario.get(cambio) == 0) {
            this.binario.set(cambio, 1);
        } else {
            this.binario.set(cambio, 0);
        }
            asignarPacientes();
            calculateCost();//para actualizar el coste}
    }

    public String toString() {  //como vamos a imprimir a un individeuo
        String cadena = "";
        cadena = cadena + "Individual (" + this.cost + "): con ponderaciones de:" + AsignacionMedicos.pondCosteCont + " para el coste de contratacion y "
                + AsignacionMedicos.pondDistancia + " para las distancias";
        cadena = cadena + "\nMedicos contratados: ";
        for (int i = 0; i < this.binario.size(); i++) {
            if (this.binario.get(i) == 1) {
                cadena = cadena + "\nMedico numero: " + i + ",precio:" + AsignacionMedicos.precio[i] + ". ";
            }
        }
        cadena = cadena + "\nPacientes asignados con medico:";
        for (int i = 0; i < this.asignacion.length; i++) {
            cadena = cadena + "\nPaciente numero: " + i + ", con medico:" + this.asignacion[i] + ", distancia para el paciente:" + AsignacionMedicos.distancias[i][this.asignacion[i]];
        }
        cadena = cadena + "\n";
        //build.append("\n");
        return cadena;
    }

    public boolean equals(Individual o) {
        if (this.binario.equals(o.binario) && this.asignacion.equals(o.asignacion)) {
            return true;
        } else {
            return false;
        }
    }

}
