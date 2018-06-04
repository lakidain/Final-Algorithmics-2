/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finaleb;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static finaleb.AsignacionMedicos.acceptanceProbability;
import static finaleb.AsignacionMedicos.checkEnfriamientoParameterErrors;
import static finaleb.AsignacionMedicos.coolingRate;
import static finaleb.AsignacionMedicos.getValor;
import static finaleb.AsignacionMedicos.numMedicos;

/**
 *
 * @author Ander
 */
public class Enfriamiento extends Thread {  //extendemos la clase Thread para poder lanzar multiples hilos

    private Thread t;   //declaramos el Thread
    private String threadName;  //el nombre del Thread

    public Enfriamiento(String name) {  //Metodo constructor para el metodo que usara el Thread
        threadName = name;
        System.out.println("Creating " + threadName);
    }

    public void run() { //sobreescribimos el metodo run() de la clase Thread, realizara todo el experimento

        checkEnfriamientoParameterErrors(); //primero comprobamos si los parametros son correctos
        Individual currentSolution = new Individual();  //solucion valida con la que empezamos
        currentSolution.generateRandomTour();   //generamos un tour random

        System.out.println("Solucion inicial: " + currentSolution); //cuanto es el valor de la solucion inicial

        Individual best = new Individual(currentSolution.binario,currentSolution.asignacion);   //nuestra mejor solucion inicial sera con la que comenzamos
        //atributos auxiliares e inicializacion de la temperatura
        int anteriorTirada = 0; 
        int contador = 0;
        int contadorBest = 0;
        double temp = 1000000000;

        // Loop until system has cooled
        while (temp > 1) {

            Individual nuevaSolucion;

            Random numero = new Random();
            int nuevaTirada = numero.nextInt(numMedicos);

            while (nuevaTirada == anteriorTirada) { //controlamos que no se repita la tirada anterior, para no terminar en el mismo vecino
                nuevaTirada = numero.nextInt(numMedicos);
            }
            anteriorTirada = nuevaTirada;

            nuevaSolucion = new Individual(currentSolution.binario,currentSolution.asignacion);
            nuevaSolucion.cambio(nuevaTirada);  //hemos modelado un vecino como un cambio random de contratacion del medico

            // Get energy of solutions
            double currentEnergy = getValor(currentSolution);
            double neighbourEnergy = getValor(nuevaSolucion);

            // Decide if we should accept the neighbour
            if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {  //evaluamos la funcion de probabilidad
                currentSolution = new Individual(nuevaSolucion.binario,nuevaSolucion.asignacion);   //si entramos aceptamos en nuevo vecino
            }

            // Keep track of the best solution found
            if (getValor(currentSolution) < getValor(best)) {
                best = new Individual(currentSolution.binario,currentSolution.asignacion);
                contadorBest = contador;
            }

            // Cool system
            temp = temp * (1 - coolingRate);    //temp *= 1 - coolingRate;
            contador++;
        }

        System.out.println("Solucion Final: " + best); 
        System.out.println("La ultima mejora se encontro en la iteracion " + contadorBest); 
    }

    public void start() {   //sobreescribimos el metodo start de la clase Thread, sera el encargado de llamar al run()
        System.out.println("Starting " + threadName);
        if (t == null) {
            try {
                t = new Thread(this, threadName);
                t.start();
                sleep(1000);    //sleep de espera para que no se superpongan entre ellos
            } catch (InterruptedException ex) { //excepcion para el sleep
                Logger.getLogger(Enfriamiento.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
