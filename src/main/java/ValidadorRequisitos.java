import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

public class ValidadorRequisitos {

    public static boolean validarConflitoEstadio(String estadioNovo, String dataNova, String horaNova, String estadioExistente, String dataExistente, String horaExistente) {
        if (estadioNovo.equals(estadioExistente) && dataNova.equals(dataExistente) && horaNova.equals(horaExistente)) {
            return false;
        }
        return true;
    }

    public static boolean validarResultadoEliminatoria(int golosEquipa1, int golosEquipa2) {
        return golosEquipa1 != golosEquipa2;
    }

    public static String determinarVencedor(String equipa1, String equipa2, int golos1, int golos2) {
        if (golos1 > golos2) return equipa1;
        if (golos2 > golos1) return equipa2;
        return "Empate";
    }

    public static boolean validarNeutralidadeArbitro(String paisArbitro, String paisEquipa1, String paisEquipa2) {
        if (paisArbitro.equalsIgnoreCase(paisEquipa1) || paisArbitro.equalsIgnoreCase(paisEquipa2)) {
            return false;
        }
        return true;
    }

    public static boolean validarDisponibilidadeArbitro(String dataNovoJogo, String dataUltimoJogoDoArbitro) {
        if (dataUltimoJogoDoArbitro != null && dataUltimoJogoDoArbitro.equals(dataNovoJogo)) {
            return false;
        }
        return true;
    }

    public static boolean validarEquipaArbitragem(List<String> funcoesAtribuidas) {
        if (funcoesAtribuidas.size() != 4) return false;

        HashSet<String> funcoesUnicas = new HashSet<>(funcoesAtribuidas);
        return funcoesUnicas.size() == 4;
    }

    public static boolean validarCompraBilhetes(int lugaresDisponiveis, int bilhetesDesejados) {
        return lugaresDisponiveis >= bilhetesDesejados;
    }

    public static double calcularPrecoBilhete(double precoBase, String faseTorneio) {
        if (faseTorneio.equalsIgnoreCase("Final")) {
            return precoBase * 3.0;
        } else if (faseTorneio.equalsIgnoreCase("Meia-Final")) {
            return precoBase * 2.0;
        }
        return precoBase;
    }

    public static boolean validarAgendamentoViagem(LocalDateTime horaUltimaViagem, LocalDateTime horaNovaViagem) {
        if (horaUltimaViagem == null) return true;

        long diferencaHoras = ChronoUnit.HOURS.between(horaUltimaViagem, horaNovaViagem);
        return diferencaHoras >= 2;
    }
}