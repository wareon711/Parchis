package presentacion;

import dto.EstadoPartidaDTO;
import dto.FichaDTO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PanelTablero extends JPanel {

    private static final int CELDAS = 15;
    private static final int[][] ANILLO = construirAnillo();

    private String idJugadorLocal = "J1";
    private Consumer<String> alClicFicha = id -> {};
    private EstadoPartidaDTO estadoActual;
    private final List<Shape> zonasClicables = new ArrayList<>();
    private final List<String> idsClicables = new ArrayList<>();

    private final Map<String, Integer> ultimasPosiciones = new HashMap<>();
    private final Map<String, Boolean> ultimasBases = new HashMap<>();

    private Timer timerSalto;
    private String idFichaAnimada;
    private final List<double[]> rutaAnimada = new ArrayList<>();
    private int pasoRuta = 0;
    private double subPaso = 0.0;
    private double animCx = 0.0;
    private double animCy = 0.0;
    private double animSaltoY = 0.0;

    public PanelTablero() {
        setOpaque(false);
        setPreferredSize(new Dimension(470, 470));
        setMinimumSize(new Dimension(470, 470));

        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (idFichaAnimada != null) {
                    return;
                }
                String id = fichaEn(e);
                if (id != null && alClicFicha != null) {
                    alClicFicha.accept(id);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (idFichaAnimada != null) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                setCursor(fichaEn(e) != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        };

        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    public PanelTablero(String idJugadorLocal, Consumer<String> alClicFicha) {
        this();
        configurar(idJugadorLocal, alClicFicha);
    }

    public void configurar(String idJugadorLocal, Consumer<String> alClicFicha) {
        this.idJugadorLocal = idJugadorLocal != null ? idJugadorLocal : "J1";
        this.alClicFicha = alClicFicha != null ? alClicFicha : id -> {};
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(470, 470);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(470, 470);
    }

    public void dibujarFichas(EstadoPartidaDTO estado) {
        if (estado == null) {
            this.estadoActual = null;
            repaint();
            return;
        }

        if (ultimasPosiciones.isEmpty()) {
            for (FichaDTO f : estado.getFichas()) {
                ultimasPosiciones.put(f.getIdFicha(), f.getPosicion());
                ultimasBases.put(f.getIdFicha(), f.isEnBase());
            }
            this.estadoActual = estado;
            repaint();
            return;
        }

        FichaDTO fichaMovida = null;
        int posVieja = -1;
        boolean estabaEnBase = false;

        for (FichaDTO f : estado.getFichas()) {
            int vieja = ultimasPosiciones.getOrDefault(f.getIdFicha(), -1);
            boolean enBaseVieja = ultimasBases.getOrDefault(f.getIdFicha(), false);

            if (enBaseVieja && !f.isEnBase()) {
                fichaMovida = f;
                posVieja = -1;
                estabaEnBase = true;
                break;
            } else if (!enBaseVieja && !f.isEnBase() && f.getPosicion() > vieja) {
                fichaMovida = f;
                posVieja = vieja;
                estabaEnBase = false;
                break;
            }
        }

        for (FichaDTO f : estado.getFichas()) {
            ultimasPosiciones.put(f.getIdFicha(), f.getPosicion());
            ultimasBases.put(f.getIdFicha(), f.isEnBase());
        }

        this.estadoActual = estado;

        if (fichaMovida != null) {
            iniciarAnimacionFicha(fichaMovida, estabaEnBase, posVieja);
        } else {
            repaint();
        }
    }

    private void iniciarAnimacionFicha(FichaDTO ficha, boolean estabaEnBase, int posVieja) {
        if (timerSalto != null && timerSalto.isRunning()) {
            timerSalto.stop();
        }

        int anchoTotal = getWidth() > 0 ? getWidth() : 470;
        int altoTotal = getHeight() > 0 ? getHeight() : 470;
        int margen = 14;
        double lado = Math.max(300, Math.min(anchoTotal, altoTotal) - 2 * margen);
        double celda = lado / CELDAS;
        double x0 = (anchoTotal - lado) / 2.0;
        double y0 = (altoTotal - lado) / 2.0;

        rutaAnimada.clear();
        String id = ficha.getIdFicha();
        String color = ficha.getColor();

        if (estabaEnBase) {
            int indice = Math.max(0, Math.min(3, ultimoDigito(id) - 1));
            double[] s = posicionBaseEnTablero(color, indice);
            rutaAnimada.add(new double[]{x0 + s[0] * celda, y0 + s[1] * celda});
            rutaAnimada.add(coordenadasCasilla(color, 0, id, x0, y0, celda));
        } else {
            int posFin = ficha.getPosicion();
            for (int p = posVieja; p <= posFin; p++) {
                rutaAnimada.add(coordenadasCasilla(color, p, id, x0, y0, celda));
            }
        }

        if (rutaAnimada.size() < 2) {
            idFichaAnimada = null;
            repaint();
            return;
        }

        idFichaAnimada = id;
        pasoRuta = 0;
        subPaso = 0.0;
        double[] inicial = rutaAnimada.get(0);
        animCx = inicial[0];
        animCy = inicial[1];
        animSaltoY = 0.0;

        timerSalto = new Timer(28, e -> {
            subPaso += 0.25;
            double[] desde = rutaAnimada.get(pasoRuta);
            double[] hasta = rutaAnimada.get(pasoRuta + 1);

            animCx = desde[0] + (hasta[0] - desde[0]) * subPaso;
            animCy = desde[1] + (hasta[1] - desde[1]) * subPaso;
            animSaltoY = -64.0 * subPaso * (1.0 - subPaso);

            if (subPaso >= 1.0) {
                subPaso = 0.0;
                pasoRuta++;
                if (pasoRuta >= rutaAnimada.size() - 1) {
                    timerSalto.stop();
                    idFichaAnimada = null;
                    animSaltoY = 0.0;
                }
            }
            repaint();
        });
        timerSalto.start();
    }

    private double[] coordenadasCasilla(String color, int posicion, String idFicha, double x0, double y0, double celda) {
        if (posicion < 0) {
            int indice = Math.max(0, Math.min(3, ultimoDigito(idFicha) - 1));
            double[] s = posicionBaseEnTablero(color, indice);
            return new double[]{x0 + s[0] * celda, y0 + s[1] * celda};
        }
        if (posicion == 56) {
            int indice = Math.max(0, Math.min(3, ultimoDigito(idFicha) - 1));
            double[] s = posicionMetaEnTablero(color, indice);
            return new double[]{x0 + s[0] * celda, y0 + (s[1] - 0.04) * celda};
        }
        if (posicion <= 50) {
            int idx = (casillaInicio(color) + posicion) % 52;
            int[] pos = coordenadaAnillo(idx);
            return new double[]{x0 + (pos[0] + 0.5) * celda, y0 + (pos[1] + 0.5) * celda};
        }
        int[] c = coordenadaCarril(color, posicion - 51);
        return new double[]{x0 + (c[0] + 0.5) * celda, y0 + (c[1] + 0.5) * celda};
    }

    public String idFichaEn(int x, int y) {
        for (int i = zonasClicables.size() - 1; i >= 0; i--) {
            if (zonasClicables.get(i).contains(x, y)) {
                return idsClicables.get(i);
            }
        }
        return null;
    }

    private String fichaEn(MouseEvent e) {
        return idFichaEn(e.getX(), e.getY());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        zonasClicables.clear();
        idsClicables.clear();

        int anchoTotal = getWidth() > 0 ? getWidth() : 470;
        int altoTotal = getHeight() > 0 ? getHeight() : 470;
        int margen = 14;
        double lado = Math.max(300, Math.min(anchoTotal, altoTotal) - 2 * margen);
        double celda = lado / CELDAS;
        double x0 = (anchoTotal - lado) / 2.0;
        double y0 = (altoTotal - lado) / 2.0;

        BufferedImage imgTablero = Recursos.tablero();
        if (imgTablero != null) {
            double altoImg = lado * (imgTablero.getHeight() / (double) imgTablero.getWidth());
            g.drawImage(imgTablero, (int) Math.round(x0), (int) Math.round(y0), (int) Math.round(lado), (int) Math.round(altoImg), null);
        }

        dibujarFichasEnTablero(g, x0, y0, celda);
        g.dispose();
    }

    private static double[] posicionBaseEnTablero(String color, int slot) {
        switch (color) {
            case "VERDE":
                switch (slot) {
                    case 0: return new double[]{10.973, 1.622};
                    case 1: return new double[]{13.189, 1.622};
                    case 2: return new double[]{10.973, 3.813};
                    default: return new double[]{13.189, 3.813};
                }
            case "ROJO":
                switch (slot) {
                    case 0: return new double[]{1.768, 1.622};
                    case 1: return new double[]{3.984, 1.622};
                    case 2: return new double[]{1.768, 3.813};
                    default: return new double[]{3.984, 3.813};
                }
            case "AMARILLO":
                switch (slot) {
                    case 0: return new double[]{10.973, 10.934};
                    case 1: return new double[]{13.189, 10.934};
                    case 2: return new double[]{10.973, 13.125};
                    default: return new double[]{13.189, 13.125};
                }
            default:
                switch (slot) {
                    case 0: return new double[]{1.726, 10.934};
                    case 1: return new double[]{3.942, 10.934};
                    case 2: return new double[]{1.726, 13.125};
                    default: return new double[]{3.942, 13.125};
                }
        }
    }

    private void dibujarFichasEnTablero(Graphics2D g, double x0, double y0, double celda) {
        if (estadoActual == null) {
            String[] colores = {"ROJO", "VERDE", "AZUL", "AMARILLO"};
            for (String col : colores) {
                BufferedImage img = Recursos.ficha(col);
                if (img == null) {
                    continue;
                }
                for (int slot = 0; slot < 4; slot++) {
                    double[] s = posicionBaseEnTablero(col, slot);
                    double cx = x0 + s[0] * celda;
                    double cy = y0 + s[1] * celda;
                    double ancho = celda * 0.95;
                    double alto = ancho * img.getHeight() / img.getWidth();
                    double px = cx - ancho / 2;
                    double py = cy + celda * 0.42 - alto;
                    g.drawImage(img, (int) Math.round(px), (int) Math.round(py), (int) Math.round(ancho), (int) Math.round(alto), null);
                }
            }
            return;
        }

        Map<String, List<PiezaVisual>> porCelda = new HashMap<>();

        for (FichaDTO f : estadoActual.getFichas()) {
            PiezaVisual p = new PiezaVisual();
            p.ficha = f;
            p.escala = 1.0;
            p.saltoY = 0.0;
            String clave;

            boolean esLaAnimada = f.getIdFicha().equals(idFichaAnimada);

            if (esLaAnimada) {
                p.cx = animCx;
                p.cy = animCy;
                p.saltoY = animSaltoY;
                clave = "animada-" + f.getIdFicha();
            } else if (f.isEnBase()) {
                int indice = Math.max(0, Math.min(3, ultimoDigito(f.getIdFicha()) - 1));
                double[] s = posicionBaseEnTablero(f.getColor(), indice);
                p.cx = x0 + s[0] * celda;
                p.cy = y0 + s[1] * celda;
                clave = "base-" + f.getIdFicha();
            } else if (f.isEnMeta()) {
                int indice = Math.max(0, Math.min(3, ultimoDigito(f.getIdFicha()) - 1));
                double[] s = posicionMetaEnTablero(f.getColor(), indice);
                p.cx = x0 + s[0] * celda;
                p.cy = y0 + (s[1] - 0.04) * celda;
                p.escala = 0.52;
                clave = "meta-" + f.getIdFicha();
            } else if (f.getPosicion() <= 50) {
                int idx = (casillaInicio(f.getColor()) + f.getPosicion()) % 52;
                int[] pos = coordenadaAnillo(idx);
                p.cx = x0 + (pos[0] + 0.5) * celda;
                p.cy = y0 + (pos[1] + 0.5) * celda;
                clave = "anillo-" + idx;
            } else {
                int[] c = coordenadaCarril(f.getColor(), f.getPosicion() - 51);
                p.cx = x0 + (c[0] + 0.5) * celda;
                p.cy = y0 + (c[1] + 0.5) * celda;
                clave = "carril-" + f.getColor() + f.getPosicion();
            }

            if (!porCelda.containsKey(clave)) {
                porCelda.put(clave, new ArrayList<>());
            }
            porCelda.get(clave).add(p);
        }

        List<PiezaVisual> todas = new ArrayList<>();
        for (List<PiezaVisual> grupo : porCelda.values()) {
            int n = grupo.size();
            for (int i = 0; i < n; i++) {
                PiezaVisual p = grupo.get(i);
                if (n > 1 && !p.ficha.getIdFicha().equals(idFichaAnimada)) {
                    p.cx += (i - (n - 1) / 2.0) * celda * 0.32;
                    p.escala *= 0.85;
                }
                todas.add(p);
            }
        }

        for (int i = 0; i < todas.size() - 1; i++) {
            for (int j = i + 1; j < todas.size(); j++) {
                if (todas.get(i).cy > todas.get(j).cy) {
                    PiezaVisual temp = todas.get(i);
                    todas.set(i, todas.get(j));
                    todas.set(j, temp);
                }
            }
        }

        for (PiezaVisual p : todas) {
            boolean esLaAnimada = p.ficha.getIdFicha().equals(idFichaAnimada);
            boolean propiaMovible = !esLaAnimada && idFichaAnimada == null && p.ficha.isMovible() && p.ficha.getIdJugador().equals(idJugadorLocal);
            BufferedImage img = Recursos.ficha(p.ficha.getColor());
            if (img == null) {
                continue;
            }

            double ancho = celda * 0.95 * p.escala;
            double alto = ancho * img.getHeight() / img.getWidth();
            double px = p.cx - ancho / 2;
            double py = p.cy + celda * 0.42 * p.escala - alto + p.saltoY;

            if (p.saltoY < -2.0) {
                g.setColor(new Color(0, 0, 0, 70));
                g.fill(new Ellipse2D.Double(p.cx - ancho * 0.35, p.cy + celda * 0.35, ancho * 0.7, celda * 0.22));
            }

            if (propiaMovible) {
                g.setColor(new Color(255, 200, 30));
                g.setStroke(new BasicStroke(2.5f));
                g.draw(new Ellipse2D.Double(p.cx - celda * 0.55, p.cy - celda * 0.4, celda * 1.1, celda * 0.9));
            }

            g.drawImage(img, (int) Math.round(px), (int) Math.round(py), (int) Math.round(ancho), (int) Math.round(alto), null);

            if (propiaMovible) {
                zonasClicables.add(new Rectangle2D.Double(
                        Math.min(px, p.cx - celda * 0.55), Math.min(py, p.cy - celda * 0.4),
                        Math.max(ancho, celda * 1.1), Math.max(alto, celda * 0.9)));
                idsClicables.add(p.ficha.getIdFicha());
            }
        }
    }

    private static double[] posicionMetaEnTablero(String color, int slot) {
        switch (color) {
            case "VERDE":
                switch (slot) {
                    case 0: return new double[]{7.324, 6.399};
                    case 1: return new double[]{7.750, 6.399};
                    case 2: return new double[]{8.176, 6.399};
                    default: return new double[]{7.750, 6.821};
                }
            case "ROJO":
                switch (slot) {
                    case 0: return new double[]{6.429, 6.736};
                    case 1: return new double[]{6.429, 7.158};
                    case 2: return new double[]{6.429, 7.579};
                    default: return new double[]{6.855, 7.158};
                }
            case "AMARILLO":
                switch (slot) {
                    case 0: return new double[]{8.560, 7.158};
                    case 1: return new double[]{8.560, 7.579};
                    case 2: return new double[]{8.560, 8.000};
                    default: return new double[]{8.134, 7.579};
                }
            default:
                switch (slot) {
                    case 0: return new double[]{6.834, 8.443};
                    case 1: return new double[]{7.260, 8.443};
                    case 2: return new double[]{7.686, 8.443};
                    default: return new double[]{7.260, 8.021};
                }
        }
    }

    private static int ultimoDigito(String idFicha) {
        return Character.digit(idFicha.charAt(idFicha.length() - 1), 10);
    }

    private static int[][] construirAnillo() {
        List<int[]> r = new ArrayList<>();
        for (int c = 1; c <= 5; c++) r.add(new int[]{c, 6});
        for (int f = 5; f >= 0; f--) r.add(new int[]{6, f});
        r.add(new int[]{7, 0});
        r.add(new int[]{8, 0});
        for (int f = 1; f <= 5; f++) r.add(new int[]{8, f});
        for (int c = 9; c <= 14; c++) r.add(new int[]{c, 6});
        r.add(new int[]{14, 7});
        r.add(new int[]{14, 8});
        for (int c = 13; c >= 9; c--) r.add(new int[]{c, 8});
        for (int f = 9; f <= 14; f++) r.add(new int[]{8, f});
        r.add(new int[]{7, 14});
        r.add(new int[]{6, 14});
        for (int f = 13; f >= 9; f--) r.add(new int[]{6, f});
        for (int c = 5; c >= 0; c--) r.add(new int[]{c, 8});
        r.add(new int[]{0, 7});
        r.add(new int[]{0, 6});
        return r.toArray(new int[0][]);
    }

    private static int casillaInicio(String color) {
        if ("VERDE".equals(color)) {
            return 13;
        }
        if ("AMARILLO".equals(color)) {
            return 26;
        }
        if ("AZUL".equals(color)) {
            return 39;
        }
        return 0;
    }

    private static int[] coordenadaCarril(String color, int paso) {
        if ("VERDE".equals(color)) {
            return new int[]{7, 1 + paso};
        }
        if ("AMARILLO".equals(color)) {
            return new int[]{13 - paso, 7};
        }
        if ("AZUL".equals(color)) {
            return new int[]{7, 13 - paso};
        }
        return new int[]{1 + paso, 7};
    }

    private static int[] coordenadaAnillo(int casilla) {
        return ANILLO[casilla];
    }

    private static class PiezaVisual {
        FichaDTO ficha;
        double cx;
        double cy;
        double escala;
        double saltoY;
    }
}

