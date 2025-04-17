import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.image.*;

public class GamePanel extends Canvas implements Runnable {
	private static final int PWIDTH = 960;
	private static final int PHEIGHT = 800;
	private Thread animator;
	private boolean running = false;
	//private boolean gameOver = false;

	int FPS, SFPS;
	int fpscount;

	public static Random rnd = new Random();

	// BufferedImage imagemcharsets;

	boolean LEFT, RIGHT, UP, DOWN;

	public static int mousex, mousey;

	public static ArrayList<Agente> listadeagentes = new ArrayList<Agente>();

	Mapa_Grid mapa;

	double posx, posy;

	MeuAgente meuHeroi = null;

	// TODO ESSE È O RESULTADO
	int caminho[] = null;

	float zoom = 1;

	int ntileW = 60;
	int ntileH = 50;

	Font f = new Font("", Font.BOLD, 20);

	// A*
	class NodoAStar {
		int x, y;
		int g, h;
		NodoAStar pai;

		public NodoAStar(int x, int y, int g, int h, NodoAStar pai) {
			this.x = x;
			this.y = y;
			this.g = g;
			this.h = h;
			this.pai = pai;
		}

		public int getF() {
			return g + h;
		}
	}

	private CopyOnWriteArrayList<NodoAStar> abertosAStar = new CopyOnWriteArrayList<>();
	private HashSet<Integer> fechadosAStar = new HashSet<>();

	public GamePanel() {

		setBackground(Color.white);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

		setFocusable(true);

		requestFocus();

		// Adiciona um Key Listner
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();

				if (keyCode == KeyEvent.VK_LEFT) {
					LEFT = true;
				}
				if (keyCode == KeyEvent.VK_RIGHT) {
					RIGHT = true;
				}
				if (keyCode == KeyEvent.VK_UP) {
					UP = true;
				}
				if (keyCode == KeyEvent.VK_DOWN) {
					DOWN = true;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();

				if (keyCode == KeyEvent.VK_LEFT) {
					LEFT = false;
				}
				if (keyCode == KeyEvent.VK_RIGHT) {
					RIGHT = false;
				}
				if (keyCode == KeyEvent.VK_UP) {
					UP = false;
				}
				if (keyCode == KeyEvent.VK_DOWN) {
					DOWN = false;
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				mousex = e.getX();
				mousey = e.getY();

			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.getButton() == 3) {
					int mousex = (int) ((e.getX() + mapa.MapX) / zoom);
					int mousey = (int) ((e.getY() + mapa.MapY) / zoom);

					int mx = mousex / 16;
					int my = mousey / 16;

					if (mx >= mapa.Largura || mx < 0) {
						return;
					}
					if (my >= mapa.Altura || my < 0) {
						return;
					}

					mapa.mapa[my][mx] = 1;
				}
			}
		});

		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				// System.out.println(" "+arg0.getButton());
				int mousex = (int) ((arg0.getX() + mapa.MapX) / zoom);
				int mousey = (int) ((arg0.getY() + mapa.MapY) / zoom);

				// System.out.println(""+arg0.getX()+" "+mapa.MapX+" "+zoom);
				// System.out.println(""+mousex+" "+mousey);

				int mx = mousex / 16;
				int my = mousey / 16;

				if (mx >= mapa.Largura || mx < 0) {
					return;
				}
				if (my >= mapa.Altura || my < 0) {
					return;
				}

				if (arg0.getButton() == 3) {

					if (mapa.mapa[my][mx] == 0) {
						mapa.mapa[my][mx] = 1;
					} else {
						mapa.mapa[my][mx] = 0;
					}
				}
				if (arg0.getButton() == 1) {
					if (mapa.mapa[my][mx] == 0) {
						caminho = null;
						long timeini = System.currentTimeMillis();

						// TODO Executa Algoritmo
						System.out.println("Destino: " + my + " " + mx);
						System.out.println("Posição Atual: " + (int) (meuHeroi.X / 16) + " " + (int) (meuHeroi.Y / 16));

						////////
						rodaAEstrela((int) (meuHeroi.X / 16), (int) (meuHeroi.Y / 16), mx, my);
						////////

						long timefin = System.currentTimeMillis() - timeini;
						System.out.println("Tempo Final: " + timefin + "ms");
					} else {
						System.out.println("Caminho Final Bloqueado");
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// System.out.println("w "+e.getWheelRotation());
				if (e.getWheelRotation() > 0) {
					zoom = zoom * 1.1f;
				} else if (e.getWheelRotation() < 0) {
					zoom = zoom * 0.90f;
				}

				ntileW = (int) ((960 / zoom) / 16) + 1;
				ntileH = (int) ((800 / zoom) / 16) + 1;

				if (ntileW >= 1000) {
					ntileW = 1000;
				}
				if (ntileH >= 1000) {
					ntileH = 1000;
				}
				mapa.NumeroTilesX = ntileW;
				mapa.NumeroTilesY = ntileH;
			}
		});

		meuHeroi = new MeuAgente(10, 10, Color.blue);

		listadeagentes.add(meuHeroi);

		mousex = mousey = 0;

		mapa = new Mapa_Grid(100, 100, ntileW, ntileH);
		mapa.loadmapfromimage("/resources/imagemlabirinto1000.png");

	}

	int passoAtual = 0;
	int tempoMovimento = 0;

	public boolean rodaAEstrela(int iniX, int iniY, int objX, int objY) {
		abertosAStar.clear();
		fechadosAStar.clear();

		NodoAStar start = new NodoAStar(iniX, iniY, 0, heuristica(iniX, iniY, objX, objY), null);
		abertosAStar.add(start);

		int maxIterations = 50000;
		int iterations = 0;

		while (!abertosAStar.isEmpty() && iterations < maxIterations) {
			iterations++;

			NodoAStar atual = null;
			int lowestF = Integer.MAX_VALUE;

			for (NodoAStar n : abertosAStar) {
				int f = n.getF();
				if (f < lowestF || (f == lowestF && n.g > atual.g)) {
					lowestF = f;
					atual = n;
				}
			}

			if (atual == null) {
				break;
			}

			abertosAStar.remove(atual);

			int hash = atual.x + atual.y * 1000;
			fechadosAStar.add(hash);

			if (atual.x == objX && atual.y == objY) {
				// Reconstruir caminho
				LinkedList<NodoAStar> path = new LinkedList<>();
				NodoAStar temp = atual;
				while (temp != null) {
					path.addFirst(temp);
					temp = temp.pai;
				}

				caminho = new int[path.size() * 2];
				int i = 0;
				for (NodoAStar node : path) {
					caminho[i] = node.x;
					caminho[i + 1] = node.y;
					i += 2;
				}
				passoAtual = 0;
				System.out.println("Caminho encontrado com " + path.size() + " passos");
				return true;
			}

			// Vizinhos (sem diagonais)
			int[] dx = { 0, 1, 0, -1 };
			int[] dy = { -1, 0, 1, 0 };

			for (int i = 0; i < 4; i++) {
				int nx = atual.x + dx[i];
				int ny = atual.y + dy[i];

				if (nx < 0 || ny < 0 || nx >= mapa.Largura || ny >= mapa.Altura) {
					continue;
				}

				hash = nx + ny * 1000;
				if (mapa.mapa[ny][nx] == 1 || fechadosAStar.contains(hash)) {
					continue;
				}

				int newG = atual.g + 10;

				boolean skip = false;
				for (NodoAStar aberto : abertosAStar) {
					if (aberto.x == nx && aberto.y == ny) {
						if (aberto.g <= newG) {
							skip = true;
							break;
						} else {
							abertosAStar.remove(aberto);
							break;
						}
					}
				}

				if (!skip) {
					int h = heuristica(nx, ny, objX, objY);
					NodoAStar novo = new NodoAStar(nx, ny, newG, h, atual);
					abertosAStar.add(novo);
				}
			}
		}

		System.out.println("Nenhum caminho encontrado após " + iterations + " iterações");
		return false;
	}

	private int heuristica(int x1, int y1, int x2, int y2) {
		return 10 * (Math.abs(x1 - x2) + Math.abs(y1 - y2));
	}

	public void startGame() {
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	}

	public void stopGame() {
		running = false;
	}

	public void run() {
		running = true;

		long DifTime, TempoAnterior;

		int segundo = 0;
		DifTime = 0;
		TempoAnterior = System.currentTimeMillis();

		this.createBufferStrategy(2);
		BufferStrategy strategy = this.getBufferStrategy();

		while (running) {

			gameUpdate(DifTime);
			Graphics g = strategy.getDrawGraphics();
			gameRender((Graphics2D) g);
			strategy.show();

			try {
				Thread.sleep(5);
			} catch (InterruptedException ex) {
			}

			DifTime = System.currentTimeMillis() - TempoAnterior;
			TempoAnterior = System.currentTimeMillis();

			if (segundo != ((int) (TempoAnterior / 1000))) {
				FPS = SFPS;
				SFPS = 1;
				segundo = ((int) (TempoAnterior / 1000));
			} else {
				SFPS++;
			}

		}
		System.exit(0);
	}

	int timerfps = 0;

	private void gameUpdate(long DiffTime) {
		if (caminho != null && passoAtual < caminho.length / 2) {
			tempoMovimento += DiffTime;
			if (tempoMovimento >= 150) {
				meuHeroi.X = caminho[passoAtual * 2] * 16;
				meuHeroi.Y = caminho[passoAtual * 2 + 1] * 16;
				passoAtual++;
				tempoMovimento = 0;
			}
		}

		if (LEFT) {
			mapa.MapX -= 5;
		}
		if (RIGHT) {
			mapa.MapX += 5;
		}
		if (UP) {
			mapa.MapY -= 5;
		}
		if (DOWN) {
			mapa.MapY += 5;
		}
	}

	private void gameRender(Graphics2D dbg) {
		dbg.setColor(Color.white);
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

		AffineTransform trans = dbg.getTransform();
		dbg.scale(zoom, zoom);

		try {
			mapa.DesenhaSe(dbg);
		} catch (Exception e) {
			System.out.println("Erro ao desenhar mapa");
			e.printStackTrace();
		}

		// desenhar nós A* (fechados = verde)
		synchronized(fechadosAStar) {
			for (Integer hash : fechadosAStar) {
				int px = hash % 1000;
				int py = hash / 1000;
				dbg.setColor(new Color(0, 200, 0, 150));
				dbg.fillRect(px * 16 - mapa.MapX, py * 16 - mapa.MapY, 16, 16);
			}
		}

		// desenhar nós A* (abertos = laranja)
		synchronized(abertosAStar) {
			for (NodoAStar n : abertosAStar) {
				dbg.setColor(new Color(255, 165, 0, 150));
				dbg.fillRect(n.x * 16 - mapa.MapX, n.y * 16 - mapa.MapY, 16, 16);
			}
		}

		for (int i = 0; i < listadeagentes.size(); i++) {
			listadeagentes.get(i).DesenhaSe(dbg, mapa.MapX, mapa.MapY);
		}

		if (caminho != null) {
			try {
				for (int i = 0; i < caminho.length / 2; i++) {
					int nx = caminho[i * 2];
					int ny = caminho[i * 2 + 1];

					float hue = (float)i / (caminho.length / 2);
					Color pathColor = Color.getHSBColor(hue, 0.8f, 0.9f);
					dbg.setColor(pathColor);

					dbg.fillRect(nx * 16 - mapa.MapX, ny * 16 - mapa.MapY, 16, 16);

					if (i < caminho.length/2 - 1) {
						int nextX = caminho[(i+1) * 2];
						int nextY = caminho[(i+1) * 2 + 1];

						dbg.setColor(Color.BLACK);
						dbg.drawLine(
								nx * 16 - mapa.MapX + 8,
								ny * 16 - mapa.MapY + 8,
								nextX * 16 - mapa.MapX + 8,
								nextY * 16 - mapa.MapY + 8
						);
					}
				}

				if (passoAtual < caminho.length / 2) {
					int cx = caminho[passoAtual * 2];
					int cy = caminho[passoAtual * 2 + 1];
					dbg.setColor(Color.RED);
					dbg.drawRect(cx * 16 - mapa.MapX, cy * 16 - mapa.MapY, 16, 16);
				}

			} catch (Exception e) {
				System.out.println("Error drawing path");
				e.printStackTrace();
			}
		}

		dbg.setTransform(trans);
		dbg.setFont(f);
		dbg.setColor(Color.BLUE);
		dbg.drawString("FPS: " + FPS, 10, 30);
		dbg.drawString("N (Fechados): " + fechadosAStar.size(), 100, 30);
		dbg.drawString("N (Abertos): " + abertosAStar.size(), 300, 30);

		if (caminho != null) {
			dbg.drawString("Tamanho do caminho: " + (caminho.length/2), 500, 30);
			dbg.drawString("Passo atual: " + passoAtual + "/" + (caminho.length/2), 700, 30);
		}
	}
}