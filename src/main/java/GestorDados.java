import java.io.*;
import java.util.*;

// A classe GestorDados guarda o estado de toda a aplicação. 
// Implementa Serializable para podermos guardar tudo num ficheiro binário.
public class GestorDados implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_PATH = "mundial_dados.dat";

    // Instância Singleton
    private static GestorDados instance;

    // ==========================================
    // ESTRUTURAS DE DADOS GLOBAIS
    // ==========================================
    public List<String> selecoes = new ArrayList<>(Arrays.asList(
            "Brasil", "Croácia", "México", "Camarões", "Portugal", "Alemanha", "Espanha", "Argentina", "França"
    ));

    // Mapeia o Estádio (Nome) -> Capacidades [Central, Topos, VIP, Total]
    public Map<String, int[]> estadios = new HashMap<>();

    // Relacionamentos 1 para 1
    public Map<String, String> hoteisVinculados = new HashMap<>(); // Hotel -> Equipa
    public Map<String, String> centrosVinculados = new HashMap<>(); // Centro -> Equipa

    // ==========================================
    // PADRÃO SINGLETON E PERSISTÊNCIA
    // ==========================================
    private GestorDados() {}

    public static GestorDados getInstance() {
        if (instance == null) {
            carregarDados();
        }
        return instance;
    }

    // Método para guardar no ficheiro binário
    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(this);
            System.out.println("Dados guardados com sucesso no ficheiro binário.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para carregar do ficheiro binário
    private static void carregarDados() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                instance = (GestorDados) ois.readObject();
                System.out.println("Dados carregados com sucesso da sessão anterior.");
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        // Se o ficheiro não existir, cria uma instância limpa
        instance = new GestorDados();
    }

    // ==========================================
    // VALIDAÇÕES 1-PARA-1 (Hotéis e Centros)
    // ==========================================
    public boolean equipaJaTemHotel(String equipa) {
        return hoteisVinculados.containsValue(equipa);
    }

    public boolean equipaJaTemCentro(String equipa) {
        return centrosVinculados.containsValue(equipa);
    }
}