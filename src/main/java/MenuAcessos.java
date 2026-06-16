import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuAcessos extends JPanel {

    // Paleta de cores do Mundial 2014 (Brasil)
    private final Color verdeBrasil = new Color(0, 156, 59);
    private final Color azulBrasil = new Color(0, 39, 118);
    private final Color amareloBrasil = new Color(255, 223, 0);

    // Variável para guardar o preço base unitário
    private int precoAtualUnitario = 50;

    public MenuAcessos() {
        // Divide o ecrã em 2 partes (Esquerda e Direita)
        setLayout(new GridLayout(1, 2, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Cor branca com alguma transparência
        Color corPaineis = new Color(255, 255, 255, 230);

        // ==========================================
        // LADO ESQUERDO: Emissão de Bilhetes
        // ==========================================
        // Aumentei o GridLayout para 10 linhas para caber o novo filtro
        JPanel painelEsquerdo = new JPanel(new GridLayout(10, 1, 5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(corPaineis);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        painelEsquerdo.setOpaque(false);
        painelEsquerdo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(verdeBrasil, 2), "Emissão de Bilhetes", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), azulBrasil));

        // NOVAS COMBOBOXES (Filtros em Cascata)
        JComboBox<String> cbGrupo = new JComboBox<>(new String[]{"Selecione o Grupo...", "Grupo A", "Grupo B", "Grupo G (Portugal)"});
        JComboBox<String> cbJogos = new JComboBox<>(new String[]{"Aguardando seleção do grupo..."});
        JComboBox<String> cbSetor = new JComboBox<>(new String[]{"Bancada Central", "Zona VIP", "Topos"});

        JLabel lblPreco = new JLabel("Preço Unitário: €  |  TOTAL: €");
        lblPreco.setFont(new Font("Arial", Font.BOLD, 14));
        lblPreco.setForeground(azulBrasil);

        JSpinner spQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        JButton btnEmitir = new JButton("EMITIR BILHETE");
        btnEmitir.setBackground(verdeBrasil);
        btnEmitir.setForeground(Color.WHITE);
        btnEmitir.setFont(new Font("Arial", Font.BOLD, 14));
        btnEmitir.setFocusPainted(false);

        cbGrupo.setOpaque(false);
        cbJogos.setOpaque(false);
        cbSetor.setOpaque(false);
        spQuantidade.setOpaque(false);

        // Adicionar componentes ao painel esquerdo pela ordem correta
        painelEsquerdo.add(new JLabel("1. Selecionar Fase/Grupo:"));
        painelEsquerdo.add(cbGrupo);
        painelEsquerdo.add(new JLabel("2. Selecionar Jogo:"));
        painelEsquerdo.add(cbJogos);
        painelEsquerdo.add(new JLabel("3. Selecionar Setor:"));
        painelEsquerdo.add(cbSetor);
        painelEsquerdo.add(lblPreco);
        painelEsquerdo.add(new JLabel("4. Quantidade:"));
        painelEsquerdo.add(spQuantidade);
        painelEsquerdo.add(btnEmitir);

        // ==========================================
        // LADO DIREITO: Controlo de Lotação
        // ==========================================
        JPanel painelDireito = new JPanel(new GridLayout(8, 1, 5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(corPaineis);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        painelDireito.setOpaque(false);
        painelDireito.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(amareloBrasil, 2), "Controlo de Lotação (Tempo Real)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), azulBrasil));

        JLabel lblEstadio = new JLabel("Estádio do <>");
        lblEstadio.setFont(new Font("Arial", Font.BOLD, 16));
        lblEstadio.setForeground(verdeBrasil);

        JProgressBar pbCentral = new JProgressBar(0, 1000);
        pbCentral.setValue(800);
        pbCentral.setStringPainted(true);
        pbCentral.setString("Bancada Central: 800 / 1000");
        pbCentral.setForeground(azulBrasil);

        JProgressBar pbVIP = new JProgressBar(0, 200);
        pbVIP.setValue(200);
        pbVIP.setStringPainted(true);
        pbVIP.setString("Zona VIP: ESGOTADO");
        pbVIP.setForeground(Color.RED);

        JProgressBar pbTopos = new JProgressBar(0, 1000);
        pbTopos.setValue(400);
        pbTopos.setStringPainted(true);
        pbTopos.setString("Topos: 400 / 1000");
        pbTopos.setForeground(verdeBrasil);

        painelDireito.add(lblEstadio);
        painelDireito.add(new JLabel(" "));
        painelDireito.add(pbCentral);
        painelDireito.add(new JLabel(" "));
        painelDireito.add(pbVIP);
        painelDireito.add(new JLabel(" "));
        painelDireito.add(pbTopos);

        // ==========================================
        // LÓGICA DE FILTRAGEM DOS JOGOS
        // ==========================================
        cbGrupo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String grupoEscolhido = (String) cbGrupo.getSelectedItem();
                cbJogos.removeAllItems(); // Limpa a lista de jogos

                if ("Grupo A".equals(grupoEscolhido)) {
                    cbJogos.addItem("Brasil vs Croácia");
                    cbJogos.addItem("México vs Camarões");
                    cbJogos.addItem("Brasil vs México");
                    cbJogos.addItem("Camarões vs Croácia");
                } else if ("Grupo B".equals(grupoEscolhido)) {
                    cbJogos.addItem("Espanha vs Holanda");
                    cbJogos.addItem("Chile vs Austrália");
                    cbJogos.addItem("Espanha vs Chile");
                } else if ("Grupo G (Portugal)".equals(grupoEscolhido)) {
                    cbJogos.addItem("Alemanha vs Portugal");
                    cbJogos.addItem("Gana vs EUA");
                    cbJogos.addItem("EUA vs Portugal");
                    cbJogos.addItem("Portugal vs Gana");
                } else {
                    cbJogos.addItem("Aguardando seleção do grupo...");
                }
            }
        });

        // ==========================================
        // LÓGICA DE ATUALIZAÇÃO DE PREÇOS (REQ 3.2)
        // ==========================================
        Runnable atualizarValores = () -> {
            String setorEscolhido = (String) cbSetor.getSelectedItem();
            int quantidade = (Integer) spQuantidade.getValue();

            if ("Zona VIP".equals(setorEscolhido)) {
                precoAtualUnitario = 150;
                btnEmitir.setEnabled(false);
                btnEmitir.setText("ESGOTADO");
                btnEmitir.setBackground(Color.GRAY);
            } else if ("Topos".equals(setorEscolhido)) {
                precoAtualUnitario = 30;
                btnEmitir.setEnabled(true);
                btnEmitir.setText("EMITIR BILHETE");
                btnEmitir.setBackground(verdeBrasil);
            } else {
                precoAtualUnitario = 50;
                btnEmitir.setEnabled(true);
                btnEmitir.setText("EMITIR BILHETE");
                btnEmitir.setBackground(verdeBrasil);
            }

            int precoTotal = precoAtualUnitario * quantidade;
            lblPreco.setText("Preço Unitário: " + precoAtualUnitario + "€  |  TOTAL: " + precoTotal + "€");
        };

        cbSetor.addActionListener(e -> atualizarValores.run());
        spQuantidade.addChangeListener(e -> atualizarValores.run());

        // Ação ao clicar no botão Emitir
        btnEmitir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Validação para evitar que comprem com o "Aguardando..." selecionado
                if (cbJogos.getSelectedIndex() == -1 || cbJogos.getSelectedItem().toString().contains("Aguardando")) {
                    JOptionPane.showMessageDialog(MenuAcessos.this,
                            "Por favor, selecione um grupo e um jogo válido primeiro.",
                            "Erro de Seleção", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String jogo = (String) cbJogos.getSelectedItem();
                String setor = (String) cbSetor.getSelectedItem();
                int qtd = (Integer) spQuantidade.getValue();
                int totalPago = precoAtualUnitario * qtd;

                String fatura = "RESUMO DA COMPRA:\n" +
                        "Jogo: " + jogo + "\n" +
                        "Setor: " + setor + "\n" +
                        "Quantidade: " + qtd + " bilhete(s)\n" +
                        "-------------------------------\n" +
                        "TOTAL PAGO: " + totalPago + "€";

                JOptionPane.showMessageDialog(MenuAcessos.this,
                        fatura,
                        "Transação Concluída - FIFA World Cup 2014", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        add(painelEsquerdo);
        add(painelDireito);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        Color corCima = new Color(180, 230, 50);
        Color corBaixo = new Color(0, 156, 59);
        GradientPaint gp = new GradientPaint(0, 0, corCima, w, h, corBaixo);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Módulo de Acessos - Mundial Brasil 2014");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500); // Aumentei ligeiramente a altura para caber confortavelmente
        frame.setLocationRelativeTo(null);
        frame.add(new MenuAcessos());
        frame.setVisible(true);
    }
}