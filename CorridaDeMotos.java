import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Competidor extends Thread {
    private final int id;
    private final CorridaDeMotos corrida;

    public Competidor(int id, CorridaDeMotos corrida) {
        this.id = id;
        this.corrida = corrida;
    }

    @Override
    public void run() {
        try {
            Thread.sleep((int)(Math.random() * 1000));
            corrida.iniciarCorrida(id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
};


public class CorridaDeMotos {
    private static int totalCorridas;
    private static int totalCompetidores;
    private static Map<Integer, Integer> placar = new ConcurrentHashMap<>();
    private static final AtomicInteger competidoresNaLinha = new AtomicInteger(0);
    private static final AtomicInteger pontos = new AtomicInteger(10);

    public CorridaDeMotos(int totalCorridas, int totalCompetidores) {
        this.totalCorridas = totalCorridas;
        this.totalCompetidores = totalCompetidores;
    }

    public synchronized void iniciarCorrida(int idCompetidor) throws InterruptedException {
        competidoresNaLinha.incrementAndGet();

        if (competidoresNaLinha.get() < totalCompetidores) {
            wait();
        } else {
            notifyAll();
            pontos.set(10);
            System.out.println("-----------Começa a corrida!-----------");
        }

        Thread.sleep(100);

        int pontosDaCorrida = Math.max(pontos.getAndDecrement(), 0);

        competidoresNaLinha.getAndDecrement();

        System.out.println("Competidor #" + idCompetidor + " fez " + pontosDaCorrida + " pontos");

        placar.merge(idCompetidor, pontosDaCorrida, Integer::sum);
    }

    public void mostrarPlacar() {
        System.out.println("\n== Podio ==");
        placar.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> System.out.println("Competidor #" + entry.getKey() + " com " + entry.getValue() + " pontos"));

        System.out.println("\n== Tabela de pontos ==");
        placar.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEach(v -> System.out.println("Competidor #" + v.getKey() + " com " + v.getValue() + " pontos"));
    }

    public static void main(String[] args) throws InterruptedException {
        int numCorridas = 10;
        int numCompetidores = 10;
        CorridaDeMotos corrida = new CorridaDeMotos(numCorridas, numCompetidores);

        for (int i = 0; i < numCorridas; i++) {
            System.out.println();
            List<Competidor> competidores = new ArrayList<>();
            for (int j = 1; j <= numCompetidores; j++) {
                Competidor competidor = new Competidor(j, corrida);
                competidores.add(competidor);
                competidor.start();
            }

            for (Competidor competidor : competidores) {
                competidor.join();
            }
        }

        corrida.mostrarPlacar();
    }
}
