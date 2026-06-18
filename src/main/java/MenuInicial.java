import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MenuInicial extends JFrame {

    private boolean isAdmin;

    public MenuInicial(boolean isAdmin) {
        this.isAdmin = isAdmin;

        setTitle("Mundial 2026 - Gestão Integrada  (" + (isAdmin ? "Organizador" : "Adepto") + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);

        JPanel contentorPrincipal = new JPanel(new BorderLayout(20, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(230, 240, 20);
                Color color2 = new Color(10, 140, 50);
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        contentorPrincipal.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        if (isAdmin) {
            JPanel zonaSuperior = new JPanel(new GridLayout(1, 2, 20, 0));
            zonaSuperior.setOpaque(false);

            zonaSuperior.add(criarModulo("FIFA WORLD CUP™ MATCH CENTER ", "Gestão de Calendário e Fases",
                    () -> new MenuMatchCenter(true).setVisible(true)));
            zonaSuperior.add(criarModulo("RECURSOS E ESPAÇOS ", "Hotéis, Centros de Treino e Estádios",
                    () -> new PaginaRecursos().setVisible(true)));

            JPanel zonaInferior = criarModuloAcessos("ACESSOS", "Bilheteira e Controlo de Entrada",
                    () -> MenuAcessos.abrirJanela());

            contentorPrincipal.add(zonaSuperior, BorderLayout.CENTER);
            contentorPrincipal.add(zonaInferior, BorderLayout.SOUTH);
        } else {
            JPanel zonaCentral = new JPanel(new GridBagLayout());
            zonaCentral.setOpaque(false);

            JPanel moduloUnico = criarModulo("MATCH CENTER", "Consultar Calendário e Classificações",
                    () -> new MenuMatchCenter(false).setVisible(true));
            moduloUnico.setPreferredSize(new Dimension(500, 250));

            zonaCentral.add(moduloUnico);
            contentorPrincipal.add(zonaCentral, BorderLayout.CENTER);
        }

        setContentPane(contentorPrincipal);
        setLocationRelativeTo(null);
    }

    private JPanel criarModulo(String titulo, String sub, Runnable acao) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                g2.setColor(new Color(255, 255, 255, hover ? 150 : 80));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (hover) {
                    g2.setColor(new Color(0, 74, 35));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel texto = new JLabel("<html><body><b style='color:#004A23; font-size:24px; font-family:sans-serif;'>" + titulo + "</b><br><br><span style='color:#003300; font-size:16px; font-family:sans-serif;'>" + sub + "</span><br><br><span style='color:#004A23; font-size:13px; font-family:sans-serif;'>▶ Clique para abrir</span></body></html>");

        p.add(texto, BorderLayout.NORTH);
        ativarClique(p, acao);
        return p;
    }

    private JPanel criarModuloAcessos(String titulo, String sub, Runnable acao) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                g2.setColor(new Color(10, 20, 80, hover ? 220 : 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (hover) {
                    g2.setColor(new Color(255, 215, 0));
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.setPreferredSize(new Dimension(0, 150));

        JLabel texto = new JLabel("<html><body><b style='color:#FFFFFF; font-size:24px; font-family:sans-serif;'>" + titulo + "</b><br><br><span style='color:#E0E0E0; font-size:16px; font-family:sans-serif;'>" + sub + "</span></body></html>");

        JLabel circulo = new JLabel("3", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 215, 0));
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                super.paintComponent(g);
            }
        };
        circulo.setPreferredSize(new Dimension(60, 60));
        circulo.setForeground(new Color(0, 0, 50));
        circulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        circulo.setToolTipText("Setores disponíveis: Bancada Central, Zona VIP, Topos");

        JPanel wrapperCirculo = new JPanel(new GridBagLayout());
        wrapperCirculo.setOpaque(false);
        wrapperCirculo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        wrapperCirculo.add(circulo);

        p.add(texto, BorderLayout.WEST);
        p.add(wrapperCirculo, BorderLayout.EAST);
        ativarClique(p, acao);
        return p;
    }

    /** Torna um painel clicável: cursor de mão, efeito de hover e ação ao clicar. */
    private void ativarClique(JPanel p, Runnable acao) {
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                acao.run();
            }
            @Override public void mouseEntered(MouseEvent e) {
                p.putClientProperty("hover", Boolean.TRUE);
                p.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                p.putClientProperty("hover", Boolean.FALSE);
                p.repaint();
            }
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            String[] opcoes = {"Organizador ", "Adepto "};
            int escolha = JOptionPane.showOptionDialog(null, "Selecione o perfil de acesso:", "Login do Sistema",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);

            if (escolha == -1) System.exit(0);

            boolean isAdmin = (escolha == 0);
            new MenuInicial(isAdmin).setVisible(true);
        });
    }
}
