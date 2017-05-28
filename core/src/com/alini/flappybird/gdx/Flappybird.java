package com.alini.flappybird.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Flappybird extends ApplicationAdapter {

	//SpriteBatch é usada para animar imagens
	private SpriteBatch batch;
	private Texture [] bird;
	private Texture background;
	private Texture topPipe;
	private Texture belowPipe;
	private Texture gameOver;
	private Random randomNumber;
	private BitmapFont bitmapFont;
	private BitmapFont bitmapMessage;

	private Circle birdCircle;
	private Rectangle rectangleTopPipe;
	private Rectangle rectangleBelowPipe;
	//private ShapeRenderer shapeRenderer;

	//Atributos de configuração
	private float deviceWidth;
	private float deviceHeight;
	private int gameStatus = 0; // 0 - não iniciado 1- jogo iniciado 2- Game over
	private int score = 0;
	private boolean scored = false;
	private float middlePositionVer;
	private float birdFirstPosition;
	private float variation = 0;
	private float fallSpeed = 0;
	private float positionPipemovimentHor;
	private float passageBetweenPipes;
	private float randomPassageBetweenPipes;
	private float deltaTime;

	//Câmera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 768;
	private final float VIRTUAL_HEIGHT = 1024;


	@Override
	public void create () {

		batch = new SpriteBatch();
		randomNumber = new Random();

		birdCircle = new Circle();
		//shapeRenderer = new ShapeRenderer();

		bitmapFont = new BitmapFont();
		bitmapFont.setColor(Color.WHITE);
		bitmapFont.getData().setScale(7);
		bitmapMessage = new BitmapFont();
		bitmapMessage.setColor(Color.WHITE);
		bitmapMessage.getData().setScale(4);

		bird = new Texture[3];
		bird[0] = new Texture("bird1.png");
		bird[1] = new Texture("bird2.png");
		bird[2] = new Texture("bird3.png");

		topPipe = new Texture("toppipe.png");
		belowPipe = new Texture("belowpipe.png");
		background = new Texture("background.png");
		gameOver = new Texture("game_over.png");

		//Configurações da câmera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT,camera);

		deviceWidth = VIRTUAL_WIDTH;
		deviceHeight = VIRTUAL_HEIGHT;

		middlePositionVer = deviceHeight/2;
		birdFirstPosition = middlePositionVer;
		positionPipemovimentHor = deviceWidth;
		passageBetweenPipes = 190;
	}

	@Override
	public void render () {

		camera.update();

		//Limpar frames anteriores e melhorar a utilização de recursos
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		deltaTime = Gdx.graphics.getDeltaTime();
		variation += deltaTime * 10;
		if (variation > 2) variation = 0;

		if (gameStatus == 0){
			if (Gdx.input.justTouched()){
				gameStatus = 1;
			}
		} else {
			fallSpeed ++;
			if (birdFirstPosition > 0 || fallSpeed < 0){
				birdFirstPosition = birdFirstPosition - fallSpeed;
			}

			if (gameStatus == 1 ){
				positionPipemovimentHor -= deltaTime * 400;

				if (Gdx.input.justTouched()){
					fallSpeed = -15;
				}

				//Verifica se o cano saiu da tela e reinicia a posição dele.
				if (positionPipemovimentHor < -topPipe.getWidth()){
					positionPipemovimentHor = deviceWidth;
					randomPassageBetweenPipes = randomNumber.nextInt(500) - 200;
					scored = false;
				}

				//Verifica pontuação
				if(positionPipemovimentHor < 120){
					if(!scored){
						score++;
						scored = true;
					}
				}
			} else {
				if (Gdx.input.justTouched()){
					gameStatus = 0;
					score = 0;
					fallSpeed = 0;
					birdFirstPosition = middlePositionVer;
					positionPipemovimentHor = deviceWidth;
				}
			}
		}

		// Configurar dados de projeção da câmera
		batch.setProjectionMatrix(camera.combined);

		//Desenhar as texturas na tela
		batch.begin();
		batch.draw(background, 0, 0, deviceWidth , deviceHeight);
		batch.draw(topPipe, positionPipemovimentHor, middlePositionVer + passageBetweenPipes + randomPassageBetweenPipes);
		batch.draw(belowPipe, positionPipemovimentHor, middlePositionVer - belowPipe.getHeight() - passageBetweenPipes + randomPassageBetweenPipes);
		batch.draw(bird[(int)variation], 120, birdFirstPosition );
		bitmapFont.draw(batch, String.valueOf(score), deviceWidth / 2 , deviceHeight - 50);

		if (gameStatus == 2){
			batch.draw(gameOver, deviceWidth / 2 - gameOver.getWidth() / 2 , deviceHeight / 2);
			bitmapMessage.draw(batch, "Tap to restart..", deviceWidth / 2 - 200 , deviceHeight / 2  - gameOver.getHeight() / 2 );
		}

		batch.end();

		birdCircle.set(120 + bird[0].getWidth()/2, birdFirstPosition + bird[0].getHeight()/ 2, bird[0].getWidth() / 2);
		rectangleTopPipe = new Rectangle(
				positionPipemovimentHor,
				middlePositionVer + passageBetweenPipes + randomPassageBetweenPipes,
				topPipe.getWidth(),
				topPipe.getHeight()
		);

		rectangleBelowPipe =  new Rectangle(
				positionPipemovimentHor,
				middlePositionVer - belowPipe.getHeight() - passageBetweenPipes + randomPassageBetweenPipes,
				belowPipe.getWidth(),
				belowPipe.getHeight()
		);

		//teste de colisao
		if(Intersector.overlaps(birdCircle, rectangleBelowPipe) || Intersector.overlaps(birdCircle, rectangleTopPipe)
				|| birdFirstPosition <= 0 ||birdFirstPosition >= deviceHeight){
			gameStatus = 2;
		}

		/*//desenha formas
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.circle(birdCircle.x, birdCircle.y, birdCircle.radius);
		shapeRenderer.rect(rectangleTopPipe.x, rectangleTopPipe.y, rectangleTopPipe.width, rectangleTopPipe.height);
		shapeRenderer.rect(rectangleBelowPipe.x, rectangleBelowPipe.y, rectangleBelowPipe.width, rectangleBelowPipe.height);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.end();*/
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}
