/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finaleb;

/**
 *
 * @author Ander
 */
import static finaleb.AsignacionMedicos.numMedicos;
import java.util.ArrayList;
import java.util.Random;

public class Population {

    public ArrayList<Individual> individuals = new ArrayList<Individual>();
    private ArrayList<Individual> sorted;

    public Population() {
    }

    public void initializePopulationRandomly(int numIndividuals) {  //la primera generacion debe iniciarse, lo hacemos de forma random
        for (int i = 0; i < numIndividuals; i++) {
            Individual ind = new Individual();
            ind.generateRandomTour();
            individuals.add(ind);
        }
    }

    public String toString() {  //para saber como se imprimira la poblacion
        StringBuilder build = new StringBuilder();
        for (Individual ind : individuals) {
            build.append(ind.toString() + "\n");
        }
        return build.toString();
    }

    /*
	 * This is where we create the next generation of the population
	 * Step 1: Select the best fit (elitism)
	 * Step 2: Create offspring given two parents (genetic crossover)
	 * Step 3: Add mutations to some of the new children (mutation)
     */
    public Population evolve() {    //en el evolve hacemos para saber que individuos pasan a la siguiente generacion
        // STEP 1: Select the best fit (elitism)
        sorted = new ArrayList<Individual>();
        Population nextGenPop = new Population();
        int populationSpaceAvailable = individuals.size();

        for (int i = 0; i < AsignacionMedicos.POPULATION_SIZE; i++) { //Aqui ordenamos los individuos de la poblacion de menor a peor coste (nos interesa minimizar)
            double bestCost = Double.MAX_VALUE;
            int bestIndex = -1;
            for (int j = 0; j < individuals.size(); j++) {
                if (individuals.get(j).getCost() < bestCost) {
                    bestCost = individuals.get(j).getCost();
                    bestIndex = j;
                }
            }
            sorted.add(individuals.get(bestIndex));
            individuals.remove(bestIndex);
        }
        // Keep the best individual
        Individual best = new Individual(sorted.get(0).binario, sorted.get(0).asignacion);
        nextGenPop.individuals.add(best);   //el mejor puede mutar en el crossover por eso paso una copia en vez del original, para que el mejor se mantenga y ademas pueda mutar su copia sin afectarle
        --populationSpaceAvailable;
        // Add "top" individuals to the next generation
        int numElite = (int) (AsignacionMedicos.POPULATION_SIZE * AsignacionMedicos.ELITE_PERCENT);
        for (int i = 0; i < numElite + 1; i++) {    //parte de elitismo, el numElite +1 es porque el 0 se mantiene en la lista y quiero no tenerlo en cuenta
            if (i != 0) {   //la posicion 0 (el mejor) no se contempla para la mutacion
                if (Math.random() < AsignacionMedicos.MUTATION_RATE) {
                    best = new Individual(sorted.get(i).binario, sorted.get(i).asignacion);
                    nextGenPop.individuals.add(mutate(best));   //asi mutamos la copia, no el original
                } else {
                    best = new Individual(sorted.get(i).binario, sorted.get(i).asignacion);
                    nextGenPop.individuals.add(best);
                }
                populationSpaceAvailable--;
            }
        }

        // STEP 2: Select 2 parents from population and generate children
        while (populationSpaceAvailable > 0) {  //parte de torneo
            // STEP 2: Create offspring given two parents (genetic crossover)
            Individual p1 = selectParentViaTournament();
            Individual p2 = selectParentViaTournament();
            Individual child = crossover(p1, p2);

            // STEP 3: Add mutations
            if (Math.random() < AsignacionMedicos.MUTATION_RATE) {
                mutate(child);
            }
            Individual auxiliar = new Individual(child.binario, child.asignacion);
            nextGenPop.individuals.add(auxiliar);
            populationSpaceAvailable--;
            if (AsignacionMedicos.printNewChildren == true) {    //Esto esta por si quieres ver la impresion de cada hijo
                System.out.println(child);
            }
        }
        return nextGenPop;
    }

    public Individual mutate(Individual ind) {  //Defino una mutacion como un cambio de bit en medicos contratados, tiene que ser un cambio valido
        boolean valido = false;
        Individual auxiliar = null;
        while (!valido) {
            auxiliar = new Individual(ind.binario, ind.asignacion);
            int indexCambio = (int) (Math.random() * AsignacionMedicos.numMedicos);
            if (auxiliar.binario.get(indexCambio) == 0) {
                auxiliar.binario.set(indexCambio, 1);
            } else {
                auxiliar.binario.set(indexCambio, 0);
            }
            valido = auxiliar.comprobarValido(auxiliar.binario);
        }

        auxiliar.asignarPacientes();
        auxiliar.calculateCost();//para actualizar el coste

        return auxiliar;
    }

    public Individual crossover(Individual p1, Individual p2) { //genera un hijo valido entre el padre y la madre
        Individual child = new Individual();
        boolean valido = false;
        Individual auxiliar = null;

        while (!valido) {
            auxiliar = new Individual(child.binario, child.asignacion);
            int index1 = (int) (Math.random() * AsignacionMedicos.numMedicos);  //genera un indice aleatorio

            // Add the subtour from parent 1
            for (int i = 0; i < index1; i++) {
                auxiliar.addContratacion(p1.getMedico(i));
            }
            // Add the remaining cities from parent 2
            for (int j = index1; j < AsignacionMedicos.numMedicos; j++) {
                auxiliar.addContratacion(p2.getMedico(j));
            }
            valido = auxiliar.comprobarValido(auxiliar.binario);
        }//para actualizar el coste
        child = new Individual(auxiliar.binario, auxiliar.asignacion);;
        child.asignarPacientes();
        child.calculateCost();

        // Small chance of cloning the better parent
        if (Math.random() < AsignacionMedicos.CLONE_RATE) {
            if (p1.getCost() < p2.getCost()) {
                return p1;
            } else {
                return p2;
            }
        }

        return child;
    }

    public Individual selectParentViaTournament() { //selecciona un individuo por torneo
        Random rand = new Random();
        if (rand.nextDouble() < AsignacionMedicos.ELITE_PARENT_RATE) {
            int numElite = (int) (AsignacionMedicos.ELITE_PARENT_RATE * AsignacionMedicos.POPULATION_SIZE);
            return sorted.get(rand.nextInt(numElite));
        }
        ArrayList<Individual> tournamentPopulation = new ArrayList<Individual>();
        for (int i = 0; i < AsignacionMedicos.TOURNAMENT_SIZE; i++) {
            int randIndex = (int) (Math.random() * sorted.size());
            tournamentPopulation.add(sorted.get(randIndex));
        }
        return getBestIndividual(tournamentPopulation);
    }

    /*private Individual rouletteSelection() {    //seleccion por ruleta propuesta en clase
        double totalFitness = 0;
        Random rand = new Random();

        for (int i = 0; i < sorted.size() - 1; i++) { //como estamos trabajando con sorted cogemos su tamaño, en el de la mochila pilla toda la poblacion
            totalFitness += sorted.get(i).getCost();    //entre los que quedan en sorted el fitness total
        }

        //pick the parents based on their percent fitness
        Individual selected = null;
        int check = sorted.size();

        //pick a spot on the roulette (from 0 to 1), and subtract the fractional fitness
        //until we find a roulette-selected parent.             
        double theSpot = rand.nextDouble();
        int j = 0;
        while (j < sorted.size() - 1 && theSpot > 0) {
            theSpot -= sorted.get(j).getCost() / totalFitness;
            j++;
        }
        selected = sorted.get(j);

        return selected;
    }*/

 /*private Individual rouletteSelectionPropio() { //seleccion por ruleta propuesta por mi, referencia usada http://www.aic.uniovi.es/ssii/Tutorial/Seleccion.htm
        double totalFitness = 0;
        Random rand = new Random();

        for (int i = 0; i < sorted.size() - 1; i++) { //como estamos trabajando con sorted cogemos su tamaño, en el de la mochila pilla toda la poblacion
            totalFitness += sorted.get(i).getCost();    //entre los que quedan en sorted el fitness total
        } //lo de arriba es cuando hay que maximizar el problema, si hubiera que minimizarlo habria que hacer totalFitness += (1/sorted.get(i).getCost()); gracias a amdres por la idea :)

        //pick the parents based on their percent fitness
        Individual selected = null;
        int check = sorted.size();
        int numTotal = rand.nextInt((int) totalFitness);    //en vez de int habria que hacerlo con double en caso de minimizar

        //pick a spot on the roulette (from 0 to 1), and subtract the fractional fitness
        //until we find a roulette-selected parent.             
        double total = 0;
        int j = 0;
        while (j < sorted.size() - 1 && numTotal >= total) {
            total += sorted.get(j).getCost();   //si hubiera que minimizar pones total += (1/sorted.get(j).getCost());
            j++;
        }
        selected = sorted.get(j);

        return selected;
    }*/
    public Individual getBestIndividualInPop() {    //si sorted no es null devuelve al individuo con menor fitness
        if (sorted != null) {
            return sorted.get(0);
        }
        return getBestIndividual(this.individuals);
    }

    public Individual getBestIndividual(ArrayList<Individual> pop) {    //encuentra al mejor individuo de la poblacion actual
        double minCost = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < pop.size(); i++) {
            if (pop.get(i).getCost() < minCost) {
                minIndex = i;
                minCost = pop.get(i).getCost();
            }
        }
        return pop.get(minIndex);
    }
}
