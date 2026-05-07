import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MenuInicial extends JFrame {

    public MenuInicial() {
        setTitle("Mundial 2026 - Gestão Integrada");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);

        // Layout Principal: BorderLayout (Norte/Centro/Sul)
        JPanel contentorPrincipal = new JPanel(new BorderLayout(20, 20));
        contentorPrincipal.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        contentorPrincipal.setBackground(new Color(245, 245, 245));

        // PARTE SUPERIOR: Match Center e Recursos (Lado a Lado)
        JPanel zonaSuperior = new JPanel(new GridLayout(1, 2, 20, 0));
        zonaSuperior.setOpaque(false);

        // Bloco 1: Match Center (Requisitos: Calendário e Jogos) [cite: 3, 4, 5]
        zonaSuperior.add(criarModulo("MATCH CENTER", "Gestão de Calendário e Fases"));

        // Bloco 2: Recursos e Espaços (Requisitos: Estádios e Alojamento) [cite: 25, 35]
        zonaSuperior.add(criarModulo("RECURSOS E ESPAÇOS", "Hotéis, Centros de Treino e Estádios"));

        // PARTE INFERIOR: Acessos (Largo) [cite: 13, 18, 42]
        JPanel zonaInferior = criarModuloAcessos("ACESSOS", "Bilheteira e Controlo de Entrada");

        contentorPrincipal.add(zonaSuperior, BorderLayout.CENTER);
        contentorPrincipal.add(zonaInferior, BorderLayout.SOUTH);

        add(contentorPrincipal);
        setLocationRelativeTo(null);
    }

    private JPanel criarModulo(String titulo, String sub) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(Color.BLACK, 2));

        JLabel texto = new JLabel("<html><body style='padding:20px'><b>" + titulo + "</b><br><small>" + sub + "</small></body></html>");
        texto.setFont(new Font("SansSerif", Font.PLAIN, 28));

        p.add(texto, BorderLayout.NORTH);
        return p;
    }

    private JPanel criarModuloAcessos(String titulo, String sub) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new LineBorder(Color.BLACK, 2));
        p.setPreferredSize(new Dimension(0, 180));

        JLabel texto = new JLabel("<html><body style='padding:20px'><b>" + titulo + "</b><br><small>" + sub + "</small></body></html>");
        texto.setFont(new Font("SansSerif", Font.PLAIN, 28));

        // Círculo de Notificações (Pedidos Pendentes) [cite: 18, 19]
        JLabel circulo = new JLabel("3", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 53, 69)); // Vermelho alerta
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                super.paintComponent(g);
            }
        };
        circulo.setPreferredSize(new Dimension(60, 60));
        circulo.setForeground(Color.WHITE);
        circulo.setFont(new Font("Arial", Font.BOLD, 25));

        JPanel wrapperCirculo = new JPanel(new GridBagLayout());
        wrapperCirculo.setOpaque(false);
        wrapperCirculo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 40));
        wrapperCirculo.add(circulo);

        p.add(texto, BorderLayout.WEST);
        p.add(wrapperCirculo, BorderLayout.EAST);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuInicial().setVisible(true));
    }
}