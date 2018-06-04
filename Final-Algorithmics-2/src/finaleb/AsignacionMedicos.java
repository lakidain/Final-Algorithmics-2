/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finaleb;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Ander
 */
public class AsignacionMedicos {

    //Ficheros
    private static final String OUTPUT_FILENAME_D = "ADP00_Distance100M100P.txt";
    private static final String OUTPUT_FILENAME_P = "ADP00_Price100M.txt";

    //Datos del problema
    public static int numPacientes = 100;    //numero de pacientes que habra, tambien deberia haberse metido en fichero porque si no cuadrara el problema no tira
    public static int numMedicos = 100;  //numero de medicos que habra
    public static int pacMax = 3;   //pacientes maximos que podra atender cada medico
    public static double pondCosteCont = 1;    //Segun se quiere ponderar mas el coste o la distancia, ahora mismo estan al 100% ambas
    public static double pondDistancia = 1;
    public static int rangoprecio = 10000;
    public static int rangodistancias = 100;
    public static int[][] distancias = new int[numPacientes][numMedicos]; //i van a ser pacientes y j medicos, matriz de distancias, cuanta hay entre cada medico y cada paciente
    public static int[] precio = new int[numMedicos];   //array de cuanto cuesta cada medico
    public static int numHilos = 5;

    //Datos para enfriamiento simulado
    public static double temp;  //temperatura inicial
    public static double coolingRate = 0.10;    //porcentaje para la velocidad de enfriamiento

    //Datos para genético
    public static boolean printNewChildren = false;
    public static final int POPULATION_SIZE = 100;
    public static final int NUM_EVOLUTION_ITERATIONS = 10000;
    public static double TOURNAMENT_SIZE_PCT = 0.1;
    public static int TOURNAMENT_SIZE = (int) (POPULATION_SIZE * TOURNAMENT_SIZE_PCT);
    public static double MUTATION_RATE = 0.5;
    public static double CLONE_RATE = 0.01;
    public static double ELITE_PERCENT = 0.1;
    public static double ELITE_PARENT_RATE = 0.1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        Enfriamiento[] listaHilos = new Enfriamiento[numHilos];   //creamos un array de hilos

        for (int i = 0; i < numHilos; i++) {    //guardamos las referencias a los hilos
            String nombreHilo = "Hilo: " + i;
            Enfriamiento hilo = new Enfriamiento(nombreHilo);
            listaHilos[i] = hilo;
        }

        try {
            readDistance();
        } catch (IOException e) {
            System.err.println(e);
        }
        try {
            readCost();
        } catch (IOException e) {
            System.err.println(e);
        }

        imprimirDatos();
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        System.out.println("\t\t\t\t\tENFRIAMIENTO SIMULADO");
        double start = System.currentTimeMillis();  //medimos el tiempo que tarda en ejecutarse en ms
        for (int i = 0; i < numHilos; i++) {    //ejecutamos los hilos por orden
            listaHilos[i].start();
        }
        double stop = System.currentTimeMillis();
        System.out.println("Tiempo de ejecucion:" + (stop - start) + "ms"); //el tiempo de ejecucion se vera afectado por los sleep

        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        System.out.println("\t\t\t\t\tALGORITMO GENETICO");
        start = System.currentTimeMillis();
        genetico(); //ejecutamos el algoritmo genetico
        stop = System.currentTimeMillis();
        System.out.println("Tiempo de ejecucion:" + (stop - start) + "ms");

        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        System.out.println("\t\t\t\t\tALGORITMO MEMETICO");
        start = System.currentTimeMillis();
        memetico(); //ejecutamos el algoritmo genetico
        stop = System.currentTimeMillis();
        System.out.println("Tiempo de ejecucion:" + (stop - start) + "ms");
    }

    //Metodos relativos al algoritmo de enfriamiento simulado y memetico
    public static void checkEnfriamientoParameterErrors() { //esto es para comprobar si los parametros que tenemos estan bien, no puedes hacer un torneo con mas individuos que la poblacion por ejemplo
        boolean error = false;

        if (AsignacionMedicos.numPacientes > AsignacionMedicos.numMedicos * AsignacionMedicos.pacMax) {
            System.err.println("ERROR: You must put more doctors in the system");
            error = true;
        }
        if (error == true) {
            System.exit(1);
        }
    }

    public static double getValor(Individual individuo) {   //obtener el valor de un individuo
        return individuo.getCost();
    }

    public static double acceptanceProbability(double energy, double newEnergy, double temperature) {   //funcion de probabilidad para el enfriamiento simulado
        // If the new solution is better, accept it
        if (newEnergy < energy) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        double resultado = Math.exp((energy - newEnergy)*10 / temperature); //cuando nos encontremos con una solucion invalida la probabilidad sera practicamente 0
        return resultado;    //return Math.exp((energy - newEnergy) / temperature); si lo dejabamos como antes nos vamos a explorar siempre las rutas peores porque seria en el numerador un numero positivo
    }

    //Metodos relativos al algoritmo genetico
    public static void genetico(){ //Si la convergencia es muy rapida es porque hay pocos medicos/pacientes entonces converge casi en la primera iteracion
        checkGeneticoParameterErrors(); //Función que tirara un error y saldra si los parametros fallan
        Population pop = new Population();   //crea la poblacion
        pop.initializePopulationRandomly(POPULATION_SIZE);  //inicia toda la primera poblacion
        Individual aux = new Individual(pop.getBestIndividualInPop().binario,pop.getBestIndividualInPop().asignacion);
        int contador = 0;
        int contadorBest = 0;
        for (int i = 0; i < NUM_EVOLUTION_ITERATIONS; i++) {   //cuantas generaciones vamos a ir cambiando 
            pop = pop.evolve(); //te devuelve la poblacion final          
            if (i % 100 == 0) {
                System.out.println("Finished Iteration: " + i + ". Best Solution: " + pop.getBestIndividualInPop());    //imprime cada 3 generaciones la solucion
            }
            if (pop.getBestIndividualInPop().getCost() < aux.getCost()) {
                aux = pop.getBestIndividualInPop();
                contadorBest = contador;
            }
            contador++;
        }
        System.out.println("BEST SOLUTION:");   //mejor solucion final
        System.out.println(pop.getBestIndividualInPop());
        System.out.println("La ultima mejora se encontro en la iteracion " + contadorBest);
    }

    public static void checkGeneticoParameterErrors() { //esto es para comprobar si los parametros que tenemos estan bien, no puedes hacer un torneo con mas individuos que la poblacion por ejemplo
        boolean error = false;
        if (POPULATION_SIZE < TOURNAMENT_SIZE) {
            System.err.println("ERROR: Tournament size must be less than population size.");
            error = true;
        }
        if (POPULATION_SIZE < 0) {
            System.err.println("ERROR: Population size must be greater than zero");
            error = true;
        }
        if (TOURNAMENT_SIZE < 0) {
            System.err.println("ERROR: Tournament size must be greater than zero");
            error = true;
        }
        if (AsignacionMedicos.numPacientes > AsignacionMedicos.numMedicos * AsignacionMedicos.pacMax) {
            System.err.println("ERROR: You must put more doctors in the system");
            error = true;
        }
        if (error == true) {
            System.exit(1);
        }
    }

    //Metodos relativos al algoritmo memetico
    public static void memetico(){ //Si la convergencia es muy rapida es porque hay pocos medicos/pacientes entonces converge casi en la primera iteracion
        //Local varibles for Local Search (Memetic)
        int count = 0;  //generacion actual por la que estamos
        int genLocalSearch = 10;    //cada cuantas generaciones vamos a aplicar la busqueda local
        float rateLocalSearch = 0.15F;  //porcentaje de individuos sobre los que va a actuar
        boolean best = true;    //solo actuara sobre el 15% de los mejores individuos

        Population pop = new Population();   //crea la poblacion
        pop.initializePopulationRandomly(POPULATION_SIZE);  //inicia toda la primera poblacion
        Individual aux = new Individual(pop.getBestIndividualInPop().binario, pop.getBestIndividualInPop().asignacion);
        int contador = 0;
        int contadorBest = 0;
        for (int i = 0; i < NUM_EVOLUTION_ITERATIONS; i++) {   //cuantas generaciones vamos a ir cambiando 
            pop = pop.evolve(); //te devuelve la poblacion final
            pop = LocalSearch(count, genLocalSearch, rateLocalSearch, best, pop);
            if (i % 100 == 0) {
                pop.getBestIndividualInPop();
                System.out.println("Finished Iteration: " + i + ". Best Solution: " + pop.getBestIndividualInPop());    //imprime cada 3 generaciones la solucion
                count++;
            }
            if (pop.getBestIndividualInPop().getCost() < aux.getCost()) {
                aux = pop.getBestIndividualInPop();
                contadorBest = contador;
            }
            contador++;
        }
        System.out.println("BEST SOLUTION:");   //mejor solucion final
        System.out.println(pop.getBestIndividualInPop());
        System.out.println("La ultima mejora se encontro en la iteracion " + contadorBest);
    }

    private static Population LocalSearch(int generation, int genLocalSearch, float rateLocalSearch, boolean best, Population population){  //metodo para aplicar la mejora a los individuos
        int i;
        Individual c;
        Random rand = new Random();

        if (generation % genLocalSearch == 0) { // Aplica búsqueda local en determinadas generaciones
            if (best) { //Aplica búsqueda local sobr los mejores
                for (i = 0; i < rateLocalSearch * POPULATION_SIZE; i++) {
                    c = population.individuals.get(i);
                    c = enfriamientoSimuladoMemetico(c);
                    c.calculateCost();
                }
            } else { //Aplica búsqueda local sobre toda la población usando la ratio de búsqueda local 
                for (i = 0; i < POPULATION_SIZE; i++) {
                    if (rand.nextFloat() < rateLocalSearch) {
                        c = population.individuals.get(i);
                        c = enfriamientoSimuladoMemetico(c);
                        c.calculateCost();
                    }
                }
            }
        }
        return population;
    }

    public static Individual enfriamientoSimuladoMemetico(Individual currentSolution){  //el proceso es el mismo que el explicado en la clase destinada al enfriamiento simulado
        temp = 1000000000;
        checkEnfriamientoParameterErrors();
        Individual best = new Individual(currentSolution.binario,currentSolution.asignacion);

        int anteriorTirada = 0;

        // Loop until system has cooled
        while (temp > 1) {

            Individual nuevaSolucion;

            Random numero = new Random();
            int nuevaTirada = numero.nextInt(numMedicos);

            while (nuevaTirada == anteriorTirada) {
                nuevaTirada = numero.nextInt(numMedicos);
            }
            anteriorTirada = nuevaTirada;

            nuevaSolucion = new Individual(currentSolution.binario,currentSolution.asignacion);
            nuevaSolucion.cambio(nuevaTirada);

            // Get energy of solutions
            double currentEnergy = getValor(currentSolution);
            double neighbourEnergy = getValor(nuevaSolucion);

            // Decide if we should accept the neighbour
            if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
                currentSolution = new Individual(nuevaSolucion.binario,nuevaSolucion.asignacion);
            }

            // Keep track of the best solution found
            if (getValor(currentSolution) < getValor(best)) {
                best = new Individual(currentSolution.binario,currentSolution.asignacion);
            }

            // Cool system
            temp = temp * (1 - coolingRate);    //temp *= 1 - coolingRate;
        }
        return best;
    }

    //Metodos generales
    public static void readCost() throws IOException {  //lee el array de coste del fichero
        BufferedReader br = new BufferedReader(new FileReader(OUTPUT_FILENAME_P));
        StringBuilder build = new StringBuilder();

        while (!build.append(br.readLine()).toString().equalsIgnoreCase("null")) {
            String[] tokens = build.toString().split(" ");
            for (int i = 0; i < numMedicos; i++) {
                precio[i] = Integer.parseInt(tokens[i]);
            }
            build.setLength(0); // Clears the buffer
        }
    }

    public static void readDistance() throws IOException {  //lee la matriz de distancias del fichero
        BufferedReader br = new BufferedReader(new FileReader(OUTPUT_FILENAME_D));
        StringBuilder build = new StringBuilder();
        int currentPaciente = 0;

        while (!build.append(br.readLine()).toString().equalsIgnoreCase("null")) {
            String[] tokens = build.toString().split(" ");
            for (int j = 0; j < numMedicos; j++) {
                distancias[currentPaciente][j] = Integer.parseInt(tokens[j]);
            }
            currentPaciente++;
            build.setLength(0); // Clears the buffer
        }
    }

    public static void imprimirDatos() { //Función para imprimir los datos leidos anteriormente de fichero
        System.out.print("La distancia de cada paciente con cada doctor es:\n");
        for (int x = 0; x < distancias.length; x++) {   //Impresión de la matriz de distancias
            System.out.print("|");
            for (int y = 0; y < distancias[x].length; y++) {
                System.out.print(distancias[x][y]);
                if (y != distancias[x].length - 1) {
                    System.out.print("\t");
                }
            }
            System.out.println("|");
        }

        System.out.print("El coste por medico es:\n");
        for (int x = 0; x < precio.length; x++) {   //Impresión del array de coste por medico
            System.out.print("Medico: " + (x + 1) + " Precio: " + precio[x] + "\n");
        }

    }
}
