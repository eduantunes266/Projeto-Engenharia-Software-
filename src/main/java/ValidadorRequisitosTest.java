import org.junit.Test;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ValidadorRequisitosTest {

    @Test
    public void testeRequisito1_2_ValidarConflitoEstadio_DeveFalhar() {
        boolean resultado = ValidadorRequisitos.validarConflitoEstadio(
                "Maracanã", "15/06/2026", "16:00",
                "Maracanã", "15/06/2026", "16:00"
        );
        assertFalse(resultado, "O sistema deve bloquear dois jogos no mesmo estádio à mesma hora.");
    }

    @Test
    public void testeRequisito6_3_ResultadoEliminatoria_DeveFalhar() {
        boolean resultado = ValidadorRequisitos.validarResultadoEliminatoria(2, 2);
        assertFalse(resultado, "A fase eliminatória não pode aceitar empates.");
    }

    @Test
    public void testeRequisito1_3_AvancoAutomatico_DevePassar() {
        String vencedor = ValidadorRequisitos.determinarVencedor("Portugal", "Espanha", 3, 1);
        assertEquals("Portugal", vencedor, "Portugal tem mais golos, logo deve avançar automaticamente.");
    }

    @Test
    public void testeRequisito2_3_RegraNeutralidade_DeveFalhar() {
        boolean resultado = ValidadorRequisitos.validarNeutralidadeArbitro("Brasil", "Brasil", "Camarões");
        assertFalse(resultado, "O árbitro não pode apitar jogos do seu próprio país.");
    }

    @Test
    public void testeRequisito2_4_GestaoDisponibilidade_DeveFalhar() {
        boolean resultado = ValidadorRequisitos.validarDisponibilidadeArbitro("18/06/2026", "18/06/2026");
        assertFalse(resultado, "O árbitro tem de ter dias de descanso e não pode apitar consecutivamente no mesmo dia.");
    }

    @Test
    public void testeRequisito2_2_EquipaArbitragemUnica_DevePassar() {
        List<String> funcoesCorretas = Arrays.asList("Árbitro Principal", "Assistente 1", "Assistente 2", "Quarto Árbitro");
        boolean resultado = ValidadorRequisitos.validarEquipaArbitragem(funcoesCorretas);
        assertTrue(resultado, "A equipa de arbitragem deve ser aprovada pois tem 4 elementos com funções distintas.");
    }

    @Test
    public void testeRequisito3_4_ControloStock_DeveFalhar() {
        boolean resultado = ValidadorRequisitos.validarCompraBilhetes(2, 5);
        assertFalse(resultado, "A compra deve ser bloqueada se os bilhetes desejados excederem a lotação disponível.");
    }

    @Test
    public void testeRequisito3_2_PrecarioCategorias_TestarMultiplicador() {
        double precoCalculado = ValidadorRequisitos.calcularPrecoBilhete(50.0, "Final");
        assertEquals(150.0, precoCalculado, "O bilhete da Final tem de custar o triplo do preço base.");
    }

    @Test
    public void testeRequisito4_1_AgendamentoViagem_Limites() {
        LocalDateTime ultimaViagem = LocalDateTime.of(2026, 6, 20, 10, 0);
        LocalDateTime novaViagem = LocalDateTime.of(2026, 6, 20, 11, 0);

        boolean resultado = ValidadorRequisitos.validarAgendamentoViagem(ultimaViagem, novaViagem);
        assertFalse(resultado, "A viagem tem de ser rejeitada porque não respeita a margem logística de 2 horas.");
    }
}